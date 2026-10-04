package com.qingmang.domain.social;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 关注关系。旧库另有 fans 表，与本表是同一份数据，这里只保留一份，粉丝列表反向查本表 */
@Data
@Accessors(chain = true)
@TableName("user_follow")
public class UserFollow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("follower_id")
    private Long followerId;

    @TableField("followee_id")
    private Long followeeId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
