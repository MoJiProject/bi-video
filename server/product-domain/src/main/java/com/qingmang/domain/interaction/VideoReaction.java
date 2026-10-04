package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频互动（点赞/投币），合表后一次查询拿到全部状态
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code video_reaction}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("video_reaction")
public class VideoReaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 */
    @TableField("user_id")
    private Long userId;

    /** 视频ID */
    @TableField("video_id")
    private Long videoId;

    /** 类型 1点赞 2投币 3收藏到默认夹 */
    @TableField("reaction_type")
    private Integer reactionType;

    /** 投币数量，仅 type=2 有效 */
    @TableField("coin_count")
    private Integer coinCount;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
