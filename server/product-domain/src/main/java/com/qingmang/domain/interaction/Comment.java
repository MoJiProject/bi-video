package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 评论（视频/动态共用）
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code comment}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 视频ID，动态评论时为 NULL */
    @TableField("video_id")
    private Long videoId;

    /** 动态ID，与 video_id 二选一 */
    @TableField("post_id")
    private Long postId;

    /** 评论者 */
    @TableField("author_id")
    private Long authorId;

    /** 根评论ID，一级评论为 NULL */
    @TableField("root_id")
    private Long rootId;

    /** 被回复的评论ID */
    @TableField("reply_to_id")
    private Long replyToId;

    /** 被回复者（冗余，省一次回表） */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /** 评论内容 */
    private String content;

    /** 图片地址数组 */
    @TableField("image_urls")
    private String imageUrls;

    /** 状态 0待审核 1正常 2已隐藏 */
    private Integer status;

    /** 点赞数 */
    @TableField("like_count")
    private Integer likeCount;

    /** 回复数 */
    @TableField("reply_count")
    private Integer replyCount;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 软删除 */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
