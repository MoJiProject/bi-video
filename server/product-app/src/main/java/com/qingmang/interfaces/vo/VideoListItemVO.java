package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoListItemVO {

    private Long id;
    private String title;
    private String coverUrl;
    private Integer durationSeconds;
    private LocalDateTime publishedAt;

    private Long ownerId;
    private String ownerNickname;
    private String ownerAvatar;

    private Long playCount;
    private Long danmakuCount;
    private Long likeCount;
    private Long coinCount;
    private Long favoriteCount;
    private Long commentCount;

    /** 当前登录用户对这条视频的互动状态，未登录全为 false */
    private Boolean liked = false;
    private Boolean coinGiven = false;
    private Boolean collected = false;
    /** 当前用户已投的硬币数 */
    private Integer myCoinCount = 0;
}