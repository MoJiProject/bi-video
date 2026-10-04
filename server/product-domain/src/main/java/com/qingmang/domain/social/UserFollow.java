package com.qingmang.domain.social;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 关注关系。旧库另有 fans 表，与本表是同一份数据，这里只保留一份，粉丝列表反向查本表
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code user_follow}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("user_follow")
public class UserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关注者 */
    @TableField("follower_id")
    private Long followerId;

    /** 被关注者 */
    @TableField("followee_id")
    private Long followeeId;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
