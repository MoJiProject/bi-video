package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 搜索词热榜 */
@Data
@Accessors(chain = true)
@TableName("search_keyword")
public class SearchKeyword implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;

    @TableField("search_count")
    private Integer searchCount;

    @TableField("last_searched_at")
    private LocalDateTime lastSearchedAt;
}
