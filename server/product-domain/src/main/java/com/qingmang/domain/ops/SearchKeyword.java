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
 * 搜索词热榜
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code search_keyword}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("search_keyword")
public class SearchKeyword implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关键词 */
    private String keyword;

    /** 累计搜索次数 */
    @TableField("search_count")
    private Integer searchCount;

    /** 最后搜索时间 */
    @TableField("last_searched_at")
    private LocalDateTime lastSearchedAt;
}
