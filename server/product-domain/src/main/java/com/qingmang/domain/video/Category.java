package com.qingmang.domain.video;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 视频分区，替代旧的 sub_zone_key 魔法字符串
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code category}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分类ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类编码，如 anime */
    private String code;

    /** 分类名，如 动画 */
    private String name;

    /** 父分类，NULL 表示一级 */
    @TableField("parent_id")
    private Long parentId;

    /** 排序，越小越靠前 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 是否启用 */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** updated_at */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
