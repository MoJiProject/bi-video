package com.qingmang.domain.social;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 私信会话。旧库每次收发都全表扫 sender/receiver，这里先定位会话再取消息 */
@Data
@Accessors(chain = true)
@TableName("conversation")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_a_id")
    private Long userAId;

    @TableField("user_b_id")
    private Long userBId;

    @TableField("last_message_id")
    private Long lastMessageId;

    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
