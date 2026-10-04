package com.qingmang.domain.watchroom;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 一起看成员
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code watch_room_member}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("watch_room_member")
public class WatchRoomMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 房间ID */
    @TableField("room_id")
    private Long roomId;

    /** 成员 */
    @TableField("user_id")
    private Long userId;

    /** joined_at */
    @TableField("joined_at")
    private LocalDateTime joinedAt;
}
