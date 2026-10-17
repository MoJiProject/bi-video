package com.qingmang.interfaces.vo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class WatchRoomVO {

    private String roomId;
    private Long videoId;
    private Long ownerId;
    private String ownerName;
    private double currentTime;
    private boolean paused = true;
    private double playbackRate = 1.0;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Set<Long> adminIds = new HashSet<>();
    private Set<Long> blacklistedIds = new HashSet<>();
    private List<WatchParticipantVO> participants = new ArrayList<>();
}
