package com.moji.vo;

import com.moji.dto.WatchParticipant;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class WatchRoom {

    private String roomId;
    private Integer videoId;
    private Integer ownerId;
    private String ownerName;
    private double currentTime;
    private boolean paused = true;
    private double playbackRate = 1.0;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Set<Integer> adminIds = new HashSet<>();
    private Set<Integer> blacklistedIds = new HashSet<>();
    private List<WatchParticipant> participants = new ArrayList<>();
}
