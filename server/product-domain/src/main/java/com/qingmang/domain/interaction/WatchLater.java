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
 * 待看清单。旧库把它塞在 dynamic 表里并用中文字符串当主键的一部分，这里独立成表
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code watch_later}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("watch_later")
public class WatchLater implements Serializable {

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

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
