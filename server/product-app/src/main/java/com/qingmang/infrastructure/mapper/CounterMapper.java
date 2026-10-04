package com.qingmang.infrastructure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 计数列的原子自增。
 *
 * <p>老代码是「先 select 出计数 → Java 里 +1 → update 回去」，
 * 并发下会互相覆盖。这里一律用数据库自增，少一次往返也没有竞态。</p>
 */
@Mapper
public interface CounterMapper {

    /** video_stats 某列 +1；行不存在时先补一行。 */
    int increaseVideoStat(@Param("videoId") Long videoId, @Param("column") String column,
                          @Param("delta") int delta);

    /** video_stats 某列 -1，下限为 0。 */
    int decreaseVideoStat(@Param("videoId") Long videoId, @Param("column") String column,
                          @Param("delta") int delta);

    /** user_stats 某列 +delta。 */
    int increaseUserStat(@Param("userId") Long userId, @Param("column") String column,
                         @Param("delta") int delta);

    /** 硬币余额 +delta，投币扣币时用 delta 传负数。 */
    int addCoinBalance(@Param("userId") Long userId, @Param("delta") int delta);

    /**
     * 扣硬币，余额不足时一条都不扣。
     *
     * @return 1 扣成功，0 余额不足
     */
    int deductCoinBalance(@Param("userId") Long userId, @Param("delta") int delta);
}