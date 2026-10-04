package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频主表，只放业务字段
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code video}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("video")
public class Video implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创作者ID */
    @TableField("owner_id")
    private Long ownerId;

    /** 分区ID */
    @TableField("category_id")
    private Long categoryId;

    /** 标题 */
    private String title;

    /** 简介（纯文本） */
    private String description;

    /** 简介（富文本） */
    @TableField("description_html")
    private String descriptionHtml;

    /** 封面地址 */
    @TableField("cover_url")
    private String coverUrl;

    /** 播放地址 */
    @TableField("play_url")
    private String playUrl;

    /** 来源 0本地 1远程直链 */
    private Integer source;

    /** 远程直链，source=1 时使用 */
    @TableField("remote_url")
    private String remoteUrl;

    /** 时长（秒），秒级时间轴 */
    @TableField("duration_seconds")
    private Integer durationSeconds;

    /** 状态 0草稿 1已发布 2审核中 3已下架 */
    private Integer status;

    /** 下架/驳回原因 */
    @TableField("reject_reason")
    private String rejectReason;

    /** 允许评论 */
    @TableField("allow_comment")
    private Boolean allowComment;

    /** 允许弹幕 */
    @TableField("allow_danmaku")
    private Boolean allowDanmaku;

    /** 允许投币 */
    @TableField("coin_enabled")
    private Boolean coinEnabled;

    /** 发布时间 */
    @TableField("published_at")
    private LocalDateTime publishedAt;

    /** 创建时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 软删除时间 */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
