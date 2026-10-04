package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 用户封禁 */
@Data
@Accessors(chain = true)
@TableName("user_ban")
public class UserBan implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String reason;

    private Boolean status;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("banned_at")
    private LocalDateTime bannedAt;

    @TableField("released_at")
    private LocalDateTime releasedAt;
}
