package com.qingmang.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 用户隐私与通知设置
 */
@Data
@Accessors(chain = true)
@TableName("user_privacy")
public class UserPrivacy implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    private Long userId;

    @TableField("show_favorite_list")
    private Boolean showFavoriteList;

    @TableField("show_follow_list")
    private Boolean showFollowList;

    @TableField("show_fans_list")
    private Boolean showFansList;

    @TableField("show_coin_list")
    private Boolean showCoinList;

    @TableField("show_like_list")
    private Boolean showLikeList;

    @TableField("show_birthday")
    private Boolean showBirthday;

    @TableField("record_history")
    private Boolean recordHistory;

    @TableField("home_section_order")
    private String homeSectionOrder;

    @TableField("show_coin_income")
    private Boolean showCoinIncome;

    @TableField("show_like_received")
    private Boolean showLikeReceived;

    @TableField("show_history")
    private Boolean showHistory;

    @TableField("notify_dynamic")
    private Boolean notifyDynamic;

    @TableField("notify_comment")
    private Integer notifyComment;

    @TableField("notify_like")
    private Boolean notifyLike;

    @TableField("notify_at")
    private Integer notifyAt;

    @TableField("notify_message")
    private Boolean notifyMessage;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
