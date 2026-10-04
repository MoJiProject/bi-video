package com.qingmang.infrastructure.mapper;

import com.qingmang.interfaces.vo.PostVO;
import com.qingmang.interfaces.vo.UserBriefVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SocialQueryMapper {

    /**
     * 动态流。
     *
     * <p>三种场景一次搞定：公开流（targetUserId 为空）、某人的动态、看自己仅自己可见的。</p>
     */
    List<PostVO> selectPosts(@Param("viewerId") Long viewerId,
                             @Param("targetUserId") Long targetUserId,
                             @Param("cursorId") Long cursorId,
                             @Param("limit") int limit);

    /** 关注列表 / 粉丝列表共用同一张表，只是方向相反。 */
    List<UserBriefVO> selectFollows(@Param("viewerId") Long viewerId,
                                    @Param("otherId") Long otherId,
                                    @Param("cursorId") Long cursorId,
                                    @Param("limit") int limit);
}