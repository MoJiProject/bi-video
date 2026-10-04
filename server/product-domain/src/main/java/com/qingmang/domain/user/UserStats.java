package com.qingmang.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 用户维度计数（派生数据，由业务层维护） */
@Data
@Accessors(chain = true)
@TableName("user_stats")
public class UserStats implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    private Long userId;

    @TableField("video_count")
    private Integer videoCount;

    @TableField("post_count")
    private Integer postCount;

    @TableField("favorite_count")
    private Integer favoriteCount;

    @TableField("follower_count")
    private Integer followerCount;

    @TableField("following_count")
    private Integer followingCount;

    @TableField("like_received")
    private Integer likeReceived;

    @TableField("play_total")
    private Long playTotal;

    @TableField("coin_received")
    private Integer coinReceived;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
