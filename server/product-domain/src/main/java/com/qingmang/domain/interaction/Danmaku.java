package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 弹幕。刻意不加外键：写入极频繁，外键校验会成为瓶颈，且视频删除由应用层按 video_id 批量清理 */
@Data
@Accessors(chain = true)
@TableName("danmaku")
public class Danmaku implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("video_id")
    private Long videoId;

    @TableField("user_id")
    private Long userId;

    private String content;

    private String color;

    @TableField("font_size")
    private Integer fontSize;

    private Integer mode;

    @TableField("video_time_ms")
    private Integer videoTimeMs;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
