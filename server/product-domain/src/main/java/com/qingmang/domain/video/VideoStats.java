package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 视频维度计数（派生数据，与 video 1:1） */
@Data
@Accessors(chain = true)
@TableName("video_stats")
public class VideoStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("video_id")
    private Long videoId;

    @TableField("play_count")
    private Long playCount;

    @TableField("danmaku_count")
    private Integer danmakuCount;

    @TableField("like_count")
    private Integer likeCount;

    @TableField("coin_count")
    private Integer coinCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField("share_count")
    private Integer shareCount;

    @TableField("comment_count")
    private Integer commentCount;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
