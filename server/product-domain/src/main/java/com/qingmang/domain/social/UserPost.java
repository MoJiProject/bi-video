package com.qingmang.domain.social;

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
 * 用户动态
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code user_post}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("user_post")
public class UserPost implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 动态ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布者 */
    @TableField("author_id")
    private Long authorId;

    /** 关联视频 */
    @TableField("video_id")
    private Long videoId;

    /** 文本内容 */
    private String content;

    /** 图片地址数组 */
    @TableField("image_urls")
    private String imageUrls;

    /** 可见性 0公开 1仅自己 */
    private Integer visibility;

    /** 状态 0待审核 1正常 2已下架 */
    private Integer status;

    /** like_count */
    @TableField("like_count")
    private Integer likeCount;

    /** comment_count */
    @TableField("comment_count")
    private Integer commentCount;

    /** share_count */
    @TableField("share_count")
    private Integer shareCount;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** deleted_at */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
