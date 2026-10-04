package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户封禁
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code user_ban}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("user_ban")
public class UserBan implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 被封禁用户 */
    @TableField("user_id")
    private Long userId;

    /** 原因 */
    private String reason;

    /** 1封禁中 0已解除 */
    private Boolean status;

    /** 操作管理员 */
    @TableField("operator_id")
    private Long operatorId;

    /** banned_at */
    @TableField("banned_at")
    private LocalDateTime bannedAt;

    /** 解除时间 */
    @TableField("released_at")
    private LocalDateTime releasedAt;
}
