package com.qingmang.interfaces.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class WatchMessage {

    private String type;
    private String roomId;
    private Long videoId;
    private Long targetUserId;
    private double currentTime;
    private double playbackRate = 1.0;
    private Boolean paused;
    private Long clientTime;
    private JsonNode data;
}
