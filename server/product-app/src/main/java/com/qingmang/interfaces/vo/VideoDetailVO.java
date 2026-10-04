package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoDetailVO {

    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String playUrl;
    private Integer source;
    private String remoteUrl;
    private Integer durationSeconds;
    private LocalDateTime publishedAt;

    private Long ownerId;
    private String ownerNickname;
    private String ownerAvatar;
    private String ownerSignature;
    private Long followerCount;
    private Boolean ownerFollowed = false;

    private Long playCount;
    private Long danmakuCount;
    private Long likeCount;
    private Long coinCount;
    private Long favoriteCount;
    private Long commentCount;
    private Long shareCount;

    private Boolean liked = false;
    private Boolean coinGiven = false;
    private Boolean collected = false;
    private Integer myCoinCount = 0;
    /** 上次观看到的秒数，-1 表示没看过 */
    private Integer watchProgress = -1;
}