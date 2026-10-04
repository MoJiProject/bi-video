package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 待看清单。旧库把它塞在 dynamic 表里并用中文字符串当主键的一部分，这里独立成表 */
@Data
@Accessors(chain = true)
@TableName("watch_later")
public class WatchLater implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("video_id")
    private Long videoId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
