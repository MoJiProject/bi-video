package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {

    private Long id;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private String authorSignature;

    private Long videoId;
    private String videoTitle;
    private String videoCover;
    private Integer videoDuration;

    private String content;
    /** 库里存的原始 JSON，取出后在 Service 里转成 imageUrls */
    private String imageUrls;
    private List<String> imageList;

    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Boolean liked = false;
    private Boolean isSelf = false;

    private LocalDateTime createdAt;
}