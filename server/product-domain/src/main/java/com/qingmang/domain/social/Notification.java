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
 * 站内通知，合掉旧的 at 表和散落在各处的 notification_* 计数
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code notification}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收者 */
    @TableField("receiver_id")
    private Long receiverId;

    /** 触发者 */
    @TableField("actor_id")
    private Long actorId;

    /** 类型 1@我 2回复我 3评论我的视频 4点赞我的视频 5点赞我的评论 6关注我 7系统 */
    private Integer type;

    /** 对象类型 1视频 2评论 3动态 4用户 */
    @TableField("target_type")
    private Integer targetType;

    /** 对象ID */
    @TableField("target_id")
    private Long targetId;

    /** 展示文案（快照） */
    private String content;

    /** 已读 */
    @TableField("is_read")
    private Boolean isRead;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
