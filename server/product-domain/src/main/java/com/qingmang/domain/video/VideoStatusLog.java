package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 视频状态流转记录 */
@Data
@Accessors(chain = true)
@TableName("video_status_log")
public class VideoStatusLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("video_id")
    private Long videoId;

    @TableField("from_status")
    private Integer fromStatus;

    @TableField("to_status")
    private Integer toStatus;

    private String reason;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
