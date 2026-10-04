package com.qingmang.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 搜索词统计。
 *
 * <p>两张表的主键都不是自增 int（分别是 keyword 和 stat_date+keyword），
 * 用不了 BaseMapper，全部走 XML。</p>
 */
@Mapper
public interface SearchStatMapper {

    /** 当天表自增；返回 0 表示这行还不存在。 */
    int bumpDaily(@Param("statDate") LocalDate statDate, @Param("keyword") String keyword);

    int insertDaily(@Param("statDate") LocalDate statDate, @Param("keyword") String keyword);

    /** 总表自增。 */
    int bumpTotal(@Param("keyword") String keyword, @Param("at") LocalDateTime at);

    /** 总表里没有这个关键词就插入一行。 */
    int insertTotal(@Param("keyword") String keyword, @Param("at") LocalDateTime at);
}