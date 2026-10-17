package com.qingmang.interfaces.dto;

import lombok.Data;

@Data
public class CreateWatchRoomRequest {

    private Long videoId;
    private double currentTime;
    private boolean paused = true;
    private double playbackRate = 1.0;
}
