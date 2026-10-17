package com.qingmang.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingmang.domain.user.User;
import com.qingmang.domain.user.mapper.UserMapper;
import com.qingmang.domain.video.Video;
import com.qingmang.domain.video.mapper.VideoMapper;
import com.qingmang.interfaces.dto.CreateWatchRoomRequest;
import com.qingmang.interfaces.dto.WatchMessage;
import com.qingmang.interfaces.vo.WatchParticipantVO;
import com.qingmang.interfaces.vo.WatchRoomVO;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class WatchTogetherService {

    private static final Duration ROOM_TTL = Duration.ofHours(24);
    private static final Duration EMPTY_ROOM_TTL = Duration.ofMinutes(30);
    private static final Duration HISTORY_TTL = Duration.ofDays(30);
    private static final int MAX_HISTORY = 50;
    private static final int ROOM_LOCK_COUNT = 64;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final VideoMapper videoMapper;
    private final Object[] roomLocks = new Object[ROOM_LOCK_COUNT];

    public WatchTogetherService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper,
                                UserMapper userMapper, VideoMapper videoMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.videoMapper = videoMapper;
        for (int index = 0; index < ROOM_LOCK_COUNT; index++) {
            roomLocks[index] = new Object();
        }
    }

    public WatchRoomVO createRoom(Long ownerId, CreateWatchRoomRequest request) {
        User owner = userMapper.selectById(ownerId);
        if (owner == null || request.getVideoId() == null) {
            throw new IllegalArgumentException("无法创建一起看房间");
        }
        Video video = videoMapper.selectById(request.getVideoId());
        if (video == null || video.getStatus() == null || video.getStatus() != 1) {
            throw new IllegalArgumentException("视频不存在或未通过审核");
        }

        WatchRoomVO room = new WatchRoomVO();
        room.setRoomId(UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        room.setVideoId(request.getVideoId());
        room.setOwnerId(ownerId);
        room.setOwnerName(displayName(owner));
        room.setCurrentTime(Math.max(0, request.getCurrentTime()));
        room.setPaused(request.isPaused());
        room.setPlaybackRate(normalizeRate(request.getPlaybackRate()));
        room.getParticipants().add(participant(owner, true, true, true, false));
        save(room, EMPTY_ROOM_TTL);
        return room;
    }

    public WatchRoomVO join(String roomId, Long userId) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireRoom(roomId);
            if (room.getBlacklistedIds().contains(userId)) {
                throw new SecurityException("你已被拉黑，无法加入该房间");
            }
            User user = userMapper.selectById(userId);
            if (user == null) throw new IllegalArgumentException("用户不存在");

            WatchParticipantVO item = findParticipant(room, userId);
            boolean admin = room.getOwnerId().equals(userId) || room.getAdminIds().contains(userId);
            if (item == null) {
                room.getParticipants().add(participant(user, room.getOwnerId().equals(userId), admin, true, false));
            } else {
                item.setUserName(displayName(user));
                item.setAvatarAddress(user.getAvatarUrl());
                item.setGender(user.getGender());
                item.setGrade(user.getLevel());
                item.setOnline(true);
                item.setBlacklisted(false);
                item.setOwner(room.getOwnerId().equals(userId));
                item.setAdmin(admin);
            }

            save(room);
            return room;
        });
    }

    public WatchRoomVO leave(String roomId, Long userId) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = findRoom(roomId);
            if (room == null) return null;
            WatchParticipantVO item = findParticipant(room, userId);
            if (item != null) item.setOnline(false);
            room.setPaused(true);
            save(room, hasOnlineParticipant(room) ? ROOM_TTL : EMPTY_ROOM_TTL);
            return room;
        });
    }

    public WatchRoomVO updatePlayback(String roomId, Long userId, WatchMessage message) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireMember(roomId, userId);
            room.setCurrentTime(Math.max(0, message.getCurrentTime()));
            room.setPlaybackRate(normalizeRate(message.getPlaybackRate()));
            if (message.getPaused() != null) room.setPaused(message.getPaused());
            save(room, ROOM_TTL, false);
            return room;
        });
    }

    public WatchRoomVO sync(String roomId, Long userId) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireMember(roomId, userId);
            save(room, ROOM_TTL, false);
            return room;
        });
    }

    public WatchRoomVO switchVideo(String roomId, Long actorId, Long videoId) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireRoom(roomId);
            if (!canManage(room, actorId)) throw new SecurityException("只有房主或管理员可以切换视频");
            if (videoId == null) throw new IllegalArgumentException("请选择要切换的视频");
            Video video = videoMapper.selectById(videoId);
            if (video == null || video.getStatus() == null || video.getStatus() != 1) {
                throw new IllegalArgumentException("视频不存在或未通过审核");
            }
            if (room.getVideoId().equals(videoId)) return room;
            room.setVideoId(videoId);
            room.setCurrentTime(0);
            room.setPaused(true);
            room.setPlaybackRate(1.0);
            save(room);
            return room;
        });
    }

    public WatchRoomVO setAdmin(String roomId, Long ownerId, Long targetUserId, boolean grant) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireRoom(roomId);
            if (!room.getOwnerId().equals(ownerId)) throw new SecurityException("只有房主可以分配管理权限");
            if (room.getOwnerId().equals(targetUserId)) return room;

            WatchParticipantVO target = requireParticipant(room, targetUserId);
            if (target.isBlacklisted()) throw new SecurityException("该成员已被拉黑，无法分配管理权限");
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

    public WatchRoomVO transferOwner(String roomId, Long ownerId, Long targetUserId) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireRoom(roomId);
            if (!room.getOwnerId().equals(ownerId)) throw new SecurityException("只有房主可以转让房间");
            if (room.getOwnerId().equals(targetUserId)) return room;

            WatchParticipantVO target = requireParticipant(room, targetUserId);
            if (target.isBlacklisted()) throw new SecurityException("该成员已被拉黑，无法转让房主");

            User newOwner = userMapper.selectById(targetUserId);
            room.setOwnerId(targetUserId);
            room.setOwnerName(newOwner == null ? target.getUserName() : displayName(newOwner));
            if (newOwner != null) {
                target.setUserName(displayName(newOwner));
                target.setAvatarAddress(newOwner.getAvatarUrl());
                target.setGender(newOwner.getGender());
                target.setGrade(newOwner.getLevel());
            }
            room.getAdminIds().remove(ownerId);
            room.getParticipants().forEach(participant -> {
                boolean owner = participant.getUserId().equals(targetUserId);
                participant.setOwner(owner);
                participant.setAdmin(owner || room.getAdminIds().contains(participant.getUserId()));
            });
            save(room);
            return room;
        });
    }

    public WatchRoomVO kick(String roomId, Long actorId, Long targetUserId) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireRoom(roomId);
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

    public WatchRoomVO setBlacklist(String roomId, Long actorId, Long targetUserId, boolean blocked) {
        return withRoomLock(roomId, () -> {
            WatchRoomVO room = requireRoom(roomId);
            if (!canManage(room, actorId)) throw new SecurityException("没有管理权限");
            if (room.getOwnerId().equals(targetUserId)) throw new SecurityException("不能拉黑房主");
            if (!room.getOwnerId().equals(actorId) && room.getAdminIds().contains(targetUserId)) {
                throw new SecurityException("管理员不能拉黑其他管理员");
            }
            if (blocked) {
                room.getBlacklistedIds().add(targetUserId);
                room.getAdminIds().remove(targetUserId);
                redisTemplate.opsForZSet().remove(historyKey(targetUserId), roomId);
                room.setPaused(true);
            } else {
                room.getBlacklistedIds().remove(targetUserId);
            }
            WatchParticipantVO target = findParticipant(room, targetUserId);
            if (target != null) {
                target.setBlacklisted(blocked);
                if (blocked) {
                    target.setOnline(false);
                    target.setAdmin(false);
                }
            }
            save(room);
            return room;
        });
    }

    public List<WatchRoomVO> history(Long userId) {
        Set<String> roomIds = redisTemplate.opsForZSet().reverseRange(historyKey(userId), 0, MAX_HISTORY - 1);
        List<WatchRoomVO> rooms = new ArrayList<>();
        if (roomIds == null) return rooms;
        for (String roomId : roomIds) {
            WatchRoomVO room = findRoom(roomId);
            if (room == null) {
                redisTemplate.opsForZSet().remove(historyKey(userId), roomId);
            } else if (canManage(room, userId)) {
                rooms.add(room);
            }
        }
        return rooms;
    }

    public WatchRoomVO findRoom(String roomId) {
        String json = redisTemplate.opsForValue().get(roomKey(roomId));
        if (json == null) return null;
        try {
            WatchRoomVO room = objectMapper.readValue(json, WatchRoomVO.class);
            advancePlayingTime(room);
            return room;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("房间状态读取失败", e);
        }
    }

    public boolean canManage(WatchRoomVO room, Long userId) {
        return room.getOwnerId().equals(userId) || room.getAdminIds().contains(userId);
    }

    private WatchRoomVO requireMember(String roomId, Long userId) {
        WatchRoomVO room = requireRoom(roomId);
        if (room.getBlacklistedIds().contains(userId)) throw new SecurityException("你已被拉黑，无法继续一起看");
        if (findParticipant(room, userId) == null) throw new SecurityException("尚未加入房间");
        return room;
    }

    private WatchRoomVO requireRoom(String roomId) {
        WatchRoomVO room = findRoom(roomId);
        if (room == null) throw new IllegalArgumentException("房间不存在或已过期");
        return room;
    }

    private WatchParticipantVO requireParticipant(WatchRoomVO room, Long userId) {
        WatchParticipantVO target = findParticipant(room, userId);
        if (target == null) throw new IllegalArgumentException("成员不在房间中");
        return target;
    }

    private WatchParticipantVO findParticipant(WatchRoomVO room, Long userId) {
        return room.getParticipants().stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private WatchParticipantVO participant(User user, boolean owner, boolean admin, boolean online, boolean blacklisted) {
        return new WatchParticipantVO(
                user.getId(),
                displayName(user),
                user.getAvatarUrl(),
                user.getGender(),
                user.getLevel(),
                owner,
                admin,
                online,
                blacklisted
        );
    }

    private String displayName(User user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) return user.getNickname();
        return user.getUsername();
    }

    private void save(WatchRoomVO room) {
        save(room, ROOM_TTL);
    }

    private void save(WatchRoomVO room, Duration ttl) {
        save(room, ttl, true);
    }

    private void save(WatchRoomVO room, Duration ttl, boolean updateHistory) {
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

    private void addHistory(Long userId, String roomId, Instant updatedAt) {
        String key = historyKey(userId);
        redisTemplate.opsForZSet().add(key, roomId, updatedAt.toEpochMilli());
        redisTemplate.opsForZSet().removeRange(key, 0, -MAX_HISTORY - 1);
        redisTemplate.expire(key, HISTORY_TTL);
    }

    private double normalizeRate(double rate) {
        return rate >= 0.5 && rate <= 2.0 ? rate : 1.0;
    }

    private boolean hasOnlineParticipant(WatchRoomVO room) {
        return room.getParticipants().stream().anyMatch(WatchParticipantVO::isOnline);
    }

    private void advancePlayingTime(WatchRoomVO room) {
        if (room.isPaused() || room.getUpdatedAt() == null) return;
        double elapsedSeconds = Duration.between(room.getUpdatedAt(), Instant.now()).toMillis() / 1000.0;
        room.setCurrentTime(room.getCurrentTime() + elapsedSeconds * room.getPlaybackRate());
    }

    private <T> T withRoomLock(String roomId, Supplier<T> operation) {
        if (roomId == null || roomId.isBlank()) throw new IllegalArgumentException("房间号不能为空");
        Object lock = roomLocks[Math.floorMod(roomId.hashCode(), ROOM_LOCK_COUNT)];
        synchronized (lock) {
            return operation.get();
        }
    }

    private String roomKey(String roomId) {
        return "watch:room:" + roomId;
    }

    private String historyKey(Long userId) {
        return "watch:history:" + userId;
    }
}
