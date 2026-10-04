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
 * 弹幕。刻意不加外键：写入极频繁，外键校验会成为瓶颈，且视频删除由应用层按 video_id 批量清理
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code danmaku}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("danmaku")
public class Danmaku implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 视频ID */
    @TableField("video_id")
    private Long videoId;

    /** 发送者，NULL 表示游客 */
    @TableField("user_id")
    private Long userId;

    /** 内容 */
    private String content;

    /** 颜色 #RRGGBB */
    private String color;

    /** 字号 */
    @TableField("font_size")
    private Integer fontSize;

    /** 模式 0滚动 1顶部 2底部 3彩色 4高级 */
    private Integer mode;

    /** 视频时间轴（毫秒） */
    @TableField("video_time_ms")
    private Integer videoTimeMs;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
