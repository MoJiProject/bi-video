package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 视频主表，只放业务字段 */
@Data
@Accessors(chain = true)
@TableName("video")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("category_id")
    private Long categoryId;

    private String title;

    private String description;

    @TableField("description_html")
    private String descriptionHtml;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("play_url")
    private String playUrl;

    private Integer source;

    @TableField("remote_url")
    private String remoteUrl;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("allow_comment")
    private Boolean allowComment;

    @TableField("allow_danmaku")
    private Boolean allowDanmaku;

    @TableField("coin_enabled")
    private Boolean coinEnabled;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
