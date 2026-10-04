package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 系统配置项 */
@Data
@Accessors(chain = true)
@TableName("sys_config")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    private String description;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
