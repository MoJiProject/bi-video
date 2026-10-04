package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 观看记录。每个用户每个视频只留一条（旧库也是这样，这里把进度拆成秒数而不是字符串） */
@Data
@Accessors(chain = true)
@TableName("watch_history")
public class WatchHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("video_id")
    private Long videoId;

    @TableField("progress_seconds")
    private Integer progressSeconds;

    @TableField("watched_times")
    private Integer watchedTimes;

    @TableField("last_watched_at")
    private LocalDateTime lastWatchedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
