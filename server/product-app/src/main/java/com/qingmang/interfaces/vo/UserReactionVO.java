package com.qingmang.interfaces.vo;

import lombok.Data;

/** 用户对某视频的互动状态，来自 video_reaction。 */
@Data
public class UserReactionVO {

    private Long videoId;
    private Integer reactionType;
    private Integer coinCount;
}