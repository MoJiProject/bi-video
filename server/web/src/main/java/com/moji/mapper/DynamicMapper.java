package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.po.Dynamic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DynamicMapper extends BaseMapper<Dynamic> {


    List<Dynamic> homeDynamic(Integer homeUserId,Integer offset);


    List<Dynamic> getAllDynamic(Integer userId, Integer offset);

    List<Dynamic> getAllDynamic2(Integer userId, Integer offset);

    List<Dynamic> getAllDynamic3(Integer userId, Integer offset);
}
