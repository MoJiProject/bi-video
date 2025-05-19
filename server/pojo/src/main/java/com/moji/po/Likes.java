package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Likes implements Serializable {

    private Integer id;
    private Integer userId;
    private Integer fondId;
    private LocalDateTime likeTime;
    private Integer likeUserId;
    private Integer likeType;//1视频 2评论 3动态
    private Integer deleteFlag;//0否 1是
}
