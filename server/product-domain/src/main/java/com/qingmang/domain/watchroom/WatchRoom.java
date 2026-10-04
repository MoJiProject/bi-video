package com.qingmang.domain.watchroom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 一起看房间 */
@Data
@Accessors(chain = true)
@TableName("watch_room")
public class WatchRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("owner_id")
    private Long ownerId;

    private String title;

    @TableField("video_id")
    private Long videoId;

    @TableField("access_code")
    private String accessCode;

    @TableField("is_private")
    private Boolean isPrivate;

    @TableField("member_count")
    private Integer memberCount;

    private Integer status;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
