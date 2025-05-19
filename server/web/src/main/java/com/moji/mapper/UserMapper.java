package com.moji.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moji.po.Users;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<Users> {

}
