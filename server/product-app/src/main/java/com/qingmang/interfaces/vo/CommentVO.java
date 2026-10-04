package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long videoId;
    private Long authorId;
    private String authorNickname;
    private String authorAvatar;
    private Integer authorLevel;

    private Long rootId;
    private Long replyToUserId;
    private String replyToNickname;

    private String content;
    /** 库里存的原始 JSON，取出后在 Service 里转成 imageUrls */
    private String imageUrlsJson;
    private List<String> imageUrls;
    private LocalDateTime createdAt;

    private Integer likeCount;
    private Integer replyCount;
    /** 当前登录用户是否赞过、是否踩过 */
    private Boolean liked = false;
    private Boolean disliked = false;

    /** 回复列表，只在根节点上填充 */
    private List<CommentVO> replies;
}