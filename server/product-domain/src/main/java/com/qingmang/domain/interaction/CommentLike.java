package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 评论点赞/踩 */
@Data
@Accessors(chain = true)
@TableName("comment_like")
public class CommentLike implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("comment_id")
    private Long commentId;

    @TableField("user_id")
    private Long userId;

    @TableField("is_dislike")
    private Boolean isDislike;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
