package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 敏感词库 */
@Data
@Accessors(chain = true)
@TableName("sys_sensitive_word")
public class SysSensitiveWord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String word;

    @TableField("word_type")
    private Integer wordType;

    @TableField("is_enabled")
    private Boolean isEnabled;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
