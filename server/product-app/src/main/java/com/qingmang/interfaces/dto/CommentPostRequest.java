package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CommentPostRequest {

    @NotNull(message = "缺少视频ID")
    private Long videoId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论最多 1000 字")
    private String content;

    /** 回复某条评论时传，被回复人从 rootId 链路上推导，这里只作冗余展示。 */
    private Long rootId;
    private Long replyToUserId;

    @Size(max = 9, message = "最多 9 张图片")
    private List<@Size(max = 512) String> imageUrls;
}