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
public class VideosDto implements Serializable {

    private Integer userId;
    private Integer likeNumber;
    private Integer commentNumber;//收藏数量
    private Integer collectNumber;
    private Integer shareNumber;
    private Integer coinThrowNumber;
    private Integer newFansNumber;//在该视频下新增粉丝的数量
    private Integer scrollingNumber;//弹幕数量
    private Integer playNumber;//播放量

}
