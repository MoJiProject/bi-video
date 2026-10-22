package com.qingmang.interfaces.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileVO {

    private Long userId;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String backgroundUrl;
    private String signature;
    private Integer gender;
    private LocalDate birthday;
    private Integer level;
    private Integer exp;
    private Integer coinBalance;
    private Integer role;
    private Boolean followed;
    private Boolean isSelf;
    private Boolean showFavoriteList;
    private Boolean showFollowList;
    private Boolean showFansList;
    private Boolean showCoinList;
    private Boolean showLikeList;
    private Boolean showBirthday;
    private Boolean recordHistory;
    private java.util.List<String> homeSectionOrder;
    private LocalDateTime createdAt;

    private Long videoCount;
    private Long postCount;
    private Long favoriteCount;
    private Long followerCount;
    private Long followingCount;
    private Long likeReceived;
    private Long playTotal;
    private Long coinReceived;
    private Long messageNumber;
    private Long replyCommentNumber;
    private Long atNumber;
    private Long likeAllNumber;
    private Long allMessageNumber;
    private Integer messageWarn;
    private Integer replyMessageWarn;
    private Integer atMessageWarn;
    private Integer likeMessageWarn;
}