package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moji.po.Comments;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {
}
