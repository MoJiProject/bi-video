package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

/** 搜索词按天分表统计，日榜从这张表聚合，不用扫全量热榜 */
@Data
@Accessors(chain = true)
@TableName("search_keyword_daily")
public class SearchKeywordDaily implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("stat_date")
    private LocalDate statDate;

    private String keyword;

    @TableField("search_count")
    private Integer searchCount;
}
