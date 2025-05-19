package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moji.dto.SelectVideoDto;
import com.moji.po.Videos;
import com.moji.vo.SelectVideoByIdVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideosMapper extends BaseMapper<Videos> {
}
