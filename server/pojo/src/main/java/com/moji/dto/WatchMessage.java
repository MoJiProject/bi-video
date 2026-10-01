package com.moji.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class WatchMessage {

    private String type;
    private String roomId;
    private Integer videoId;
    private Integer targetUserId;
    private double currentTime;
    private double playbackRate = 1.0;
    private Boolean paused;
    private JsonNode data;
}
