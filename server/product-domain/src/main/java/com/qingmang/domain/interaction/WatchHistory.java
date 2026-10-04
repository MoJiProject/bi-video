package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 观看记录。每个用户每个视频只留一条（旧库也是这样，这里把进度拆成秒数而不是字符串）
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code watch_history}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("watch_history")
public class WatchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 视频ID */
    @TableField("video_id")
    private Long videoId;

    /** 观看进度（秒） */
    @TableField("progress_seconds")
    private Integer progressSeconds;

    /** 观看次数 */
    @TableField("watched_times")
    private Integer watchedTimes;

    /** 最后观看时间 */
    @TableField("last_watched_at")
    private LocalDateTime lastWatchedAt;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
