package com.moji.watch;

import lombok.Data;

@Data
public class CreateWatchRoomRequest {

    private Integer videoId;
    private double currentTime;
    private boolean paused = true;
    private double playbackRate = 1.0;
}
