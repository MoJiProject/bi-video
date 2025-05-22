package com.moji.service.impl;
import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.MessageConstant;
import com.moji.dto.*;
import com.moji.exception.AccountExistException;
import com.moji.exception.AccountNotFoundException;
import com.moji.exception.PasswordErrorException;
import com.moji.mapper.*;
import com.moji.po.*;
import com.moji.service.*;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, Users> implements UserService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private DynamicMapper dynamicMapper;

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private CollectService collectService;

    @Autowired
    private VideosService videosService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CacheService cacheService;

    public Users login(Users users) {

        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getUserName, users.getUserName());

        Users users2=userMapper.selectOne(wrapper);
        if (users2==null) {

            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }


        if (!BCrypt.checkpw(users.getPassword(),users2.getPassword())) {

            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        collectService.getAllCollect(users2.getId());

        return users2;
    }

    @Override
    public String sign(Users users) {

        LambdaQueryWrapper<Users> wrapper=new LambdaQueryWrapper<>();
         wrapper.eq(Users::getUserName,users.getUserName());

         if (userMapper.selectOne(wrapper)!=null)
             throw new AccountExistException(MessageConstant.ACCOUNT_EXIST);

         users.setCreateTime(LocalDateTime.now());
         users.setAvatarAddress("/默认头像.gif");

         //密码加密
        users.setPassword(BCrypt.hashpw(users.getPassword(), BCrypt.gensalt()));

        if ( userMapper.insert(users)>0)
        return "注册成功";
         else
             return "注册失败";
    }

    @Override
    @Transactional
    public String putPassword(Users users) {


        LambdaQueryWrapper<Users> wrapper=new LambdaQueryWrapper<>();

        wrapper.eq(Users::getUserName,users.getUserName());
        if (userMapper.selectOne(wrapper)==null)
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);

        wrapper.eq(Users::getPhone,users.getPhone());

        if (userMapper.selectOne(wrapper)==null)

            throw new PasswordErrorException(MessageConstant.PHONE_ERROR);

        wrapper.eq(Users::getQuestion,users.getQuestion());

        if (userMapper.selectOne(wrapper)==null)
             throw new PasswordErrorException((MessageConstant.QUESTION_ERROR));

          Users users1=userMapper.selectOne(wrapper);

          users1.setPassword(BCrypt.hashpw(users.getPassword(),BCrypt.gensalt()));

           if (userMapper.updateById(users1)>0)
               return "修改成功";
           else return "修改失败";
    }

    @Override
    @Transactional
    public Boolean addFollow(AddFollowDto addFollowDto) {

        LambdaQueryWrapper<Follow> followLambdaQueryWrapper2=new LambdaQueryWrapper<>();
        followLambdaQueryWrapper2.eq(Follow::getUserId,addFollowDto.getFansId())
                .eq(Follow::getFollowId,addFollowDto.getFollowId());
        if(followMapper.selectOne(followLambdaQueryWrapper2)!=null)
            return true;

        Fans fans = new Fans();
        Follow follow = new Follow();

        fans.setFansId(addFollowDto.getFansId());
        fans.setUserId(addFollowDto.getFollowId());
        fans.setNewFansId(addFollowDto.getFansId());

        follow.setFollowId(addFollowDto.getFollowId());
        follow.setUserId(addFollowDto.getFansId());

        Users fansUser = userMapper.selectById(addFollowDto.getFansId());
        Users followUser = userMapper.selectById(addFollowDto.getFollowId());

        fans.setFansUserName(fansUser.getUserName());
        follow.setFollowUserName(followUser.getUserName());

        fansUser.setFollowNumber(fansUser.getFollowNumber() + 1);
        followUser.setFansNumber(followUser.getFansNumber() + 1);

        LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
        videosLambdaQueryWrapper.eq(Videos::getUserId, addFollowDto.getFollowId())
                .eq(Videos::getStatus,1);


        List<Videos> videos = videosMapper.selectList(videosLambdaQueryWrapper);
        int dynamicNumber=0;
        if(!videos.isEmpty()){

            for (Videos video : videos) {

                //查询是否存在该记录
                LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
                dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId, addFollowDto.getFollowId())
                        .eq(Dynamic::getFansId, addFollowDto.getFansId())
                        .eq(Dynamic::getVideoId,video.getId())
                        .isNull(Dynamic::getCommentId);
                Dynamic dynamic1 = dynamicMapper.selectOne(dynamicLambdaQueryWrapper);
                //如过存在
                if(dynamic1!=null){

                    dynamic1.setFansFlag(1);
                    if(dynamic1.getWatchDynamicFlag()==0)
                     dynamicNumber++;
                    dynamicMapper.updateById(dynamic1);
                }
                else
                {
                    Dynamic dynamic=Dynamic.builder()
                            .followId(addFollowDto.getFollowId())
                            .videoId(video.getId())
                            .fansId(addFollowDto.getFansId())
                            .watchDynamicFlag(0)
                            .fansFlag(1).build();
                    dynamicMapper.insert(dynamic);
                    dynamicNumber++;

                }
            }
        }

        fansUser.setDynamicNumber(fansUser.getDynamicNumber()+dynamicNumber);
        userMapper.updateById(fansUser);
        userMapper.updateById(followUser);

        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Fans::getUserId, addFollowDto.getFollowId())
                .eq(Fans::getFansId, addFollowDto.getFansId());
        followLambdaQueryWrapper.eq(Follow::getUserId, addFollowDto.getFansId())
                .eq(Follow::getFollowId, addFollowDto.getFollowId());

        Fans fans1 = fansMapper.selectOne(fansLambdaQueryWrapper);
        Follow follow1 = followMapper.selectOne(followLambdaQueryWrapper);

        if(fans1==null&&follow1==null) {
            int insert = fansMapper.insert(fans);
            int insert1 = followMapper.insert(follow);

            return insert1 > 0 && insert > 0;
        }

        else return true;
    }

    @Override
    @Transactional
    public Boolean daleteFollow(AddFollowDto addFollowDto) {

        LambdaQueryWrapper<Follow> followLambdaQueryWrapper2=new LambdaQueryWrapper<>();
        followLambdaQueryWrapper2.eq(Follow::getUserId,addFollowDto.getFansId())
                .eq(Follow::getFollowId,addFollowDto.getFollowId());
        if(followMapper.selectOne(followLambdaQueryWrapper2)==null)
            return true;

        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();

        fansLambdaQueryWrapper.eq(Fans::getFansId, addFollowDto.getFansId())
                .eq(Fans::getUserId, addFollowDto.getFollowId());

        followLambdaQueryWrapper.eq(Follow::getFollowId, addFollowDto.getFollowId())
                .eq(Follow::getUserId, addFollowDto.getFansId());


        Users fansUser = userMapper.selectById(addFollowDto.getFansId());
        Users followUser = userMapper.selectById(addFollowDto.getFollowId());

        fansUser.setFollowNumber(fansUser.getFollowNumber()-1);
        followUser.setFansNumber(followUser.getFansNumber()-1);

        LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
        videosLambdaQueryWrapper.eq(Videos::getUserId, addFollowDto.getFollowId());

        List<Videos> videos = videosMapper.selectList(videosLambdaQueryWrapper);
        int dynamicNumber=0;
        if(!videos.isEmpty()){

            for (Videos video : videos) {

                //查询是否存在该记录
                LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
                dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId, addFollowDto.getFollowId())
                        .eq(Dynamic::getFansId, addFollowDto.getFansId())
                        .eq(Dynamic::getVideoId,video.getId())
                        .isNull(Dynamic::getCommentId);
                Dynamic dynamic1 = dynamicMapper.selectOne(dynamicLambdaQueryWrapper);
                //如过存在
                if(dynamic1!=null){

                    dynamic1.setFansFlag(0);
                    if(dynamic1.getWatchDynamicFlag()==0)
                        dynamicNumber++;
                    dynamicMapper.updateById(dynamic1);

                }

            }

        }
        fansUser.setDynamicNumber(fansUser.getDynamicNumber()==0?0:fansUser.getDynamicNumber()-dynamicNumber);
        userMapper.updateById(fansUser);
        userMapper.updateById(followUser);

        int delete = followMapper.delete(followLambdaQueryWrapper);
        int delete1 = fansMapper.delete(fansLambdaQueryWrapper);

        return delete1 > 0 && delete > 0;
    }

    @Override
    @Transactional
    public Users updateUserInfo(Users userInfo) {

        Users user = userMapper.selectById(userInfo.getId());

        Long days=0L;
        //计算上回登录的时间到这次登录的时间是否是同一天
        if(user.getLoginDateTime()!=null) {
            LocalDateTime loginDateTime =user.getLoginDateTime();
            LocalDateTime userLogin  =LocalDateTime.of(loginDateTime.getYear(),loginDateTime.getMonth(),loginDateTime.getDayOfMonth(),0,0,0);

            days = ChronoUnit.DAYS.between(userLogin, LocalDateTime.now());
        }
        user.setLoginDateTime(LocalDateTime.now());
        //实现每天登录加硬币和经验
        if(days>0)
        {
            if(user.getGrade()<6)
             user.setExp(user.getExp()+1);
            user.setCoinNumber(user.getCoinNumber()+2);
        }

        //判断是否为6级 检查经验值是否超额
        if(user.getExp()>99 && user.getGrade()<6) {
            user.setExp(user.getExp() - 100);
            user.setGrade(user.getGrade() + 1);
            if (user.getGrade() == 6)
                user.setExp(100);
        }

        user.setToken(userInfo.getToken());
            userMapper.updateById(user);

        return user;

    }

    @Override
    public EitDto getEit(Integer userId) {

        List<Eit> friendList=new ArrayList<>();

        //获取关注列表
        if(userId!=null)
        {

            LambdaQueryWrapper<Follow> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Follow::getUserId,userId);
            List<Follow> follows = followMapper.selectList(lambdaQueryWrapper);
            if(!follows.isEmpty()){

                for (Follow follow : follows) {

                    Users users = userMapper.selectById(follow.getFollowId());
                    Eit eit=new Eit();
                    BeanUtils.copyProperties(users,eit);
                    friendList.add(eit);
                }
            }
        }

        //获取所有用户列表
        List<Eit> otherList=new ArrayList<>();

        List<Users> usersList = userMapper.selectList(null);
        for (Users users : usersList) {
            boolean otherFlag=true;
            if(!friendList.isEmpty()){
                for (Eit eit : friendList) {
                    if(users.getUserName().equals(eit.getUserName()))
                        otherFlag=false;
                }
            }
            if(otherFlag) {
                Eit eit = new Eit();
                BeanUtils.copyProperties(users, eit);
                otherList.add(eit);
            }
        }

        EitDto eitDto=new EitDto();
        eitDto.setFriendList(friendList);
        eitDto.setOtherList(otherList);

        return eitDto;
    }

    @Override
    public Integer checkFollow(Integer viewUserId, Integer userId) {

        LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getUserId,userId)
                .eq(Follow::getFollowId,viewUserId);
        Follow follow = followMapper.selectOne(followLambdaQueryWrapper);
        return follow!=null ? 1:0;
    }

    @Override
    public UserInfo2 selectUserInfoById(Integer eitUserId, Integer userId) {

        UserInfo2 userInfo2=new UserInfo2();
        Users users = userMapper.selectById(eitUserId);
        BeanUtils.copyProperties(users,userInfo2);

        LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getFollowId,eitUserId)
                .eq(Follow::getUserId,userId);
        Follow follow = followMapper.selectOne(followLambdaQueryWrapper);
        if(follow!=null)
            userInfo2.setIsFansFlag(1);
        else userInfo2.setIsFansFlag(0);

        return userInfo2;
    }

    @Override
    @Transactional
    public void putMessageSettings(UserInfo2 users) {

        Users users1 = userMapper.selectById(users.getId());
        users1.setLikeMessageWarn(users.getLikeMessageWarn());
        users1.setMessageWarn(users.getMessageWarn());
        users1.setAtMessageWarn(users.getAtMessageWarn());
        users1.setReplyMessageWarn(users.getReplyMessageWarn());
        userMapper.updateById(users1);
    }

    @Override
    public List<UserInfo2> selectFollowAndFans(Integer userId, Integer pageNum) {

        List<UserInfo2> userInfo2List=new ArrayList<>();
        List<Fans> fans = fansMapper.selectFansAndFollowerByUserId(userId,(pageNum-1)*10);
        for (Fans fan : fans) {
            Users users = userMapper.selectById(fan.getFansId());
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);
            userInfo2List.add(userInfo2);
        }
        return userInfo2List;
    }

    @Override
    @Transactional
    public Boolean putUserInfo(UserInfo userInfo2) {

        Users users = userMapper.selectById(userInfo2.getId());

        if(userInfo2.getUserName()==null)
            return false;

        if(userInfo2.getControl()==1){

            if(!users.getUserName().equals(userInfo2.getUserName())){
                LambdaQueryWrapper<Users> usersLambdaQueryWrapper=new LambdaQueryWrapper<>();
                usersLambdaQueryWrapper.eq(Users::getUserName,userInfo2.getUserName());
                if(userMapper.selectOne(usersLambdaQueryWrapper)!=null){
                    throw new AccountExistException("昵称已存在");
                }
            }

           if(!userInfo2.getPassword1().isEmpty()){
               if(BCrypt.checkpw(userInfo2.getPassword1(),users.getPassword())){
                   users.setPassword(BCrypt.hashpw(userInfo2.getNewPassword(),BCrypt.gensalt()));
               }
               else{
                   throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
               }
           }
        }

            users.setHistoryFlag(userInfo2.getHistoryFlag());
            users.setBirthday(userInfo2.getBirthday());
            users.setIntroduce(userInfo2.getIntroduce());
            users.setPhone(userInfo2.getPhone());
            users.setGender(userInfo2.getGender());
            userMapper.updateById(users);

        return true;
    }

    @Override
    public UserInfo2 selectUserInfoById(Integer id) {

        Users users = userMapper.selectById(id);
        UserInfo2 userInfo2=new UserInfo2();
        BeanUtils.copyProperties(users,userInfo2);
        VideosDto videosDto = videosService.selectDtoData(id);
        userInfo2.setPlayNumber(videosDto.getPlayNumber());
        return userInfo2;
    }

    @Override
    @Transactional
    public Boolean putSettings(UserInfo2 userInfo2) {

        Users users = userMapper.selectById(userInfo2.getId());

        if(checkValue(userInfo2.getPublicBirthday())&&
                checkValue(userInfo2.getPublicCoin())&&
                checkValue(userInfo2.getPublicAnime())&&
                checkValue(userInfo2.getPublicLove())&&
                checkValue(userInfo2.getPublicFansList())&&
                checkValue(userInfo2.getPublicCollect())&&
                checkValue(userInfo2.getPublicFollowList()))
            return false;

        users.setPublicBirthday(userInfo2.getPublicBirthday());
        users.setPublicCoin(userInfo2.getPublicCoin());
        users.setPublicCollect(userInfo2.getPublicCollect());
        users.setPublicAnime(userInfo2.getPublicAnime());
        users.setPublicLove(userInfo2.getPublicLove());
        users.setPublicFansList(userInfo2.getPublicFansList());
        users.setPublicFollowList(userInfo2.getPublicFollowList());
        int i = userMapper.updateById(users);
        return i>0;
    }

    @Override
    public Page<UserInfo2> fansOrFollow(Integer homeUserId, Integer userId, Integer type, Integer pageNum, String keyWord) {

        if(userId==null||homeUserId==null)
            return null;

        Page<UserInfo2> userInfo2Page=new Page<>();
        //关注
        if(type==8){
            Page<Follow> page=new Page<>(pageNum,24);
            LambdaQueryWrapper<Follow>  followLambdaQueryWrapper=new LambdaQueryWrapper<>();
            if(keyWord==null||keyWord.isEmpty())
                followLambdaQueryWrapper.eq(Follow::getUserId,homeUserId);
            else
                followLambdaQueryWrapper.like(Follow::getFollowUserName,keyWord);

            Page<Follow> page1 = followMapper.selectPage(page, followLambdaQueryWrapper);
            List<Follow> records = page1.getRecords();

            List<UserInfo2> userInfo2List=new ArrayList<>();
            for (Follow record : records) {
                Users users = userMapper.selectById(record.getFollowId());
                UserInfo2 userInfo2=new UserInfo2();
                BeanUtils.copyProperties(users,userInfo2);

                LambdaQueryWrapper<Follow> followLambdaQueryWrapper1=new LambdaQueryWrapper<>();
                followLambdaQueryWrapper1.eq(Follow::getUserId,users.getId())
                        .eq(Follow::getFollowId,userId);
                Follow follow = followMapper.selectOne(followLambdaQueryWrapper1);
                LambdaQueryWrapper<Follow> followLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                followLambdaQueryWrapper2.eq(Follow::getUserId,userId)
                        .eq(Follow::getFollowId,users.getId());
                Follow follow2 = followMapper.selectOne(followLambdaQueryWrapper2);
                if(follow!=null&&follow2!=null) {
                    userInfo2.setIsFollowFlag(1);
                    userInfo2.setIsFansFlag(1);
                }
                else if(follow!=null||follow2!=null) {
                   if(follow!=null){
                       userInfo2.setIsFollowFlag(0);
                       userInfo2.setIsFansFlag(1);
                   }else {
                       userInfo2.setIsFollowFlag(1);
                       userInfo2.setIsFansFlag(0);
                   }
                }
                else {
                    userInfo2.setIsFollowFlag(0);
                    userInfo2.setIsFansFlag(0);
                }
                userInfo2List.add(userInfo2);
            }
            userInfo2Page.setRecords(userInfo2List);
            userInfo2Page.setTotal(page1.getTotal());
        }
        //粉丝
        else if (type == 9) {
            Page<Fans> page=new Page<>(pageNum,24);
            LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
            if(keyWord==null||keyWord.isEmpty())
                fansLambdaQueryWrapper.eq(Fans::getUserId,homeUserId);
            else
                fansLambdaQueryWrapper.like(Fans::getFansUserName,keyWord);

            Page<Fans> page1 = fansMapper.selectPage(page,fansLambdaQueryWrapper);
            List<Fans> records = page1.getRecords();

            List<UserInfo2> userInfo2List=new ArrayList<>();
            for (Fans record : records) {
                Users users = userMapper.selectById(record.getFansId());
                UserInfo2 userInfo2=new UserInfo2();
                BeanUtils.copyProperties(users,userInfo2);

                LambdaQueryWrapper<Fans> fansLambdaQueryWrapper1=new LambdaQueryWrapper<>();
                fansLambdaQueryWrapper1.eq(Fans::getFansId,users.getId())
                        .eq(Fans::getUserId,userId);
                Fans fans = fansMapper.selectOne(fansLambdaQueryWrapper1);
                LambdaQueryWrapper<Fans> fansLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                fansLambdaQueryWrapper2.eq(Fans::getFansId,userId)
                        .eq(Fans::getUserId,users.getId());
                Fans fans2 = fansMapper.selectOne(fansLambdaQueryWrapper2);
                if(fans!=null&&fans2!=null) {
                    userInfo2.setIsFollowFlag(1);
                    userInfo2.setIsFansFlag(1);
                    userInfo2.setNewFans(fans.getNewFansId());
                }
                else if(fans!=null||fans2!=null) {
                    if(fans!=null){
                        userInfo2.setIsFollowFlag(0);
                        userInfo2.setIsFansFlag(1);
                        userInfo2.setNewFans(fans.getNewFansId());
                    }else {
                        userInfo2.setIsFollowFlag(1);
                        userInfo2.setIsFansFlag(0);
                    }
                }
                else {
                    userInfo2.setIsFollowFlag(0);
                    userInfo2.setIsFansFlag(0);
                }
                userInfo2List.add(userInfo2);
            }
            userInfo2Page.setRecords(userInfo2List);
            userInfo2Page.setTotal(page1.getTotal());
        }
        return userInfo2Page;
    }

    @Override
    public void putNewFans(Integer userId) {

        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Fans::getUserId,userId);
        List<Fans> fans = fansMapper.selectList(fansLambdaQueryWrapper);
        for (Fans fan : fans) {
            fan.setNewFansId(-1);
            fansMapper.updateById(fan);
        }
    }

    @Override
    @Transactional
    public Boolean uploadAvatar(Integer userId, String avatar) {

        Users users = userMapper.selectById(userId);
        if(users==null)
            return false;

        try {
            String base64=null;
            if(avatar.contains(","))
                base64=avatar.split(",")[1];
            else
                return false;

            //删除之前的头像
            if(users.getAvatarAddress()!=null&&!users.getAvatarAddress().isEmpty()&&!users.getAvatarAddress().equals("/默认头像.gif")){
                String avatarAddress=users.getAvatarAddress();
                int lastIndexOf = avatarAddress.lastIndexOf("/");
                String avatarFile=(lastIndexOf!=-1)? avatarAddress.substring(lastIndexOf+1):avatarAddress;
                Path avatarPath= Paths.get(FilePathEnum.UPLOAD_AVATAR.getPath()+avatarFile);
                try {
                    Files.delete(avatarPath);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    return false;
                }
            }

            //上传文件
            UUID avatarName = UUID.randomUUID();
            String imageFormat = commentService.getImageFormat(avatar);
            File dir=new File(FilePathEnum.UPLOAD_AVATAR.getPath());
            byte[] decode = Base64.getDecoder().decode(base64);
            File dest=new File(dir,avatarName+imageFormat);
            try(FileOutputStream fos=new FileOutputStream(dest)){
                fos.write(decode);
            }
            users.setAvatarAddress("/upload/avatar/"+avatarName+imageFormat);
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }

        cacheService.deleteMessageByUserId(userId,null);
        cacheService.deleteDialogueByUserId(userId);
        cacheService.deleteCommentCacheByVideoId(null,null,userId);
        cacheService.deleteReplyCommentCacheByCommentId(null,null,null,userId);

        int i = userMapper.updateById(users);
        return i>0;
    }

    @Override
    @Transactional
    public Boolean uploadBackground(Integer userId, String background) {

        Users users = userMapper.selectById(userId);
        if(users==null)
            return false;

        try {
            String base64=null;
            if(background.contains(","))
                base64=background.split(",")[1];
            else
                return false;

            //删除之前的头像
            if(users.getBackgroundAddress()!=null&&!users.getBackgroundAddress().isEmpty()){
                String backgroundAddress=users.getBackgroundAddress();
                int lastIndexOf = backgroundAddress.lastIndexOf("/");
                String backgroundFile=(lastIndexOf!=-1)? backgroundAddress.substring(lastIndexOf+1):backgroundAddress;
                Path backgroundPath= Paths.get(FilePathEnum.UPLOAD_IMG_BACKGROUND.getPath()+backgroundFile);
                try {
                    Files.delete(backgroundPath);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                    return false;
                }
            }

            //上传文件
            UUID backgroundName = UUID.randomUUID();
            String imageFormat = commentService.getImageFormat(background);
            File dir=new File(FilePathEnum.UPLOAD_IMG_BACKGROUND.getPath());
            byte[] decode = Base64.getDecoder().decode(base64);
            File dest=new File(dir,backgroundName+imageFormat);
            try(FileOutputStream fos=new FileOutputStream(dest)){
                fos.write(decode);
            }
            users.setBackgroundAddress("/upload/background/"+backgroundName+imageFormat);
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }

        cacheService.deleteMessageByUserId(userId,null);
        cacheService.deleteDialogueByUserId(userId);
        cacheService.deleteCommentCacheByVideoId(null,null,userId);
        cacheService.deleteReplyCommentCacheByCommentId(null,null,null,userId);

        int i = userMapper.updateById(users);
        return i>0;
    }

    @Override
    public Page<UserInfo2> getUsers(Integer pageNum, String keyword, Integer type) {

        Page<Users> usersPage=new Page<>(pageNum,10);
        LambdaQueryWrapper<Users> usersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(StringUtil.notNullNorEmpty(keyword))
                usersLambdaQueryWrapper
                        .apply("LOWER({0}) LIKE CONCAT('%', LOWER(user_name), '%')",keyword)
                        .or()
                        .like(Users::getUserName,keyword);
        if(type!=-1)
            usersLambdaQueryWrapper.eq(Users::getAdminFlag,type);
        usersLambdaQueryWrapper.orderByDesc(Users::getCreateTime);
        Page<Users> usersPage1 = userMapper.selectPage(usersPage, usersLambdaQueryWrapper);
        List<Users> records = usersPage1.getRecords();
        List<UserInfo2> userInfo2List=new ArrayList<>();
        for (Users record : records) {
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(record,userInfo2);
            userInfo2List.add(userInfo2);
        }
        Page<UserInfo2> userInfo2Page=new Page<>();
        userInfo2Page.setRecords(userInfo2List);
        userInfo2Page.setTotal(usersPage.getTotal());
        return userInfo2Page;
    }

    @Override
    @Transactional
    public boolean putAdmin(Integer adminId) {

        Users users = userMapper.selectById(adminId);
        if(users==null)
            return false;
        users.setAdminFlag(users.getAdminFlag()==0?1:0);
        int i = userMapper.updateById(users);
        return i>0;
    }


    public boolean checkValue(Integer value){
        return value != 0 && value != 1;
    }


}
