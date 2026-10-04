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
 * 敏感词库
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code sys_sensitive_word}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("sys_sensitive_word")
public class SysSensitiveWord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** id */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 敏感词 */
    private String word;

    /** 1评论 2私信 3昵称 4标题 */
    @TableField("word_type")
    private Integer wordType;

    /** is_enabled */
    @TableField("is_enabled")
    private Boolean isEnabled;

    /** created_at */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
