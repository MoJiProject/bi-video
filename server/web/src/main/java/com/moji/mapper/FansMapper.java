package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moji.po.Fans;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FansMapper extends BaseMapper<Fans> {

    @Select("SELECT * FROM fans " +
            "LEFT JOIN follow ON follow.follow_id = fans.fans_id " +
            "WHERE fans.user_id = #{userId} AND follow.user_id = #{userId} " +
            "LIMIT #{offset}, 10")          // 分页参数
    List<Fans> selectFansAndFollowerByUserId(
            Integer userId,
            Integer offset    // 新增分页起始位置参数
    );

}
