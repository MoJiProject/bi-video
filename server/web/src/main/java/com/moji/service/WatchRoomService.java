package com.moji.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moji.dto.WatchMessage;
import com.moji.dto.WatchParticipant;
import com.moji.mapper.UserMapper;
import com.moji.po.Users;
import com.moji.vo.CreateWatchRoomRequest;
import com.moji.vo.WatchRoom;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class WatchRoomService {

    private static final Duration ROOM_TTL = Duration.ofHours(24);
    private static final Duration EMPTY_ROOM_TTL = Duration.ofMinutes(30);
    private static final Duration HISTORY_TTL = Duration.ofDays(30);
    private static final int MAX_HISTORY = 50;
    private static final int ROOM_LOCK_COUNT = 64;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final Object[] roomLocks = new Object[ROOM_LOCK_COUNT];

    public WatchRoomService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, UserMapper userMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        for (int index = 0; index < ROOM_LOCK_COUNT; index++) {
            roomLocks[index] = new Object();
        }
    }

    public WatchRoom createRoom(Integer ownerId, CreateWatchRoomRequest request) {
        Users owner = userMapper.selectById(ownerId);
        if (owner == null || request.getVideoId() == null) {
            throw new IllegalArgumentException("无法创建一起看房间");
        }

        WatchRoom room = new WatchRoom();
        room.setRoomId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        room.setVideoId(request.getVideoId());
        room.setOwnerId(ownerId);
        room.setOwnerName(owner.getUserName());
        room.setCurrentTime(Math.max(0, request.getCurrentTime()));
        room.setPaused(request.isPaused());
        room.setPlaybackRate(normalizeRate(request.getPlaybackRate()));
        room.getParticipants().add(new WatchParticipant(
            ownerId, owner.getUserName(), owner.getAvatarAddress(), true, true, false
        ));
        save(room, EMPTY_ROOM_TTL);
        return room;
    }

    public WatchRoom join(String roomId, Integer userId) {
        return withRoomLock(roomId, () -> {
            WatchRoom room = requireRoom(roomId);
            advancePlayingTime(room);
            Users user = userMapper.selectById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }

            WatchParticipant participant = findParticipant(room, userId);
            if (participant == null) {
                participant = new WatchParticipant(
                        userId,
                        user.getUserName(),
                        user.getAvatarAddress(),
                        room.getOwnerId().equals(userId),
                        room.getOwnerId().equals(userId) || room.getAdminIds().contains(userId),
                        true
                );
                room.getParticipants().add(participant);
            } else {
                participant.setUserName(user.getUserName());
                participant.setAvatarAddress(user.getAvatarAddress());
                participant.setOnline(true);
                participant.setOwner(room.getOwnerId().equals(userId));
                participant.setAdmin(participant.isOwner() || room.getAdminIds().contains(userId));
            }

            save(room);
            return room;
        });
    }

    public WatchRoom leave(String roomId, Integer userId) {
        return withRoomLock(roomId, () -> {
            WatchRoom room = findRoom(roomId);
            if (room == null) return null;

            WatchParticipant participant = findParticipant(room, userId);
            if (participant != null) participant.setOnline(false);
            room.setPaused(true);
            save(room, hasOnlineParticipant(room) ? ROOM_TTL : EMPTY_ROOM_TTL);
            return room;
        });
    }

    public WatchRoom updatePlayback(String roomId, Integer userId, WatchMessage message) {
        return withRoomLock(roomId, () -> {
            WatchRoom room = requireMember(roomId, userId);
            room.setCurrentTime(Math.max(0, message.getCurrentTime()));
            room.setPlaybackRate(normalizeRate(message.getPlaybackRate()));
            if (message.getPaused() != null) room.setPaused(message.getPaused());
            save(room, ROOM_TTL, false);
            return room;
        });
    }

    public WatchRoom setAdmin(String roomId, Integer ownerId, Integer targetUserId, boolean grant) {
        return withRoomLock(roomId, () -> {
            WatchRoom room = requireRoom(roomId);
            if (!room.getOwnerId().equals(ownerId)) {
                throw new SecurityException("只有房主可以分配管理权限");
            }
            if (room.getOwnerId().equals(targetUserId)) return room;

            WatchParticipant target = findParticipant(room, targetUserId);
            if (target == null) throw new IllegalArgumentException("成员不在房间中");

            if (grant) {
                room.getAdminIds().add(targetUserId);
                addHistory(targetUserId, roomId, room.getUpdatedAt());
            } else {
                room.getAdminIds().remove(targetUserId);
                redisTemplate.opsForZSet().remove(historyKey(targetUserId), roomId);
            }
            target.setAdmin(grant);
            save(room);
            return room;
        });
    }

    public WatchRoom kick(String roomId, Integer actorId, Integer targetUserId) {
        return withRoomLock(roomId, () -> {
            WatchRoom room = requireRoom(roomId);
            if (!canManage(room, actorId)) throw new SecurityException("没有管理权限");
            if (room.getOwnerId().equals(targetUserId)) throw new SecurityException("不能移除房主");
            if (!room.getOwnerId().equals(actorId) && room.getAdminIds().contains(targetUserId)) {
                throw new SecurityException("管理员不能移除其他管理员");
            }

            room.getParticipants().removeIf(participant -> participant.getUserId().equals(targetUserId));
            room.getAdminIds().remove(targetUserId);
            redisTemplate.opsForZSet().remove(historyKey(targetUserId), roomId);
            room.setPaused(true);
            save(room);
            return room;
        });
    }

    public List<WatchRoom> history(Integer userId) {
        Set<String> roomIds = redisTemplate.opsForZSet().reverseRange(historyKey(userId), 0, MAX_HISTORY - 1);
        List<WatchRoom> rooms = new ArrayList<>();
        if (roomIds == null) return rooms;

        for (String roomId : roomIds) {
            WatchRoom room = findRoom(roomId);
            if (room == null) {
                redisTemplate.opsForZSet().remove(historyKey(userId), roomId);
            } else if (canManage(room, userId)) {
                rooms.add(room);
            }
        }
        return rooms;
    }

    public WatchRoom findRoom(String roomId) {
        String json = redisTemplate.opsForValue().get(roomKey(roomId));
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, WatchRoom.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("房间状态读取失败", e);
        }
    }

    public boolean canManage(WatchRoom room, Integer userId) {
        return room.getOwnerId().equals(userId) || room.getAdminIds().contains(userId);
    }

    private WatchRoom requireMember(String roomId, Integer userId) {
        WatchRoom room = requireRoom(roomId);
        if (findParticipant(room, userId) == null) throw new SecurityException("尚未加入房间");
        return room;
    }

    private WatchRoom requireRoom(String roomId) {
        WatchRoom room = findRoom(roomId);
        if (room == null) throw new IllegalArgumentException("房间不存在或已过期");
        return room;
    }

    private WatchParticipant findParticipant(WatchRoom room, Integer userId) {
        return room.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private void save(WatchRoom room) {
        save(room, ROOM_TTL);
    }

    private void save(WatchRoom room, Duration ttl) {
        save(room, ttl, true);
    }

    private void save(WatchRoom room, Duration ttl, boolean updateHistory) {
        room.setUpdatedAt(Instant.now());
        try {
            redisTemplate.opsForValue().set(roomKey(room.getRoomId()), objectMapper.writeValueAsString(room), ttl);
            if (updateHistory) {
                addHistory(room.getOwnerId(), room.getRoomId(), room.getUpdatedAt());
                room.getAdminIds().forEach(adminId -> addHistory(adminId, room.getRoomId(), room.getUpdatedAt()));
            }
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("房间状态保存失败", e);
        }
    }

    private void addHistory(Integer userId, String roomId, Instant updatedAt) {
        String key = historyKey(userId);
        redisTemplate.opsForZSet().add(key, roomId, updatedAt.toEpochMilli());
        redisTemplate.opsForZSet().removeRange(key, 0, -MAX_HISTORY - 1);
        redisTemplate.expire(key, HISTORY_TTL);
    }

    private double normalizeRate(double rate) {
        return rate >= 0.5 && rate <= 2.0 ? rate : 1.0;
    }

    private boolean hasOnlineParticipant(WatchRoom room) {
        return room.getParticipants().stream().anyMatch(WatchParticipant::isOnline);
    }

    private void advancePlayingTime(WatchRoom room) {
        if (room.isPaused() || room.getUpdatedAt() == null) return;
        double elapsedSeconds = Duration.between(room.getUpdatedAt(), Instant.now()).toMillis() / 1000.0;
        room.setCurrentTime(room.getCurrentTime() + elapsedSeconds * room.getPlaybackRate());
    }

    private <T> T withRoomLock(String roomId, Supplier<T> operation) {
        Object lock = roomLocks[Math.floorMod(roomId.hashCode(), ROOM_LOCK_COUNT)];
        synchronized (lock) {
            return operation.get();
        }
    }

    private String roomKey(String roomId) {
        return "watch:room:" + roomId;
    }

    private String historyKey(Integer userId) {
        return "watch:history:" + userId;
    }
}
