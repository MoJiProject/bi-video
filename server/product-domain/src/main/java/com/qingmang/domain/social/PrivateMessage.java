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
 * 私信消息
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code private_message}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("private_message")
public class PrivateMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话ID */
    @TableField("conversation_id")
    private Long conversationId;

    /** 发送者 */
    @TableField("sender_id")
    private Long senderId;

    /** 接收者（冗余，便于按人查） */
    @TableField("receiver_id")
    private Long receiverId;

    /** 类型 1文本 2图片 3系统 */
    @TableField("message_type")
    private Integer messageType;

    /** 内容 */
    private String content;

    /** 已读 */
    @TableField("is_read")
    private Boolean isRead;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** deleted_at */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
