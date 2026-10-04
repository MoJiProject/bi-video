package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频状态流转记录
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code video_status_log}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("video_status_log")
public class VideoStatusLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 视频ID */
    @TableField("video_id")
    private Long videoId;

    /** 变更前状态 */
    @TableField("from_status")
    private Integer fromStatus;

    /** 变更后状态 */
    @TableField("to_status")
    private Integer toStatus;

    /** 原因 */
    private String reason;

    /** 操作人（管理员） */
    @TableField("operator_id")
    private Long operatorId;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
