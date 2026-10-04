package com.qingmang.domain.interaction;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 视频互动（点赞/投币），合表后一次查询拿到全部状态 */
@Data
@Accessors(chain = true)
@TableName("video_reaction")
public class VideoReaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("video_id")
    private Long videoId;

    @TableField("reaction_type")
    private Integer reactionType;

    @TableField("coin_count")
    private Integer coinCount;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
