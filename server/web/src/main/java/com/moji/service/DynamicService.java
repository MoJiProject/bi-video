package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.AddDynamic;
import com.moji.dto.HomeDynamicDto;
import com.moji.po.Dynamic;
import com.moji.dto.DynamicDto;
import com.moji.po.UserInfo2;

import java.util.List;

public interface DynamicService extends IService<Dynamic> {
    List<DynamicDto> getFollowDynamic(Integer userID);

    Boolean cleanFollowDynamicAllNumber(Integer userId);

    int updateWaitWatch(Integer userId, Integer videoId);

    Page<Dynamic> dynamicImg(Integer homeUserId, Integer type, Integer pageNum);

    Page<HomeDynamicDto> homeDynamic(Integer homeUserId, Integer userId, Integer pageNum, Integer type, String ketWord);

    Boolean putUpStatus(Integer dynamicId);

    Boolean likeDynamic(Integer userId, Integer dynamicId);

    Boolean deleteDynamic(Integer dynamicId);

    boolean addDynamic(AddDynamic addDynamic);

    void addDynamicByFansMessage(Dynamic dynamic);

    void cleanDynamicMessage(Integer userId, Integer dynamicUserId);

    List<UserInfo2> usersDynamic(Integer userId, Integer pageNum);

    List<HomeDynamicDto> getDynamic(Integer dynamicUserId, Integer userId, Integer pageNum, Integer type);

    Boolean allDynamicMessage(Integer userId);

    Boolean selectDynamicByIdLike(Integer dynamicId, Integer userId);

    HomeDynamicDto selectDynamicById(Integer dynamicId);

    List<UserInfo2> likeDynamicUser(Integer userId, Integer pageNum, Integer dynamicId);
}
