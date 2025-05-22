package com.moji.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.EitDto;
import com.moji.dto.LimiterLoginDto;
import com.moji.mapper.FansMapper;
import com.moji.mapper.UserMapper;
import com.moji.po.*;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FansMapper fansMapper;


    /**
     * 登录
     * @param limiterLoginDto
     * @return
     */
    @PostMapping("/login")
    public R<UserInfo> userLogin(@RequestBody LimiterLoginDto limiterLoginDto) {

        //增加限制登录的次数
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(limiterLoginDto.getUserIp()!=null)
        {
            Boolean loginFlag = limiterServer.isAllowedToLogin(limiterLoginDto.getUserIp());
            if(!loginFlag)
                return R.error("操作频繁");
        }

        Users user = userService.login(limiterLoginDto.getUser());
        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        limiterServer.setAutoLogin(limiterLoginDto.getUserIp(),tokenInfo.tokenValue);
        user.setToken(tokenInfo.tokenValue);

        Users users1 = userService.updateUserInfo(user);
        UserInfo userInfo=new UserInfo();
        BeanUtils.copyProperties(users1,userInfo);

        return R.success(userInfo);
    }

    /**
     * 注册
     * @param limiterLoginDto
     * @return
     */
    @PostMapping("/sign")
    public R<String> userSIgn(@RequestBody LimiterLoginDto limiterLoginDto){

        //增加限制登录的次数
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(limiterLoginDto.getUserIp()!=null)
        {
            Boolean loginFlag = limiterServer.isAllowedToSign(limiterLoginDto.getUserIp());
            if(!loginFlag)
                return R.error("操作频繁");
        }

        return R.success(userService.sign(limiterLoginDto.getUser()));

    }

    /**
     * 忘记密码
     * @param limiterLoginDto
     * @return
     */
    @PutMapping("/forget")
    public R<String> userPutPassword(@RequestBody LimiterLoginDto limiterLoginDto){

        //增加限制登录的次数
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(limiterLoginDto.getUserIp()!=null)
        {
            Boolean loginFlag = limiterServer.isAllowedToPut(limiterLoginDto.getUserIp());
            if(!loginFlag)
                return R.error("操作频繁");
        }

        return R.success(userService.putPassword(limiterLoginDto.getUser()));
    }


    /**
     * 自动登录
     * @param userIp
     * @return
     */
    @GetMapping("/checkLogin/{userIp}")
    public R<UserInfo> onMounted(@PathVariable String userIp){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        String token = limiterServer.isAutoLogin(userIp);

        if(StpUtil.getLoginIdByToken(token)==null)
            return R.error(null);

        LambdaQueryWrapper<Users> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Users::getToken,token);
        Users users = userMapper.selectOne(wrapper);
        if(users==null)
            return R.error("未知错误");

        Users users1 = userService.updateUserInfo(users);

        UserInfo userInfo=new UserInfo();
        BeanUtils.copyProperties(users1,userInfo);

        return R.success(userInfo);

    }

    /**
     * 检查是否登录
     * @param userIp
     * @return
     */
    @GetMapping("/checkLoginFlag/{userIp}")
    public R<UserInfo> checkLogin(@PathVariable String userIp){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        String token = limiterServer.isAutoLogin(userIp);

        if(token==null)
            return R.error(null);

        LambdaQueryWrapper<Users> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Users::getToken,token);
        Users users = userMapper.selectOne(wrapper);
        if(users==null)
            return R.error("未知错误");

        UserInfo userInfo=new UserInfo();
        BeanUtils.copyProperties(users,userInfo);

        return R.success(userInfo);
    }

    /**
     * 获得用户信息
     * @param id
     * @return
     */
    @GetMapping("/userInfo/{id}")
    public R<UserInfo2> userInfo(@PathVariable Integer id){

        return R.success(userService.selectUserInfoById(id));

    }


    /**
     * 修改用户信息
     * @param userInfo2
     * @param token
     * @return
     */
    @PutMapping("/putUserInfo")
    public R<String> putUserInfo(@RequestBody UserInfo userInfo2,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userInfo2.getId(),token))
            return R.error("非法操作");

        Boolean b = userService.putUserInfo(userInfo2);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");
    }



    /**
     * 退出登录
     * @param limiterLoginDto
     * @return
     */
    @PostMapping("/signOut")
    public R<String> signOut(@RequestBody LimiterLoginDto limiterLoginDto,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(limiterLoginDto.getUser().getId(),token))
            R.error("非法操作");

        limiterServer.delAutoLogin(limiterLoginDto.getUserIp());

        StpUtil.logout(limiterLoginDto.getUser().getId());

        return R.success("登出成功");
    }


    /**
     * 获得新粉丝数量
     * @param userId
     * @return
     */
    @GetMapping("getNewFansNumber/{userId}")
    public R<Integer> getNewFans(@PathVariable Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("非法操作");
       LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
       fansLambdaQueryWrapper.eq(Fans::getUserId,userId);

        List<Fans> fans = fansMapper.selectList(fansLambdaQueryWrapper);
        Integer newFansNumber=0;
        for (Fans fan : fans) {

            if(fan.getNewFansId()!=null)
                newFansNumber++;

        }
        return R.success(newFansNumber);
    }


    /**
     * 获取@的列表
     * @return
     */
    @GetMapping("/getEit/{userId}")
    public R<EitDto> getEitList(@PathVariable Integer userId,@RequestHeader("Authorization") String token){


        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");


        EitDto eitList = userService.getEit(userId);
        return R.success(eitList);

    }


    /**
     * 查询是否关注该用户
     * @param userId
     * @param token
     * @return
     */
    @GetMapping("/selectFollow")
    public R<Integer> checkFollow(@RequestParam Integer userId,@RequestParam Integer viewUserId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token)||viewUserId==null)
            return R.error("查询失败");

        return R.success(userService.checkFollow(viewUserId,userId));


    }


    /**
     * 获取eit用户信息
     * @param eitUserId
     * @param userId
     * @return
     */
    @GetMapping("/selectUserInfoById")
    public R<UserInfo2> selectUserInfoById(@RequestParam Integer eitUserId,@RequestParam Integer userId){

        UserInfo2 userInfo2 = userService.selectUserInfoById(eitUserId, userId);
        if(userInfo2!=null)
         return R.success(userInfo2);
        return R.error("查询失败");

    }


    /**
     * 修改消息提醒设置
     * @param users
     * @param token
     * @return
     */
    @PutMapping("/putMessageSettings")
    public R<String> putMessageSettings(@RequestBody UserInfo2 users,@RequestHeader("Authorization") String token){

            LoginLimiterServer limiterServer=new LoginLimiterServer();
            if(!limiterServer.checkUser(users.getId(),token))
                return R.error("修改失败");

            userService.putMessageSettings(users);
            return R.success("修改成功");

    }


    /**
     * 查询互相关注列表
     * @param userId
     * @param token
     * @return
     */
    @GetMapping("/selectFollowAndFans")
    public R<List<UserInfo2>> selectFollowAndFans(@RequestParam Integer userId,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(userService.selectFollowAndFans(userId,pageNum));
    }


    /**
     * 修改主页设置
     * @param userInfo2
     * @return
     */
    @PutMapping("/putSettings")
    public R<String> putSettings(@RequestBody UserInfo2 userInfo2,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userInfo2.getId(),token))
            return R.error("修改失败");

        Boolean b = userService.putSettings(userInfo2);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");

    }


    /**
     * 获取主页粉丝或关注列表
     * @param homeUserId
     * @param userId
     * @param type
     * @param pageNum
     * @return
     */
    @GetMapping("/fansOrFollow")
    public R<Page<UserInfo2>> fansOrFollow(@RequestParam Integer homeUserId, @RequestParam Integer userId, @RequestParam Integer type, @RequestParam Integer pageNum, @RequestParam String keyWord,@RequestHeader("Authorization") String token) {

        return R.success(userService.fansOrFollow(homeUserId, userId, type, pageNum, keyWord));
    }


    /**
     * 清除新粉丝提醒
     * @param userId
     */
    @PutMapping("/putNewFans")
    public void putNewFans(@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return;

        userService.putNewFans(userId);
    }



    /**
     * 上传头像
     * @param avatar
     * @param userId
     * @return
     */
    @PostMapping("/uploadAvatar")
    public R<String> uploadAvatar(@RequestBody String avatar,@RequestParam Integer userId,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("头像更新失败");

        Boolean b = userService.uploadAvatar(userId, avatar);
        if(b)
            return R.success("头像更新成功");
        return R.error("头像更新失败");
    }


    /**
     * 上传头像
     * @param background
     * @param userId
     * @return
     */
    @PostMapping("/uploadBackground")
    public R<String> uploadBackground(@RequestBody String background,@RequestParam Integer userId,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("背景更新失败");

        Boolean b = userService.uploadBackground(userId, background);
        if(b)
            return R.success("背景更新成功");
        return R.error("背景更新失败");
    }


    /**
     * 获取所有用户
     * @param userId
     * @param pageNum
     * @param type -1 0 1
     * @param keyword
     * @param token
     * @return
     */
    @GetMapping("/getUsers")
    public R<Page<UserInfo2>> getUsers(@RequestParam Integer userId,@RequestParam Integer type,@RequestParam Integer pageNum,@RequestParam(required = false) String keyword,@RequestHeader("Authorization") String token){

        Users users = userMapper.selectById(userId);
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(users==null||!users.getUserName().equals("MoJi")||!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(userService.getUsers(pageNum,keyword,type));
    }


    /**
     * 设置用户管理员
     * @param userId
     * @param adminId
     * @param token
     * @return
     */
    @PutMapping("/putAdmin")
    public R<String> putAdmin(@RequestParam Integer userId,@RequestParam Integer adminId,@RequestHeader("Authorization") String token) {

        Users users = userMapper.selectById(userId);
        LoginLimiterServer limiterServer = new LoginLimiterServer();
        if (users == null || !users.getUserName().equals("MoJi") || !limiterServer.checkUser(userId, token))
            return R.error("查询失败");

        boolean b = userService.putAdmin(adminId);
        if (b)
            return R.success("修改成功");
        return R.error("修改失败");
    }

    }
