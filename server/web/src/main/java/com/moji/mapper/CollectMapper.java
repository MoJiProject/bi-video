package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moji.po.Collects;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface CollectMapper extends BaseMapper<Collects> {


    List<Collects> getCollectByName(String collectName, Integer homeUserId, Integer sort, int offset);

    List<Collects> getCollectByName2(String collectName, Integer homeUserId, Integer sort, String keyWord, int offset);

    List<Collects> getCollectByName3(Integer homeUserId, Integer sort, String keyWord, int offset);


    long getCollectByNameCount(String collectName, Integer homeUserId, Integer sort, int i);

    long getCollectByNameCount2(String collectName, Integer homeUserId, Integer sort, String keyWord, int i);

    long getCollectByNameCount3(Integer homeUserId, Integer sort, String keyWord, int i);

    List<Collects> selectWaitWatch(Integer userId, int offset, Integer sort, Integer sort2, String keyWord, LocalDateTime startTime, LocalDateTime endTime);
}
