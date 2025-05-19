package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.AddFollowDto;
import com.moji.dto.EitDto;
import com.moji.po.UserInfo;
import com.moji.po.UserInfo2;
import com.moji.po.Users;

import java.util.List;


public interface UserService extends IService<Users> {


    Users login(Users users);

    String sign(Users users);

    String putPassword(Users users);

    Boolean addFollow(AddFollowDto addFollowDto);

    Boolean daleteFollow(AddFollowDto addFollowDto);

    Users updateUserInfo(Users users);


    EitDto getEit(Integer userId);

    Integer checkFollow(Integer viewUserId, Integer userId);

    UserInfo2 selectUserInfoById(Integer eitUserId, Integer userId);

    void putMessageSettings(UserInfo2 users);

    List<UserInfo2> selectFollowAndFans(Integer userId, Integer pageNum);

    Boolean putUserInfo(UserInfo userInfo2);

    UserInfo2 selectUserInfoById(Integer id);

    Boolean putSettings(UserInfo2 userInfo2);

    Page<UserInfo2> fansOrFollow(Integer homeUserId, Integer userId, Integer type, Integer pageNum, String keyWord);

    void putNewFans(Integer userId);

    Boolean uploadAvatar(Integer userId, String avatar);

    Boolean uploadBackground(Integer userId, String background);
}
