package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频维度计数（派生数据，与 video 1:1）
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code video_stats}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("video_stats")
public class VideoStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 视频ID */
    @TableField("video_id")
    private Long videoId;

    /** 播放量 */
    @TableField("play_count")
    private Long playCount;

    /** 弹幕数 */
    @TableField("danmaku_count")
    private Integer danmakuCount;

    /** 点赞数 */
    @TableField("like_count")
    private Integer likeCount;

    /** 投币数 */
    @TableField("coin_count")
    private Integer coinCount;

    /** 收藏数 */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /** 分享数 */
    @TableField("share_count")
    private Integer shareCount;

    /** 评论数 */
    @TableField("comment_count")
    private Integer commentCount;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
