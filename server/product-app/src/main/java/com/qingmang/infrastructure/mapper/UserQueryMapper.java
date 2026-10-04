package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.LoginRow;
import com.qingmang.interfaces.vo.UserProfileVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户相关的跨表查询。 */
@Mapper
public interface UserQueryMapper {

    /** 一次拿全用户资料 + 计数 + 当前登录用户的关注状态。 */
    UserProfileVO selectProfile(@Param("userId") Long userId, @Param("viewerId") Long viewerId);

    /** 按账号查，只取登录必需的列。 */
    LoginRow selectForLogin(@Param("username") String username);
}