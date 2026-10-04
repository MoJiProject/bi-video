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
 * 用户隐私与通知设置
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code user_privacy}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("user_privacy")
public class UserPrivacy implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 公开收藏夹 */
    @TableField("show_favorite_list")
    private Boolean showFavoriteList;

    /** 公开关注列表 */
    @TableField("show_follow_list")
    private Boolean showFollowList;

    /** 公开粉丝列表 */
    @TableField("show_fans_list")
    private Boolean showFansList;

    /** 公开投币收入 */
    @TableField("show_coin_income")
    private Boolean showCoinIncome;

    /** 公开获赞数 */
    @TableField("show_like_received")
    private Boolean showLikeReceived;

    /** 公开观看记录 */
    @TableField("show_history")
    private Boolean showHistory;

    /** 接收动态通知 */
    @TableField("notify_dynamic")
    private Boolean notifyDynamic;

    /** 接收评论通知 */
    @TableField("notify_comment")
    private Boolean notifyComment;

    /** 接收点赞通知 */
    @TableField("notify_like")
    private Boolean notifyLike;

    /** 接收@通知 */
    @TableField("notify_at")
    private Boolean notifyAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
