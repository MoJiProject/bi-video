package com.qingmang.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户维度计数（派生数据，由业务层维护）
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code user_stats}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("user_stats")
public class UserStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 投稿数 */
    @TableField("video_count")
    private Integer videoCount;

    /** 动态数 */
    @TableField("post_count")
    private Integer postCount;

    /** 收藏视频数 */
    @TableField("favorite_count")
    private Integer favoriteCount;

    /** 粉丝数 */
    @TableField("follower_count")
    private Integer followerCount;

    /** 关注数 */
    @TableField("following_count")
    private Integer followingCount;

    /** 累计获赞 */
    @TableField("like_received")
    private Integer likeReceived;

    /** 累计播放 */
    @TableField("play_total")
    private Long playTotal;

    /** 累计收到投币 */
    @TableField("coin_received")
    private Integer coinReceived;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
