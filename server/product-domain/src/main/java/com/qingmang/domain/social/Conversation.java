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
 * 私信会话。旧库每次收发都全表扫 sender/receiver，这里先定位会话再取消息
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code conversation}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("conversation")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话成员A（恒为较小ID，保证唯一） */
    @TableField("user_a_id")
    private Long userAId;

    /** 会话成员B */
    @TableField("user_b_id")
    private Long userBId;

    /** 最后一条消息 */
    @TableField("last_message_id")
    private Long lastMessageId;

    /** 最后消息时间 */
    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
