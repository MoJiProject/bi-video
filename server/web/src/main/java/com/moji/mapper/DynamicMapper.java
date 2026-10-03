package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.po.Dynamic;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DynamicMapper extends BaseMapper<Dynamic> {


    List<Dynamic> homeDynamic(Integer homeUserId,Integer offset);


    List<Dynamic> getAllDynamic(Integer userId, Integer offset);

    List<Dynamic> getAllDynamic2(Integer userId, Integer offset);

    List<Dynamic> getAllDynamic3(Integer userId, Integer offset);

    /**
     * 批量插入粉丝动态。
     * 审核通过时要把同一个视频一次性推给所有粉丝，逐条insert会产生N次网络往返。
     *
     * @param list     待插入的动态
     * @param publishTime 发布时间
     * @return 影响行数
     */
    @Insert("<script>"
            + "INSERT INTO dynamic (follow_id,fans_id,video_id,watch_dynamic_flag,fans_flag,publish_time) VALUES "
            + "<foreach collection='list' item='item' separator=','>"
            + "(#{item.followId},#{item.fansId},#{item.videoId},#{item.watchDynamicFlag},#{item.fansFlag},#{publishTime})"
            + "</foreach>"
            + "</script>")
    int insertBatch(@Param("list") List<Dynamic> list, @Param("publishTime") LocalDateTime publishTime);
}
