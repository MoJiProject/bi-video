package com.qingmang.domain.social;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 站内通知，合掉旧的 at 表和散落在各处的 notification_* 计数 */
@Data
@Accessors(chain = true)
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("receiver_id")
    private Long receiverId;

    @TableField("actor_id")
    private Long actorId;

    private Integer type;

    @TableField("target_type")
    private Integer targetType;

    @TableField("target_id")
    private Long targetId;

    private String content;

    @TableField("is_read")
    private Boolean isRead;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
