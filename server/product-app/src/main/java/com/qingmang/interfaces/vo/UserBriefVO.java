package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserBriefVO {

    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String signature;
    private Integer level;
    private Boolean followed = false;
    private LocalDateTime followedAt;
}