package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBriefVO {

    private Long userId;
    private Long relationId;
    private String nickname;
    private String avatarUrl;
    private String signature;
    private Integer level;
    private Boolean followed = false;
    private Long followerCount;
    private Long unreadCount;
    private LocalDateTime followedAt;
}