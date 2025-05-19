package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectHistoryDto implements Serializable {

    private Integer historyId;
    private Integer videoId;
    private String watchVideoDate;
    private String watchVideoDateTime;
    private String watchVideoTime;
    private String videoCoverAddress;
    private String videoUserName;
    private String videoTitle;
    private String videoTime;
    private Integer watchVideoFlag;//是否观看完

}
