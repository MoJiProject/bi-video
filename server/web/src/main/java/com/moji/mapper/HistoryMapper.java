package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moji.po.History;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HistoryMapper extends BaseMapper<History> {
    List<History> selectHistoryList(Integer userId, int offset, Integer sort, String keyWord, LocalDateTime startTime, LocalDateTime endTime);
}
