package com.qingmang.domain.social.mapper;

import com.qingmang.domain.social.UserPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * {@link UserPost} 的 Mapper。
 *
 * <p>单表 CRUD 走 {@link BaseMapper}；需要连表或批量 IN 查询的方法写在同名 XML 里，
 * 避免为了一个统计查询去开 {@code @Select} 注解把 SQL 散落在 Java 代码中。</p>
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPost> {
}
