package com.qingmang.domain.ops;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 搜索词按天分表统计，日榜从这张表聚合，不用扫全量热榜
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code search_keyword_daily}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("search_keyword_daily")
public class SearchKeywordDaily implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 统计日期 */
    @TableField("stat_date")
    private LocalDate statDate;

    /** 关键词 */
    private String keyword;

    /** 当日搜索次数 */
    @TableField("search_count")
    private Integer searchCount;
}
