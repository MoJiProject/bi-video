package com.qingmang.domain.watchroom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 一起看成员 */
@Data
@Accessors(chain = true)
@TableName("watch_room_member")
public class WatchRoomMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("room_id")
    private Long roomId;

    @TableField("user_id")
    private Long userId;

    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
