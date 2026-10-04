package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 后台操作审计
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code operation_log}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人 */
    @TableField("operator_id")
    private Long operatorId;

    /** 操作人名快照 */
    @TableField("operator_name")
    private String operatorName;

    /** 模块 user/video/comment/post/message */
    private String module;

    /** 动作 delete/publish/ban/... */
    private String action;

    /** 对象类型 */
    @TableField("target_type")
    private String targetType;

    /** 对象ID */
    @TableField("target_id")
    private Long targetId;

    /** 补充说明 */
    private String detail;

    /** 是否成功 */
    private Boolean success;

    /** 来源IP（兼容IPv6） */
    private String ip;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
