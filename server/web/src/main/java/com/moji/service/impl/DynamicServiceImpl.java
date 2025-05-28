package com.moji.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.dto.AddDynamic;
import com.moji.dto.DynamicDto;
import com.moji.dto.HomeDynamicDto;
import com.moji.dto.SelectVideoDto;
import com.moji.mapper.*;
import com.moji.po.*;
import com.moji.service.CommentService;
import com.moji.service.DynamicService;
import com.moji.service.VideosService;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DynamicServiceImpl extends ServiceImpl<DynamicMapper, Dynamic> implements DynamicService {

    @Autowired
    private FollowMapper followMapper;
    @Autowired
    private VideosMapper videosMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DynamicMapper dynamicMapper;
    @Autowired
    private CollectMapper collectMapper;
    @Autowired
    private CollectClassifyMapper collectClassifyMapper;
    @Autowired
    private CommentsMapper commentsMapper;
    @Autowired
    private CommentControlsMapper commentControlsMapper;
    @Autowired
    private LikesMapper likesMapper;
    @Autowired
    private VideosService videosService;
    @Autowired
    private FansMapper fansMapper;
    @Autowired
    private CommentService commentService;
    @Autowired
    private AtMapper atMapper;


    @Override
    public List<DynamicDto> getFollowDynamic(Integer userID) {

        List<DynamicDto> dynamicDtoList =new ArrayList<>();

        Users fansUser = userMapper.selectById(userID);

        LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getUserId,userID);
        List<Follow> follows = followMapper.selectList(followLambdaQueryWrapper);
        int historyDynamicNumber=0;
        if(!follows.isEmpty()){
            //查询到是关注列表的所有人
            for (Follow follow : follows) {
                LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper1=new LambdaQueryWrapper<>();
                //查询粉id根关注id对应并且现在是粉丝状态的
                dynamicLambdaQueryWrapper1.eq(Dynamic::getFollowId,follow.getFollowId())
                        .eq(Dynamic::getFansId,userID)
                        .isNotNull(Dynamic::getVideoId)
                        .isNull(Dynamic::getDynamicId)
                        .eq(Dynamic::getFansFlag,1);
                List<Dynamic> dynamics = dynamicMapper.selectList(dynamicLambdaQueryWrapper1);
                if (!dynamics.isEmpty()){
                    //一个关注的动态对象集合
                    for (Dynamic dynamic : dynamics) {

                        LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        collectsLambdaQueryWrapper.eq(Collects::getUserId,userID)
                                .eq(Collects::getVideoId,dynamic.getVideoId())
                                .eq(Collects::getCollectName,"稍后再看");
                        dynamic.setWaitWatch(collectMapper.selectOne(collectsLambdaQueryWrapper)!=null?1:0);
                        //没有观看过该动态
                        if(dynamic.getWatchDynamicFlag()==0){

                            Videos video = videosMapper.selectById(dynamic.getVideoId());

                                Users followUser = userMapper.selectById(dynamic.getFollowId());
                                DynamicDto dynamicDto = DynamicDto.builder()
                                        .newDynamicNumber(fansUser.getDynamicNumber())
                                        .userId(followUser.getId())
                                        .avatarAddress(followUser.getAvatarAddress())
                                        .userName(followUser.getUserName())
                                        .title(video.getTitle())
                                        .coverAddress(video.getCoverAddress())
                                        .createTime(getDynamicCreatTime(video.getCreateTime()))
                                        .videoId(video.getId())
                                        .waitWatch(dynamic.getWaitWatch())
                                        .build();
                                dynamicDtoList.add(dynamicDto);

                            }
                        //观看过（历史动态）
                        else if(dynamic.getWatchDynamicFlag()==1){

                            Videos video = videosMapper.selectById(dynamic.getVideoId());

                            Users followUser = userMapper.selectById(dynamic.getFollowId());
                            DynamicDto dynamicDto = DynamicDto.builder()
                                    .newDynamicNumber(0)
                                    .avatarAddress(followUser.getAvatarAddress())
                                    .userName(followUser.getUserName())
                                    .userId(followUser.getId())
                                    .title(video.getTitle())
                                    .coverAddress(video.getCoverAddress())
                                    .createTime(getDynamicCreatTime(video.getCreateTime()))
                                    .videoId(video.getId())
                                    .waitWatch(dynamic.getWaitWatch())
                                    .build();
                            historyDynamicNumber++;
                            if(historyDynamicNumber<46)
                            dynamicDtoList.add(dynamicDto);
                            else break;
                        }
                        }
                    }
                }
        }
        return dynamicDtoList;
    }

    @Override
    @Transactional
    public Boolean cleanFollowDynamicAllNumber(Integer userId) {

        Users users = userMapper.selectById(userId);
        if(users.getDynamicNumber()!=0)
        {
            users.setDynamicNumber(0);
            userMapper.updateById(users);
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getFansId,userId)
                    .eq(Dynamic::getWatchDynamicFlag,0)
                    .eq(Dynamic::getFansFlag,1);

            List<Dynamic> dynamics = dynamicMapper.selectList(dynamicLambdaQueryWrapper);
            if(!dynamics.isEmpty())
            {
                for (Dynamic dynamic : dynamics) {
                    dynamic.setWatchDynamicFlag(1);
                    dynamicMapper.updateById(dynamic);
                }
            }
           return true;
        }
        else return false;

    }

    @Override
    @Transactional
    public int updateWaitWatch(Integer userId, Integer videoId) {

        Videos videos = videosMapper.selectById(videoId);
        if (videos!=null)
        {
                LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectsLambdaQueryWrapper.eq(Collects::getVideoId,videoId)
                        .eq(Collects::getUserId,userId)
                        .eq(Collects::getCollectName,"稍后再看");
                Collects collects = collectMapper.selectOne(collectsLambdaQueryWrapper);
                if(collects==null){

                    Collects collects1=Collects.builder()
                            .videoId(videoId)
                            .userId(userId)
                            .collectName("稍后再看")
                            .collectTime(LocalDateTime.now())
                            .build();
                    collectMapper.insert(collects1);

                    LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId)
                            .eq(CollectsClassify::getCollectName,"稍后再看");
                    CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
                    collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()+1);
                    collectClassifyMapper.updateById(collectsClassify);
                }
                else {
                    LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper1=new LambdaQueryWrapper<>();
                    collectsLambdaQueryWrapper1.eq(Collects::getUserId,userId)
                            .eq(Collects::getCollectName,"稍后再看")
                            .eq(Collects::getVideoId,videoId);
                    collectMapper.delete(collectsLambdaQueryWrapper1);

                    LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId)
                            .eq(CollectsClassify::getCollectName,"稍后再看");
                    CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
                    collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()-1);
                    collectClassifyMapper.updateById(collectsClassify);
                }
                if(collects==null)
                return 0;
                else
                    return 1;
            }
        return -1;
    }

    @Override
    public Page<Dynamic> dynamicImg(Integer homeUserId, Integer type, Integer pageNum) {

        Page<Dynamic> page=new Page<>(pageNum,20);
        //动态+评论
        if(type==1){
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,homeUserId)
                    .isNull(Dynamic::getFansId)
                    .isNotNull(Dynamic::getImgAddress);
            Page<Dynamic> page1 = dynamicMapper.selectPage(page,dynamicLambdaQueryWrapper);
           return page1;
           //评论
        }
        else if (type == 2) {
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,homeUserId)
                    .isNotNull(Dynamic::getCommentId)
                    .isNull(Dynamic::getFansId)
                    .isNotNull(Dynamic::getImgAddress);
            Page<Dynamic> page1 = dynamicMapper.selectPage(page,dynamicLambdaQueryWrapper);
            return page1;
            //动态
        }
        else if (type == 3) {
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,homeUserId)
                    .isNull(Dynamic::getCommentId)
                    .isNull(Dynamic::getFansId)
                    .isNotNull(Dynamic::getImgAddress);
            Page<Dynamic> page1 = dynamicMapper.selectPage(page,dynamicLambdaQueryWrapper);
            return page1;
        }
        return null;
    }

    @Override
    public Page<HomeDynamicDto> homeDynamic(Integer homeUserId, Integer userId, Integer pageNum, Integer type, String keyWord) {

        //全部
       if(type==1)
       {
           Page<HomeDynamicDto> page=new Page<>();
           if(StringUtil.isNullOrEmpty(keyWord)) {
               List<Dynamic> dynamicPage1 = dynamicMapper.homeDynamic(homeUserId,(pageNum-1)*10);
               page.setRecords(this.HomeDynamicDto(dynamicPage1, homeUserId, userId));
           }
           else{
               Page<Dynamic> dynamicPage=new Page<>(pageNum,10);
               LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
               dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,homeUserId)
                       .isNull(Dynamic::getFansId)
                       .isNull(Dynamic::getDynamicId)
                       .and(wrapper->wrapper
                        .like(Dynamic::getTitle,keyWord)
                        .or()
                        .like(Dynamic::getContent,keyWord)
                       )
                       .orderByDesc(Dynamic::getUpFlag)
                       .orderByDesc(Dynamic::getId);
               Page<Dynamic> dynamicPage1 = dynamicMapper.selectPage(dynamicPage, dynamicLambdaQueryWrapper);
               page.setRecords(this.HomeDynamicDto(dynamicPage1.getRecords(),homeUserId,userId));
               page.setTotal(dynamicPage1.getTotal());
           }
           return page;
       }
       //视频
       else if(type==2)
       {
           Page<Dynamic> dynamicPage=new Page<>(pageNum,10);
           LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
           dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,homeUserId)
                   .isNotNull(Dynamic::getVideoId)
                   .isNull(Dynamic::getDynamicId)
                   .isNull(Dynamic::getCommentId)
                   .isNull(Dynamic::getFansId)
                   .orderByDesc(Dynamic::getUpFlag)
                   .orderByDesc(Dynamic::getId);
           Page<Dynamic> dynamicPage1 = dynamicMapper.selectPage(dynamicPage, dynamicLambdaQueryWrapper);
           List<Dynamic> records = dynamicPage1.getRecords();

           Page<HomeDynamicDto> page=new Page<>();
           page.setRecords(this.HomeDynamicDto(records,homeUserId,userId));
           return page;
       }
       //图文
       else if(type==3){
           Page<Dynamic> dynamicPage=new Page<>(pageNum,10);
           LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
           dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,homeUserId)
                   .and(wrapper -> wrapper
                           .isNotNull(Dynamic::getContent)
                           .or()
                           .isNotNull(Dynamic::getImgAddress))
                   .isNull(Dynamic::getDynamicId)
                   .orderByDesc(Dynamic::getUpFlag)
                   .orderByDesc(Dynamic::getId);
           Page<Dynamic> dynamicPage1 = dynamicMapper.selectPage(dynamicPage, dynamicLambdaQueryWrapper);
           List<Dynamic> records = dynamicPage1.getRecords();

           Page<HomeDynamicDto> page=new Page<>();
           page.setRecords(this.HomeDynamicDto(records,homeUserId,userId));
           return page;
       }
       return null;
    }

    @Override
    @Transactional
    public Boolean putUpStatus(Integer dynamicId) {

        Dynamic dynamic = dynamicMapper.selectById(dynamicId);
        if(dynamic==null)
            return false;

        dynamic.setUpFlag(dynamic.getUpFlag()==1?0:1);
        //取消其他up的动态
        if(dynamic.getUpFlag()==1){
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,dynamic.getFollowId())
                    .eq(Dynamic::getUpFlag,1)
                    .isNull(Dynamic::getFansId);
            Dynamic dynamic1 = dynamicMapper.selectOne(dynamicLambdaQueryWrapper);
            if(dynamic1!=null){
                dynamic1.setUpFlag(0);
                dynamicMapper.updateById(dynamic1);
            }
        }
        dynamicMapper.updateById(dynamic);

        return true;
    }

    @Override
    @Transactional
    public Boolean likeDynamic(Integer userId, Integer dynamicId) {

        Dynamic dynamic = dynamicMapper.selectById(dynamicId);
        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getUserId,userId);
        if(dynamic.getCommentId()==null&&dynamic.getVideoId()!=null)
            likesLambdaQueryWrapper
                    .eq(Likes::getLikeType,1)
                    .eq(Likes::getFondId,dynamic.getVideoId());
        else
            likesLambdaQueryWrapper.
                    eq(Likes::getLikeType,3)
                    .eq(Likes::getFondId,dynamicId);

        Likes likes = likesMapper.selectOne(likesLambdaQueryWrapper);
        //取消点赞
        if(likes!=null){
            likesMapper.deleteById(likes);
            //是视频投稿
            if(dynamic.getCommentId()==null&&dynamic.getVideoId()!=null)
            {
                Videos videos = videosMapper.selectById(dynamic.getVideoId());
                videos.setLikeNumber(videos.getLikeNumber()-1);
                videosMapper.updateById(videos);
            }
            //动态
            else{
                dynamic.setLikeNumber(dynamic.getLikeNumber()-1);
                dynamicMapper.updateById(dynamic);
            }
        }
        //点赞
        else{
            Videos videos = videosMapper.selectById(dynamic.getVideoId());
            Likes likes1=Likes
                    .builder()
                    .likeTime(LocalDateTime.now())
                    .fondId(dynamic.getCommentId()==null&&dynamic.getVideoId()!=null?videos.getId():dynamicId)
                    .userId(userId)
                    .likeUserId(dynamic.getFollowId())
                    .likeType(dynamic.getCommentId()==null&&dynamic.getVideoId()!=null?1:3)
                    .build();
            likesMapper.insert(likes1);

            //是视频投稿
            if(dynamic.getCommentId()==null&&dynamic.getVideoId()!=null)
            {
                videos.setLikeNumber(videos.getLikeNumber()+1);
                videosMapper.updateById(videos);
                //更新消息提醒
                if(videos.getLikeWarn()==1&&!Objects.equals(videos.getUserId(), userId)){
                    Users users = userMapper.selectById(videos.getUserId());
                    if(users.getLikeMessageWarn()==1) {
                        users.setLikeAllNumber(users.getLikeAllNumber() + 1);
                        users.setAllMessageNumber(users.getAllMessageNumber() + 1);
                        userMapper.updateById(users);
                    }
                }
            }
            //动态
            else{
                dynamic.setLikeNumber(dynamic.getLikeNumber()+1);
                dynamicMapper.updateById(dynamic);
                //更新消息提醒
                if(dynamic.getLikeWarn()==1&&!Objects.equals(dynamic.getFollowId(), userId)){
                    Users users = userMapper.selectById(dynamic.getFollowId());
                    if(users.getLikeMessageWarn()==1) {
                        users.setLikeAllNumber(users.getLikeAllNumber() + 1);
                        users.setAllMessageNumber(users.getAllMessageNumber() + 1);
                        userMapper.updateById(users);
                    }
                }
            }
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteDynamic(Integer dynamicId) {

        Dynamic dynamic = dynamicMapper.selectById(dynamicId);
        if(dynamic==null)
            return false;

        dynamicMapper.deleteById(dynamic);

        //删除提醒的动态
        LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dynamicLambdaQueryWrapper.eq(Dynamic::getDynamicId,dynamicId);
        dynamicMapper.delete(dynamicLambdaQueryWrapper);

        Users users = userMapper.selectById(dynamic.getFollowId());
        users.setOwnDynamicNumber(users.getOwnDynamicNumber()-1);
        userMapper.updateById(users);

        LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        commentsLambdaQueryWrapper.eq(Comments::getDynamicId,dynamicId);
        commentsMapper.delete(commentsLambdaQueryWrapper);

        LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        commentControlsLambdaQueryWrapper.eq(CommentControls::getDynamicId,dynamicId);
        commentControlsMapper.delete(commentControlsLambdaQueryWrapper);

        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getFondId,dynamicId)
                  .eq(Likes::getLikeType,3);
        likesMapper.delete(likesLambdaQueryWrapper);

        if (dynamic.getVideoId() == null && StringUtil.notNullNorEmpty(dynamic.getImgAddress()) && dynamic.getImgAddress().contains("dynamic")) {
                String imgAddress = dynamic.getImgAddress();
                String[] split = imgAddress.split(",");
                for (String dynamicImg : split) {

                    int lastIndexOf = dynamicImg.lastIndexOf("/");
                    String dynamicFile = (lastIndexOf != -1) ? dynamicImg.substring(lastIndexOf + 1) : dynamicImg;
                    Path dynamicPath = Paths.get(FilePathEnum.UPLOAD_IMG_DYNAMIC.getPath() + dynamicFile);
                    try {
                        Files.delete(dynamicPath);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean addDynamic(AddDynamic addDynamic) {

        Dynamic dynamic = addDynamic.getDynamic();

        dynamic.setPublishTime(LocalDateTime.now());
        Users users = userMapper.selectById(dynamic.getFollowId());
        users.setOwnDynamicNumber(users.getOwnDynamicNumber()+1);
        int i = userMapper.updateById(users);

        if(StringUtil.notNullNorEmpty(dynamic.getImgAddress())){
            //上传图片
            StringBuilder imgAddress= new StringBuilder();
            String[] imgBase64S = dynamic.getImgAddress().split(",,");
                for (String imgBase64 : imgBase64S) {
                    String imageFormat = commentService.getImageFormat(imgBase64);
                    if (imageFormat == null||imageFormat.equals(".gif"))
                        continue;
                    UUID dynamicImgName = UUID.randomUUID();
                    //添加图片地址
                    imgAddress.append("/upload/dynamic/").append(dynamicImgName).append(imageFormat).append(",");
                    // 创建上传目录
                    File uploadDir;
                    uploadDir = new File(FilePathEnum.UPLOAD_IMG_DYNAMIC.getPath());
                    if (!uploadDir.exists())
                        uploadDir.mkdirs();
                    //上传
                    try {
                        String base64Body = imgBase64.contains(",") ? imgBase64.split(",")[1] : imgBase64;
                        byte[] decodedBytes = Base64.getDecoder().decode(base64Body);
                        // 写入文件
                        File dest = new File(uploadDir, dynamicImgName + imageFormat);
                        try (FileOutputStream fos = new FileOutputStream(dest)) {
                            fos.write(decodedBytes);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            dynamic.setImgAddress(imgAddress.toString());
        }
        int insert = dynamicMapper.insert(dynamic);

        //新增at
        List<At> ats = addDynamic.getAts();
        if(!ats.isEmpty()){
            for (At at : ats) {
                at.setDynamicId(dynamic.getId());
                if(Objects.equals(at.getUserId(), at.getAtUserId()))
                    continue;
                Users atUser = userMapper.selectById(at.getAtUserId());
                //所有人
                if(atUser.getAtMessageWarn()==1)
                {
                    atUser.setAtNumber(atUser.getAtNumber()+1);
                    atUser.setAllMessageNumber(atUser.getAllMessageNumber()+1);
                    userMapper.updateById(atUser);
                    atMapper.insert(at);
                }
                //关注的人
                else if(atUser.getAtMessageWarn()==2)
                {
                    LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    followLambdaQueryWrapper.eq(Follow::getUserId,at.getAtUserId())
                            .eq(Follow::getFollowId,at.getUserId());
                    Follow follow = followMapper.selectOne(followLambdaQueryWrapper);
                    if(follow!=null){
                        atUser.setAtNumber(atUser.getAtNumber() + 1);
                        atUser.setAllMessageNumber(atUser.getAllMessageNumber()+1);
                        userMapper.updateById(atUser);
                        atMapper.insert(at);
                    }
                }
            }
        }

        addDynamicByFansMessage(dynamic);
        return i>0&&insert>0;
    }

    private List<HomeDynamicDto> HomeDynamicDto(List<Dynamic> records,Integer homeUserId,Integer userId){

        List<HomeDynamicDto> homeDynamicDtoList=new ArrayList<>();
        for (Dynamic record : records) {
            HomeDynamicDto homeDynamicDto=new HomeDynamicDto();

            if(record.getCommentId()!=null){
                homeDynamicDto.setComment(commentsMapper.selectById(record.getCommentId()));
                homeDynamicDto.setPublishTime(this.getDynamicCreatTime2(homeDynamicDto.getComment().getCommentTime()));

                //封装视频
                Videos videos = videosMapper.selectById(homeDynamicDto.getComment().getVideoId());
                if(videos!=null){
                    List<Videos> videosList=new ArrayList<>();
                    videosList.add(videos);
                    List<SelectVideoDto> selectVideoDto2 = videosService.getSelectVideoDto2(videosList, userId, false);
                    if(!selectVideoDto2.isEmpty())
                        homeDynamicDto.setVideo(selectVideoDto2.get(0));
                    //up信息
                    Users upUser = userMapper.selectById(videos.getUserId());
                    UserInfo2 upUserInfo2=new UserInfo2();
                    BeanUtils.copyProperties(upUser,upUserInfo2);
                    //查询是否关注该up
                    LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    followLambdaQueryWrapper.eq(Follow::getUserId,userId)
                            .eq(Follow::getFollowId,videos.getUserId());
                    upUserInfo2.setIsFollowFlag(followMapper.selectOne(followLambdaQueryWrapper)!=null?1:0);
                    homeDynamicDto.setUpUserInfo2(upUserInfo2);
                }

                //查询是否点赞该视频
                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper2.eq(Likes::getFondId,record.getId())
                        .eq(Likes::getLikeType,3)
                        .eq(Likes::getDeleteFlag,0)
                        .eq(Likes::getUserId,userId)
                        .eq(Likes::getLikeUserId,homeUserId);
                homeDynamicDto.setLikeFlag(likesMapper.selectOne(likesLambdaQueryWrapper2)!=null?1:0);

                QueryWrapper<CommentControls> commentControlsLambdaQueryWrapper= new QueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq("dynamic_id",record.getId())
                        .select("comment_id","COUNT(*) as count")
                        .ne("user_id",homeUserId)
                        .last("LIMIT 1");
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

                //查询点赞数量最多的评论
                if(commentControls!=null){
                    Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                    if(comments.getMainCommentId()==null)
                        homeDynamicDto.setReplyComment(comments);
                }

                if(homeDynamicDto.getReplyComment()==null){
                    LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    commentsLambdaQueryWrapper.eq(Comments::getDynamicId,record.getId())
                            .eq(Comments::getDeleteSign,0)
                            .isNull(Comments::getMainCommentId)
                            .orderByDesc(Comments::getCommentTime)
                            .last("LIMIT 1");
                    homeDynamicDto.setReplyComment(commentsMapper.selectOne(commentsLambdaQueryWrapper));
                    if(homeDynamicDto.getReplyComment()!=null)
                    {
                        Users commentUsers = userMapper.selectById(homeDynamicDto.getReplyComment().getUserId());
                        UserInfo2 commentUserInfo2=new UserInfo2();
                        BeanUtils.copyProperties(commentUsers,commentUserInfo2);
                        homeDynamicDto.setReplyUserInfo2(commentUserInfo2);
                    }
                    //查询点赞
                    if(homeDynamicDto.getReplyComment()==null){
                        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        likesLambdaQueryWrapper.eq(Likes::getFondId,record.getId())
                                .eq(Likes::getLikeType,3)
                                .eq(Likes::getDeleteFlag,0)
                                .last("LIMIT 2")
                                .orderByDesc(Likes::getId);
                        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
                        List<UserInfo2> userInfo2List=new ArrayList<>();
                        for (Likes like : likes) {
                            Users users = userMapper.selectById(like.getUserId());
                            UserInfo2 userInfo2=new UserInfo2();
                            BeanUtils.copyProperties(users,userInfo2);
                            userInfo2List.add(userInfo2);
                        }
                        homeDynamicDto.setUserInfo2s(userInfo2List);
                    }
                }
            }
            else if(record.getVideoId() != null){

                Videos videos = videosMapper.selectById(record.getVideoId());

                List<Videos> videosList=new ArrayList<>();
                videosList.add(videos);
                List<SelectVideoDto> selectVideoDto = videosService.getSelectVideoDto2(videosList, userId, false);

                if(!selectVideoDto.isEmpty())
                    homeDynamicDto.setVideo(selectVideoDto.get(0));

                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper2.eq(Likes::getFondId,record.getVideoId())
                        .eq(Likes::getLikeType,1)
                        .eq(Likes::getDeleteFlag,0)
                        .eq(Likes::getUserId,userId)
                        .eq(Likes::getLikeUserId,homeUserId);
                homeDynamicDto.setLikeFlag(likesMapper.selectOne(likesLambdaQueryWrapper2)!=null?1:0);

                //查找点赞最多的评论
                QueryWrapper<CommentControls> commentControlsLambdaQueryWrapper= new QueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq("video_id",record.getVideoId())
                        .select("comment_id","COUNT(*) as count")
                        .ne("user_id",homeUserId)
                        .last("LIMIT 1");
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

                //查询点赞数量最多的评论
                if(commentControls!=null){
                    Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                    if(comments.getMainCommentId()==null)
                        homeDynamicDto.setReplyComment(comments);
                }

                if(homeDynamicDto.getReplyComment()==null){
                    LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    commentsLambdaQueryWrapper.eq(Comments::getVideoId,record.getVideoId())
                            .eq(Comments::getDeleteSign,0)
                            .isNull(Comments::getMainCommentId)
                            .orderByDesc(Comments::getCommentTime)
                            .last("LIMIT 1");
                    homeDynamicDto.setReplyComment(commentsMapper.selectOne(commentsLambdaQueryWrapper));
                    if(homeDynamicDto.getReplyComment()!=null)
                    {
                        Users commentUsers = userMapper.selectById(homeDynamicDto.getReplyComment().getUserId());
                        UserInfo2 commentUserInfo2=new UserInfo2();
                        BeanUtils.copyProperties(commentUsers,commentUserInfo2);
                        homeDynamicDto.setReplyUserInfo2(commentUserInfo2);
                    }
                    //查询点赞
                    if(homeDynamicDto.getReplyComment()==null) {
                        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper = new LambdaQueryWrapper<>();
                        likesLambdaQueryWrapper.eq(Likes::getFondId, record.getVideoId())
                                .eq(Likes::getLikeType,1)
                                .eq(Likes::getDeleteFlag,0)
                                .last("LIMIT 2")
                                .orderByDesc(Likes::getId);
                        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
                        List<UserInfo2> userInfo2List = new ArrayList<>();
                        for (Likes like : likes) {
                            Users users = userMapper.selectById(like.getUserId());
                            UserInfo2 userInfo2 = new UserInfo2();
                            BeanUtils.copyProperties(users, userInfo2);
                            userInfo2List.add(userInfo2);
                        }
                        homeDynamicDto.setUserInfo2s(userInfo2List);
                    }
                }
            }
            else{
                homeDynamicDto.setPublishTime(this.getDynamicCreatTime2(record.getPublishTime()));

                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper2.eq(Likes::getFondId,record.getId())
                        .eq(Likes::getLikeType,3)
                        .eq(Likes::getDeleteFlag,0)
                        .eq(Likes::getUserId,userId)
                        .eq(Likes::getLikeUserId,homeUserId);
                homeDynamicDto.setLikeFlag(likesMapper.selectOne(likesLambdaQueryWrapper2)!=null?1:0);

                //查找点赞最多的评论
                QueryWrapper<CommentControls> commentControlsLambdaQueryWrapper= new QueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq("dynamic_id",record.getId())
                        .select("comment_id","COUNT(*) as count")
                        .ne("user_id",homeUserId)
                        .last("LIMIT 1");
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

                //查询点赞数量最多的评论
                if(commentControls!=null){
                    Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                    if(comments.getMainCommentId()==null)
                        homeDynamicDto.setReplyComment(comments);
                }

                if(homeDynamicDto.getReplyComment()==null){
                    LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    commentsLambdaQueryWrapper.eq(Comments::getDynamicId,record.getId())
                            .eq(Comments::getDeleteSign,0)
                            .isNull(Comments::getMainCommentId)
                            .orderByDesc(Comments::getCommentTime)
                            .last("LIMIT 1");
                    homeDynamicDto.setReplyComment(commentsMapper.selectOne(commentsLambdaQueryWrapper));
                    if(homeDynamicDto.getReplyComment()!=null)
                    {
                        Users commentUsers = userMapper.selectById(homeDynamicDto.getReplyComment().getUserId());
                        UserInfo2 commentUserInfo2=new UserInfo2();
                        BeanUtils.copyProperties(commentUsers,commentUserInfo2);
                        homeDynamicDto.setReplyUserInfo2(commentUserInfo2);
                    }
                    //查询点赞
                    if(homeDynamicDto.getReplyComment()==null){
                        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        likesLambdaQueryWrapper.eq(Likes::getFondId,record.getId())
                                .eq(Likes::getLikeType,3)
                                .eq(Likes::getDeleteFlag,0)
                                .last("LIMIT 2")
                                .orderByDesc(Likes::getId);
                        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
                        List<UserInfo2> userInfo2List=new ArrayList<>();
                        for (Likes like : likes) {
                            Users users = userMapper.selectById(like.getUserId());
                            UserInfo2 userInfo2=new UserInfo2();
                            BeanUtils.copyProperties(users,userInfo2);
                            userInfo2List.add(userInfo2);
                        }
                        homeDynamicDto.setUserInfo2s(userInfo2List);
                    }
                }
            }

            Users users = userMapper.selectById(homeUserId);
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);
            homeDynamicDto.setDynamicUserInfo2(userInfo2);
            homeDynamicDto.setDynamic(record);
            homeDynamicDtoList.add(homeDynamicDto);
        }
        return homeDynamicDtoList;
    }

    private HomeDynamicDto HomeDynamicDto(Dynamic record,Integer homeUserId,Integer userId){

            HomeDynamicDto homeDynamicDto=new HomeDynamicDto();

            if(record.getCommentId()!=null){
                homeDynamicDto.setComment(commentsMapper.selectById(record.getCommentId()));
                homeDynamicDto.setPublishTime(this.getDynamicCreatTime2(homeDynamicDto.getComment().getCommentTime()));

                //封装视频
                Videos videos = videosMapper.selectById(homeDynamicDto.getComment().getVideoId());
                if(videos!=null){
                    List<Videos> videosList=new ArrayList<>();
                    videosList.add(videos);
                    List<SelectVideoDto> selectVideoDto2 = videosService.getSelectVideoDto2(videosList, userId, false);
                    if(!selectVideoDto2.isEmpty())
                        homeDynamicDto.setVideo(selectVideoDto2.get(0));
                    //up信息
                    Users upUser = userMapper.selectById(videos.getUserId());
                    UserInfo2 upUserInfo2=new UserInfo2();
                    BeanUtils.copyProperties(upUser,upUserInfo2);
                    //查询是否关注该up
                    LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    followLambdaQueryWrapper.eq(Follow::getUserId,userId)
                            .eq(Follow::getFollowId,videos.getUserId());
                    upUserInfo2.setIsFollowFlag(followMapper.selectOne(followLambdaQueryWrapper)!=null?1:0);
                    homeDynamicDto.setUpUserInfo2(upUserInfo2);
                }

                //查询是否点赞该视频
                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper2.eq(Likes::getFondId,record.getId())
                        .eq(Likes::getLikeType,3)
                        .eq(Likes::getDeleteFlag,0)
                        .eq(Likes::getUserId,userId)
                        .eq(Likes::getLikeUserId,homeUserId);
                homeDynamicDto.setLikeFlag(likesMapper.selectOne(likesLambdaQueryWrapper2)!=null?1:0);

                QueryWrapper<CommentControls> commentControlsLambdaQueryWrapper= new QueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq("dynamic_id",record.getId())
                        .select("comment_id","COUNT(*) as count")
                        .ne("user_id",homeUserId)
                        .last("LIMIT 1");
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

                //查询点赞数量最多的评论
                if(commentControls!=null){
                    Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                    if(comments.getMainCommentId()==null)
                        homeDynamicDto.setReplyComment(comments);
                }

                if(homeDynamicDto.getReplyComment()==null){
                    LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    commentsLambdaQueryWrapper.eq(Comments::getDynamicId,record.getId())
                            .eq(Comments::getDeleteSign,0)
                            .isNull(Comments::getMainCommentId)
                            .orderByDesc(Comments::getCommentTime)
                            .last("LIMIT 1");
                    homeDynamicDto.setReplyComment(commentsMapper.selectOne(commentsLambdaQueryWrapper));
                    if(homeDynamicDto.getReplyComment()!=null)
                    {
                        Users commentUsers = userMapper.selectById(homeDynamicDto.getReplyComment().getUserId());
                        UserInfo2 commentUserInfo2=new UserInfo2();
                        BeanUtils.copyProperties(commentUsers,commentUserInfo2);
                        homeDynamicDto.setReplyUserInfo2(commentUserInfo2);
                    }
                    //查询点赞
                    if(homeDynamicDto.getReplyComment()==null){
                        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        likesLambdaQueryWrapper.eq(Likes::getFondId,record.getId())
                                .eq(Likes::getLikeType,3)
                                .eq(Likes::getDeleteFlag,0)
                                .last("LIMIT 2")
                                .orderByDesc(Likes::getId);
                        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
                        List<UserInfo2> userInfo2List=new ArrayList<>();
                        for (Likes like : likes) {
                            Users users = userMapper.selectById(like.getUserId());
                            UserInfo2 userInfo2=new UserInfo2();
                            BeanUtils.copyProperties(users,userInfo2);
                            userInfo2List.add(userInfo2);
                        }
                        homeDynamicDto.setUserInfo2s(userInfo2List);
                    }
                }
            }
            else if(record.getVideoId() != null){

                Videos videos = videosMapper.selectById(record.getVideoId());

                List<Videos> videosList=new ArrayList<>();
                videosList.add(videos);
                List<SelectVideoDto> selectVideoDto = videosService.getSelectVideoDto2(videosList, userId, false);

                if(!selectVideoDto.isEmpty())
                    homeDynamicDto.setVideo(selectVideoDto.get(0));

                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper2.eq(Likes::getFondId,record.getVideoId())
                        .eq(Likes::getLikeType,1)
                        .eq(Likes::getDeleteFlag,0)
                        .eq(Likes::getUserId,userId)
                        .eq(Likes::getLikeUserId,homeUserId);
                homeDynamicDto.setLikeFlag(likesMapper.selectOne(likesLambdaQueryWrapper2)!=null?1:0);

                //查找点赞最多的评论
                QueryWrapper<CommentControls> commentControlsLambdaQueryWrapper= new QueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq("video_id",record.getVideoId())
                        .select("comment_id","COUNT(*) as count")
                        .ne("user_id",homeUserId)
                        .last("LIMIT 1");
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

                //查询点赞数量最多的评论
                if(commentControls!=null){
                    Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                    if(comments.getMainCommentId()==null)
                        homeDynamicDto.setReplyComment(comments);
                }

                if(homeDynamicDto.getReplyComment()==null){
                    LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    commentsLambdaQueryWrapper.eq(Comments::getVideoId,record.getVideoId())
                            .eq(Comments::getDeleteSign,0)
                            .isNull(Comments::getMainCommentId)
                            .orderByDesc(Comments::getCommentTime)
                            .last("LIMIT 1");
                    homeDynamicDto.setReplyComment(commentsMapper.selectOne(commentsLambdaQueryWrapper));
                    if(homeDynamicDto.getReplyComment()!=null)
                    {
                        Users commentUsers = userMapper.selectById(homeDynamicDto.getReplyComment().getUserId());
                        UserInfo2 commentUserInfo2=new UserInfo2();
                        BeanUtils.copyProperties(commentUsers,commentUserInfo2);
                        homeDynamicDto.setReplyUserInfo2(commentUserInfo2);
                    }
                    //查询点赞
                    if(homeDynamicDto.getReplyComment()==null) {
                        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper = new LambdaQueryWrapper<>();
                        likesLambdaQueryWrapper.eq(Likes::getFondId, record.getVideoId())
                                .eq(Likes::getLikeType,1)
                                .eq(Likes::getDeleteFlag,0)
                                .last("LIMIT 2")
                                .orderByDesc(Likes::getId);
                        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
                        List<UserInfo2> userInfo2List = new ArrayList<>();
                        for (Likes like : likes) {
                            Users users = userMapper.selectById(like.getUserId());
                            UserInfo2 userInfo2 = new UserInfo2();
                            BeanUtils.copyProperties(users, userInfo2);
                            userInfo2List.add(userInfo2);
                        }
                        homeDynamicDto.setUserInfo2s(userInfo2List);
                    }
                }
            }
            else{
                homeDynamicDto.setPublishTime(this.getDynamicCreatTime2(record.getPublishTime()));

                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper2.eq(Likes::getFondId,record.getId())
                        .eq(Likes::getLikeType,3)
                        .eq(Likes::getDeleteFlag,0)
                        .eq(Likes::getUserId,userId)
                        .eq(Likes::getLikeUserId,homeUserId);
                homeDynamicDto.setLikeFlag(likesMapper.selectOne(likesLambdaQueryWrapper2)!=null?1:0);

                //查找点赞最多的评论
                QueryWrapper<CommentControls> commentControlsLambdaQueryWrapper= new QueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq("dynamic_id",record.getId())
                        .select("comment_id","COUNT(*) as count")
                        .ne("user_id",homeUserId)
                        .last("LIMIT 1");
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

                //查询点赞数量最多的评论
                if(commentControls!=null){
                    Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                    if(comments.getMainCommentId()==null)
                        homeDynamicDto.setReplyComment(comments);
                }

                if(homeDynamicDto.getReplyComment()==null){
                    LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    commentsLambdaQueryWrapper.eq(Comments::getDynamicId,record.getId())
                            .eq(Comments::getDeleteSign,0)
                            .isNull(Comments::getMainCommentId)
                            .orderByDesc(Comments::getCommentTime)
                            .last("LIMIT 1");
                    homeDynamicDto.setReplyComment(commentsMapper.selectOne(commentsLambdaQueryWrapper));
                    if(homeDynamicDto.getReplyComment()!=null)
                    {
                        Users commentUsers = userMapper.selectById(homeDynamicDto.getReplyComment().getUserId());
                        UserInfo2 commentUserInfo2=new UserInfo2();
                        BeanUtils.copyProperties(commentUsers,commentUserInfo2);
                        homeDynamicDto.setReplyUserInfo2(commentUserInfo2);
                    }
                    //查询点赞
                    if(homeDynamicDto.getReplyComment()==null){
                        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
                        likesLambdaQueryWrapper.eq(Likes::getFondId,record.getId())
                                .eq(Likes::getLikeType,3)
                                .eq(Likes::getDeleteFlag,0)
                                .last("LIMIT 2")
                                .orderByDesc(Likes::getId);
                        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
                        List<UserInfo2> userInfo2List=new ArrayList<>();
                        for (Likes like : likes) {
                            Users users = userMapper.selectById(like.getUserId());
                            UserInfo2 userInfo2=new UserInfo2();
                            BeanUtils.copyProperties(users,userInfo2);
                            userInfo2List.add(userInfo2);
                        }
                        homeDynamicDto.setUserInfo2s(userInfo2List);
                    }
                }
            }

        Users users = userMapper.selectById(homeUserId);
        UserInfo2 userInfo2=new UserInfo2();
        BeanUtils.copyProperties(users,userInfo2);
        homeDynamicDto.setDynamicUserInfo2(userInfo2);
        homeDynamicDto.setDynamic(record);
        return homeDynamicDto;
    }

    private String getDynamicCreatTime(LocalDateTime localDateTime) {

        String substring;
        Duration duration=Duration.between(localDateTime,LocalDateTime.now());
        if (duration.toDays()<1){

            if(duration.toHours()<1){
                if(duration.toMinutes()<1)
                    substring="1分钟前";
                else
                    substring=duration.toMinutes()+"分钟前";
            }
            else
                substring=duration.toHours()+"小时前";
        }

             else if (duration.toHours()<2) {

                substring="昨天";

            }else if (duration.toHours()<3) {

                substring="前天";

            } else if (duration.toHours()<4) {

            substring="3天前";
        }

        else{
            int nowYear=LocalDateTime.now().getYear();
            LocalDateTime startTIme=LocalDateTime.of(nowYear,1,1,0,0,0);
            LocalDateTime endTIme=LocalDateTime.of(nowYear,12,31,23,59,59);
            if(localDateTime.isAfter(startTIme)&&localDateTime.isBefore(endTIme) ) {
                String createTime = String.valueOf(localDateTime);
                substring = createTime.charAt(5) == '0' ? createTime.substring(6, 10) : createTime.substring(5, 10);
            }
            else

            {
                String a= String.valueOf(localDateTime);
                substring=a.substring(0,10);
            }
        }

        return substring;

    }

    private String getDynamicCreatTime2(LocalDateTime localDateTime) {

        String substring;
        Duration duration=Duration.between(localDateTime,LocalDateTime.now());
        if (duration.toDays()<1){

            if(duration.toHours()<1){
                if(duration.toMinutes()<1)
                    substring="1分钟前";
                else
                    substring=duration.toMinutes()+"分钟前";
            }
            else
                substring=duration.toHours()+"小时前";
        }

        else if (duration.toHours()<2) {

            substring="昨天";

        }else if (duration.toHours()<3) {

            substring="前天";

        } else if (duration.toHours()<4) {

            substring="3天前";
        }

        else{
            int nowYear=LocalDateTime.now().getYear();
            LocalDateTime startTIme=LocalDateTime.of(nowYear,1,1,0,0,0);
            LocalDateTime endTIme=LocalDateTime.of(nowYear,12,31,23,59,59);
            if(localDateTime.isAfter(startTIme)&&localDateTime.isBefore(endTIme) ) {
                String createTime = String.valueOf(localDateTime);
                substring = createTime.substring(5, 10);
            }
            else

            {
                String a= String.valueOf(localDateTime);
                substring=a.substring(0,10);
            }
        }

        return substring;

    }

    @Override
    public void addDynamicByFansMessage(Dynamic dynamic){

        Integer dynamicId=dynamic.getId();
        dynamic.setId(null);
        //进行更新提醒
        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Fans::getUserId,dynamic.getFollowId());
        List<Fans> fans = fansMapper.selectList(fansLambdaQueryWrapper);
        for (Fans fan : fans) {
            dynamic.setFansId(fan.getFansId());
            dynamic.setDynamicId(dynamicId);
            dynamicMapper.insert(dynamic);
        }
    }

    @Override
    @Transactional
    public void cleanDynamicMessage(Integer userId, Integer dynamicUserId) {

        LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dynamicLambdaQueryWrapper.eq(Dynamic::getFansId,userId)
                .eq(Dynamic::getFollowId,dynamicUserId)
                .isNotNull(Dynamic::getDynamicId);
        dynamicMapper.delete(dynamicLambdaQueryWrapper);

        LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper2=new LambdaQueryWrapper<>();
        dynamicLambdaQueryWrapper2.eq(Dynamic::getFansId,userId)
                .eq(Dynamic::getFollowId,dynamicUserId)
                .isNull(Dynamic::getDynamicId);
        List<Dynamic> dynamics = dynamicMapper.selectList(dynamicLambdaQueryWrapper2);
        for (Dynamic dynamic : dynamics) {
            dynamic.setWaitWatch(1);
            dynamicMapper.updateById(dynamic);
        }
    }

    @Override
    public List<UserInfo2> usersDynamic(Integer userId, Integer pageNum) {

        Page<Follow> followPage=new Page<>(pageNum,10);

        LambdaQueryWrapper<Follow> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Follow::getUserId,userId);
        Page<Follow> followPage1 = followMapper.selectPage(followPage, fansLambdaQueryWrapper);
        List<Follow> records = followPage1.getRecords();

        List<UserInfo2> userInfo2List=new ArrayList<>();

        if(pageNum==1)
        {
            Users users = userMapper.selectById(userId);
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);
            userInfo2.setNewDynamicFlag(0);
            userInfo2List.add(userInfo2);
        }

        for (Follow record : records) {

            Users users = userMapper.selectById(record.getFollowId());
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);

            //查询是否有新的动态
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getFollowId,record.getFollowId())
                    .eq(Dynamic::getFansId,userId)
                    .and(wrapper->wrapper
                            .isNull(Dynamic::getDynamicId)
                            .eq(Dynamic::getWaitWatch,0)
                            .or()
                            .isNotNull(Dynamic::getDynamicId)
                    )
                    .last("LIMIT 1");
            if(dynamicMapper.selectOne(dynamicLambdaQueryWrapper)!=null)
                userInfo2.setNewDynamicFlag(1);
            else userInfo2.setNewDynamicFlag(0);
            userInfo2List.add(userInfo2);
        }

        return userInfo2List;
    }

    @Override
    public List<HomeDynamicDto> getDynamic(Integer dynamicUserId, Integer userId, Integer pageNum, Integer type) {

        //全部动态
        if(dynamicUserId==0){
            if(type==1){
                List<HomeDynamicDto> homeDynamicDtoList=new ArrayList<>();
                List<Dynamic> allDynamic = dynamicMapper.getAllDynamic(userId,(pageNum-1)*10);
                for (Dynamic dynamic : allDynamic) {
                    homeDynamicDtoList.add(this.HomeDynamicDto(dynamic, dynamic.getFollowId(), userId));
                }
                return homeDynamicDtoList;
            } else if (type == 2) {
                List<HomeDynamicDto> homeDynamicDtoList=new ArrayList<>();
                List<Dynamic> allDynamic = dynamicMapper.getAllDynamic2(userId,(pageNum-1)*10);
                for (Dynamic dynamic : allDynamic) {
                    homeDynamicDtoList.add(this.HomeDynamicDto(dynamic, dynamic.getFollowId(), userId));
                }
                return homeDynamicDtoList;
            } else if (type == 3) {
                List<HomeDynamicDto> homeDynamicDtoList=new ArrayList<>();
                List<Dynamic> allDynamic = dynamicMapper.getAllDynamic3(userId,(pageNum-1)*10);
                for (Dynamic dynamic : allDynamic) {
                    homeDynamicDtoList.add(this.HomeDynamicDto(dynamic, dynamic.getFollowId(), userId));
                }
                return homeDynamicDtoList;
            }
        }
        //个人动态
        else{
           return this.homeDynamic(dynamicUserId, userId, pageNum, type, "").getRecords();
        }
        return null;
    }

    @Override
    public Boolean allDynamicMessage(Integer userId) {

        LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dynamicLambdaQueryWrapper.eq(Dynamic::getFansId,userId)
                .and(wrapper->wrapper
                        .isNull(Dynamic::getDynamicId)
                        .eq(Dynamic::getWaitWatch,0)
                        .or()
                        .isNotNull(Dynamic::getDynamicId)
                )
                .last("LIMIT 1");
        return dynamicMapper.selectOne(dynamicLambdaQueryWrapper)!=null;
    }

    @Override
    public Boolean selectDynamicByIdLike(Integer dynamicId, Integer userId) {

        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getUserId,userId)
                .eq(Likes::getFondId,dynamicId)
                .eq(Likes::getLikeType,3)
                .eq(Likes::getDeleteFlag,0);
        return likesMapper.selectOne(likesLambdaQueryWrapper)!=null;
    }

    @Override
    public HomeDynamicDto selectDynamicById(Integer dynamicId) {

        Dynamic dynamic = dynamicMapper.selectById(dynamicId);
        if(dynamic==null)
            return null;

        HomeDynamicDto homeDynamicDto=new HomeDynamicDto();

        Users users = userMapper.selectById(dynamic.getFollowId());
        UserInfo2 userInfo2=new UserInfo2();
        BeanUtils.copyProperties(users,userInfo2);

        if(dynamic.getVideoId()!=null){
            Videos videos = videosMapper.selectById(dynamic.getVideoId());
            homeDynamicDto.setVideo(videosService.getSelectVideo(videos,0,false));
            Users users1 = userMapper.selectById(videos.getUserId());
            UserInfo2 upUserInfo2=new UserInfo2();
            BeanUtils.copyProperties(users1,upUserInfo2);
            homeDynamicDto.setUpUserInfo2(upUserInfo2);
            dynamic.setPublishTime(videos.getCreateTime() );
        }
        homeDynamicDto.setDynamic(dynamic);
        homeDynamicDto.setPublishTime(this.getDynamicCreatTime2(dynamic.getPublishTime()));
        homeDynamicDto.setDynamicUserInfo2(userInfo2);
        return homeDynamicDto;
    }

    @Override
    public List<UserInfo2> likeDynamicUser(Integer userId, Integer pageNum, Integer dynamicId) {

        Page<Likes> page=new Page<>(pageNum,10);
        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getLikeType,3)
                .eq(Likes::getFondId,dynamicId);

        Page<Likes> likesPage = likesMapper.selectPage(page, likesLambdaQueryWrapper);
        List<Likes> records = likesPage.getRecords();

        List<UserInfo2> userInfo2List=new ArrayList<>();
        for (Likes record : records) {

            Users users = userMapper.selectById(record.getUserId());
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);
            //查询是否关注
            if(userId!=null)
            {
               LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
               followLambdaQueryWrapper.eq(Follow::getFollowId,record.getUserId())
                       .eq(Follow::getUserId,userId);
                Follow follow = followMapper.selectOne(followLambdaQueryWrapper);
                userInfo2.setIsFollowFlag(follow!=null?1:0);
            }
            userInfo2List.add(userInfo2);
        }
        return userInfo2List;
    }

}
