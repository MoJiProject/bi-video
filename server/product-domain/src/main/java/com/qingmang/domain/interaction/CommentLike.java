package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 评论点赞/踩
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code comment_like}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("comment_like")
public class CommentLike implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @TableField("comment_id")
    private Long commentId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 1踩 0赞 */
    @TableField("is_dislike")
    private Boolean isDislike;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
