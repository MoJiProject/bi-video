package com.qingmang.domain.watchroom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 一起看房间
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code watch_room}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("watch_room")
public class WatchRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 房主 */
    @TableField("owner_id")
    private Long ownerId;

    /** 房间标题 */
    private String title;

    /** 当前播放视频 */
    @TableField("video_id")
    private Long videoId;

    /** 房间号，私密时必填 */
    @TableField("access_code")
    private String accessCode;

    /** 是否私密 */
    @TableField("is_private")
    private Boolean isPrivate;

    /** 成员数（派生） */
    @TableField("member_count")
    private Integer memberCount;

    /** 状态 0已结束 1进行中 */
    private Integer status;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
