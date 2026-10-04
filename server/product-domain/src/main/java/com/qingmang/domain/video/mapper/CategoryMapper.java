package com.qingmang.domain.video.mapper;

import com.qingmang.domain.video.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link Category} 的 Mapper。
 *
 * <p>单表 CRUD 走 {@link BaseMapper}；连表和批量 IN 查询写在同名 XML 里，
 * 不为了一个统计查询去开 {@code @Select} 注解把 SQL 散在 Java 代码中。</p>
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
