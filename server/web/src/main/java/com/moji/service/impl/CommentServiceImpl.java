package com.moji.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.dto.*;
import com.moji.mapper.*;
import com.moji.po.*;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.CacheService;
import com.moji.service.CommentService;
import com.moji.service.DynamicService;
import com.moji.vo.SelectComment;
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
import java.util.*;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentService {

   @Autowired
   private CommentsMapper commentsMapper;
   @Autowired
   private VideosMapper videosMapper;
   @Autowired
   private UserMapper userMapper;
   @Autowired
   private CommentControlsMapper commentControlsMapper;
   @Autowired
   private FollowMapper followMapper;
   @Autowired
   private AtMapper atMapper;
   @Autowired
   private LikesMapper likesMapper;
   @Autowired
   private FansMapper fansMapper;
   @Autowired
   private CacheService cacheService;
   @Autowired
   private DynamicMapper dynamicMapper;
   @Autowired
   private DynamicService dynamicService;


    @Override
    @Transactional
    public SelectComment addComment(AddComment addComment)  {

        Comments comments = addComment.getComments();
        if(comments==null)
            return null;

        //更新评论数量
        Videos videos = videosMapper.selectById(comments.getVideoId());
        if(videos!=null){
            videos.setCommentNumber(videos.getCommentNumber()+1);
            videosMapper.updateById(videos);
        }else {
            Dynamic dynamic = dynamicMapper.selectById(comments.getDynamicId());
            dynamic.setCommentNumber(dynamic.getCommentNumber()+1);
            dynamicMapper.updateById(dynamic);
        }
        //上传图片
        StringBuilder imgAddress= new StringBuilder();
        String[] imgBase64S = comments.getImgAddress()!=null?comments.getImgAddress().split(",,"):null;
        if(comments.getImgAddress()!=null) {
            for (String imgBase64 : imgBase64S) {
                String imageFormat = getImageFormat(imgBase64);
                if (imageFormat == null||imageFormat.equals(".gif"))
                    continue;
                UUID commentImgName = UUID.randomUUID();
                //添加图片地址
                imgAddress.append("/upload/comment/").append(commentImgName).append(imageFormat).append(",");
                // 创建上传目录
                File uploadDir;
                uploadDir = new File(FilePathEnum.UPLOAD_COMMENT_IMG.getPath());
                if (!uploadDir.exists())
                    uploadDir.mkdirs();
                //上传
                try {
                    String base64Body = imgBase64.contains(",") ? imgBase64.split(",")[1] : imgBase64;
                    byte[] decodedBytes = Base64.getDecoder().decode(base64Body);
                    // 写入文件
                    File dest = new File(uploadDir, commentImgName + imageFormat);
                    try (FileOutputStream fos = new FileOutputStream(dest)) {
                        fos.write(decodedBytes);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        //更新被回复的用户消息数量
        if(comments.getReplyUserId()!=null&&!comments.getReplyUserId().equals(comments.getUserId()))
        {
            Users replyUser = userMapper.selectById(comments.getReplyUserId());
            if(replyUser==null)
                return null;
            //所有人
            if(replyUser.getReplyMessageWarn()==1){
                replyUser.setAllMessageNumber(replyUser.getAllMessageNumber()+1);
                replyUser.setReplyCommentNumber(replyUser.getReplyCommentNumber()+1);
                userMapper.updateById(replyUser);
            }
            //关注的人
            else if(replyUser.getReplyMessageWarn()==2){
                LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
                followLambdaQueryWrapper.eq(Follow::getUserId,comments.getReplyUserId())
                        .eq(Follow::getFollowId,comments.getUserId());
                Follow follow = followMapper.selectOne(followLambdaQueryWrapper);
                if(follow!=null){
                    replyUser.setAllMessageNumber(replyUser.getAllMessageNumber()+1);
                    replyUser.setReplyCommentNumber(replyUser.getReplyCommentNumber()+1);
                    userMapper.updateById(replyUser);
                }
            }
        }

        comments.setCommentTime(LocalDateTime.now());
        comments.setImgAddress(imgAddress.toString());
        commentsMapper.insert(comments);

        //新增at
        List<At> ats = addComment.getAts();
        if(!ats.isEmpty()){
            for (At at : ats) {
                at.setCommentId(comments.getId());
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

        UserInfo2 userInfo2=new UserInfo2();
        Users users = userMapper.selectById(comments.getUserId());
        BeanUtils.copyProperties(users,userInfo2);
        //更新自己动态的数量和新增评论
        if(comments.getDynamicFlag()==1)
        {
            users.setOwnDynamicNumber(users.getOwnDynamicNumber()+1);
            userMapper.updateById(users);
            Dynamic dynamic=Dynamic
                    .builder()
                    .videoId(comments.getVideoId())
                    .followId(comments.getUserId())
                    .commentId(comments.getId())
                    .content(comments.getContent())
                    .imgAddress(comments.getImgAddress()!=null&&!comments.getImgAddress().isEmpty()?comments.getImgAddress():null)
                    .build();
            dynamicMapper.insert(dynamic);
            dynamicService.addDynamicByFansMessage(dynamic);
        }

        //删除缓存
        if(comments.getMainCommentId()==null)
            cacheService.deleteCommentCacheByVideoId(comments.getVideoId(),comments.getDynamicId(),comments.getUserId());
        else
        {
            cacheService.deleteCommentCacheByVideoId(comments.getVideoId(),comments.getDynamicId(),comments.getUserId());
            cacheService.deleteReplyCommentCacheByCommentId(comments.getVideoId(),comments.getDynamicId(),comments.getMainCommentId(), comments.getUserId());
        }

        return SelectComment.builder().
                userInfo(userInfo2)
                .comments(comments)
                .build();
    }

    @Override
    public Page<SelectComment> selectCommentByVideoId(Integer videoId,Integer dynamicId, Integer userId, Integer commentSortFlag, Integer pageNum) {
        // 创建分页对象
        Page<Comments> page = new Page<>(pageNum, 10);

        // 构造查询条件
        LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper = new LambdaQueryWrapper<>();
        commentsLambdaQueryWrapper
                .lt(Comments::getDynamicFlag,2)
                .isNull(Comments::getMainCommentId)
                .orderByDesc(Comments::getUpFlag);

        if(videoId!=null)
            commentsLambdaQueryWrapper.eq(Comments::getVideoId,videoId);
        else if(dynamicId!=null)
            commentsLambdaQueryWrapper.eq(Comments::getDynamicId,dynamicId);
        else
            return null;

        // 根据排序方式进行排序
        if (commentSortFlag == 1) {
            commentsLambdaQueryWrapper.orderByDesc(Comments::getLikeCommentNumber);
        } else {
            commentsLambdaQueryWrapper.orderByDesc(Comments::getCommentTime);
        }

        // 执行分页查询
        Page<Comments> commentsPage = commentsMapper.selectPage(page, commentsLambdaQueryWrapper);

        // 获取查询结果
        List<Comments> commentsList = commentsPage.getRecords();
        List<SelectComment> selectComments = new ArrayList<>();

        for (Comments comment : commentsList) {
            SelectComment selectComment = new SelectComment();

            //查询up是否给该评论点赞
            LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper2=new LambdaQueryWrapper<>();
            commentControlsLambdaQueryWrapper2.eq(CommentControls::getCommentId,comment.getId());
            if(comment.getVideoId()!=null)
            {
                Videos videos = videosMapper.selectById(comment.getVideoId());
                commentControlsLambdaQueryWrapper2.eq(CommentControls::getUserId,videos.getUserId());
            }
            else{
                Dynamic dynamic = dynamicMapper.selectById(comment.getDynamicId());
                commentControlsLambdaQueryWrapper2.eq(CommentControls::getUserId,dynamic.getFollowId());
            }
            CommentControls commentControls1 = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper2);
            if(commentControls1!=null&&commentControls1.getControls()==1)
                selectComment.setUpLikeFlag(1);
            else selectComment.setUpLikeFlag(0);

            //查询该评论的回复数量
            LambdaQueryWrapper<Comments> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(Comments::getMainCommentId,comment.getId())
                    .eq(Comments::getDeleteSign,0)
                    .lt(Comments::getDynamicFlag,2);
            Long commentsCount = commentsMapper.selectCount(lambdaQueryWrapper);
            selectComment.setReplyNumber(commentsCount);

            // 查询评论的用户信息
            UserInfo2 userInfo2 = new UserInfo2();
            Users users = userMapper.selectById(comment.getUserId());
            if (users != null) {
                BeanUtils.copyProperties(users, userInfo2);
                //查询是否互相关注
                if(userId!=0)
                {
                    LambdaQueryWrapper<Follow> followLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    followLambdaQueryWrapper.eq(Follow::getUserId,userId)
                            .eq(Follow::getFollowId,comment.getUserId());
                    Follow follow = followMapper.selectOne(followLambdaQueryWrapper);
                    if(follow!=null)
                        userInfo2.setIsFansFlag(1);
                    else userInfo2.setIsFansFlag(0);

                    LambdaQueryWrapper<Follow> followLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                    followLambdaQueryWrapper2.eq(Follow::getUserId,comment.getUserId())
                            .eq(Follow::getFollowId,userId);
                    Follow follow2 = followMapper.selectOne(followLambdaQueryWrapper2);
                    if(follow2!=null)
                        userInfo2.setIsFollowFlag(1);
                    else userInfo2.setIsFollowFlag(0);

                }
            }

            // 查询用户是否对该评论操作
            if (userId != 0) {
                LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper = new LambdaQueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq(CommentControls::getCommentId, comment.getId())
                        .ne(CommentControls::getControls,-1)
                        .eq(CommentControls::getUserId, userId);
                CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);
                if (commentControls != null) {
                    selectComment.setCommentControls(commentControls);
                }
            }

            selectComment.setComments(comment);
            selectComment.setUserInfo(userInfo2);
            selectComments.add(selectComment);
        }

        // 构造返回的分页对象
        Page<SelectComment> resultPage = new Page<>(pageNum, 10);
        resultPage.setRecords(selectComments);
        resultPage.setTotal(commentsPage.getTotal());
        resultPage.setCurrent(commentsPage.getCurrent());
        resultPage.setSize(commentsPage.getSize());

        return resultPage;
    }

    @Override
    @Transactional
    public Boolean deleteComment(Integer commentId, Integer userId, String token) {

        Comments comments = commentsMapper.selectById(commentId);
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(comments.getVideoId()!=null)
        {
            Videos videos = videosMapper.selectById(comments.getVideoId());
            if(!limiterServer.checkUser(userId,token) || !(Objects.equals(userId, videos.getUserId())||Objects.equals(comments.getUserId(),userId)))
                return false;
        }else if(comments.getDynamicId()!=null)
        {
            Dynamic dynamic = dynamicMapper.selectById(comments.getDynamicId());
            if(!limiterServer.checkUser(userId,token) || !(Objects.equals(userId, dynamic.getFollowId())||Objects.equals(userId,comments.getUserId())))
                return false;
        }

            //删除主评论 判断是否是回复评论
            if(comments.getMainCommentId()==null){
                List<Comments> commentsList=new ArrayList<>();
                commentsList.add(comments);
                deleteReply(commentsList,true,true);

                //查找回复这条评论的评论
                LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                commentsLambdaQueryWrapper.eq(Comments::getMainCommentId,commentId);
                List<Comments> replyCommentList = commentsMapper.selectList(commentsLambdaQueryWrapper);
                deleteReply(replyCommentList,true,true);
                cacheService.deleteCommentCacheByVideoId(comments.getVideoId(),comments.getDynamicId(),userId);
                return true;
            }
            else {
               List<Comments> commentsList=new ArrayList<>();
               commentsList.add(comments);
               deleteReply(commentsList,true,false);
                cacheService.deleteCommentCacheByVideoId(comments.getVideoId(),comments.getDynamicId(),userId);
                cacheService.deleteReplyCommentCacheByCommentId(comments.getVideoId(),comments.getDynamicId(),comments.getMainCommentId(),comments.getUserId());
                return true;
            }
    }

    @Override
    @Transactional
    public SelectComment controlsComment(CommentControls commentControls, String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(commentControls!=null&&limiterServer.checkUser(commentControls.getUserId(),token)){

            LambdaQueryWrapper<Users> usersLambdaQueryWrapper=new LambdaQueryWrapper<>();
            usersLambdaQueryWrapper.eq(Users::getId,commentControls.getUserId());
            Users users = userMapper.selectOne(usersLambdaQueryWrapper);
            if(users==null)
                return null;

            commentControls.setLikeCommentTime(LocalDateTime.now());
            LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper = new LambdaQueryWrapper<>();
            commentControlsLambdaQueryWrapper.eq(CommentControls::getCommentId, commentControls.getCommentId())
                    .eq(CommentControls::getUserId, commentControls.getUserId());
            CommentControls commentControls1=commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);
            if(Objects.equals(users.getId(), commentControls.getUserId()))
            {
                //踩
                if(commentControls.getControls()==0)
                {
                    //如果没有查到新增记录
                    if(commentControls1==null){
                        commentControlsMapper.insert(commentControls);
                    }
                    else{
                        //如果==代表取消踩
                        if(Objects.equals(0, commentControls1.getControls())) {
                            commentControlsMapper.deleteById(commentControls1);
                        }
                        else if(commentControls1.getControls()==1)//否则更新
                        {
                            commentControls.setId(commentControls1.getId());
                            commentControlsMapper.updateById(commentControls);
                            //减评论喜欢数量
                            Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                            if(comments==null)
                                return null;

                            //删除喜欢记录
                            LambdaQueryWrapper<Likes> lambdaQueryWrapper=new LambdaQueryWrapper<>();
                            lambdaQueryWrapper.eq(Likes::getUserId,users.getId())
                                    .eq(Likes::getLikeType,2)
                                    .eq(Likes::getFondId,comments.getId());
                            likesMapper.delete(lambdaQueryWrapper);

                            comments.setLikeCommentNumber(comments.getLikeCommentNumber()-1);
                            commentsMapper.updateById(comments);
                            commentControlsMapper.updateById(commentControls);
                            //判断是否开启点赞消息提醒
                            if(comments.getLikeWarn()==1) {
                                Users users1 = userMapper.selectById(comments.getUserId());
                                if(users1.getLikeMessageWarn()==1&&!Objects.equals(comments.getUserId(), users1.getId()))
                                {
                                    users1.setLikeAllNumber(users1.getLikeAllNumber() == 0 ? 0 : users1.getLikeAllNumber() - 1);
                                    users1.setAllMessageNumber(users1.getAllMessageNumber() == 0 ? 0 : users1.getAllMessageNumber() - 1);
                                    userMapper.updateById(users1);
                                }
                            }
                        }
                    }
                }//点赞
                else if(commentControls.getControls()==1){
                        //如果没有查到新增记录
                        if(commentControls1==null){
                            //增加评论喜欢数量
                            Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                            if(comments==null)
                                return null;

                            Likes likes=Likes.builder()
                                        .userId(users.getId())
                                        .fondId(comments.getId())
                                        .likeTime(LocalDateTime.now())
                                        .likeType(2)
                                        .likeUserId(comments.getUserId())
                                        .build();
                            likesMapper.insert(likes);

                            comments.setLikeCommentNumber(comments.getLikeCommentNumber()+1);
                            commentsMapper.updateById(comments);
                            commentControlsMapper.insert(commentControls);
                            //判断是否开启点赞消息提醒
                            if(comments.getLikeWarn()==1){
                                Users users1 = userMapper.selectById(comments.getUserId());
                                if(users1.getLikeMessageWarn()==1&&!Objects.equals(comments.getUserId(), users1.getId()))
                                {
                                    users1.setLikeAllNumber(users1.getLikeAllNumber()+1);
                                    users1.setAllMessageNumber(users1.getAllMessageNumber()+1);
                                    userMapper.updateById(users1);
                                }
                            }
                        }
                        else {
                            //如果==代表取消喜欢
                            if (Objects.equals(commentControls.getControls(), commentControls1.getControls())) {
                                //减评论喜欢数量
                                Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                                if(comments==null)
                                    return null;

                                //删除喜欢记录
                                LambdaQueryWrapper<Likes> lambdaQueryWrapper=new LambdaQueryWrapper<>();
                                lambdaQueryWrapper.eq(Likes::getUserId,users.getId())
                                                .eq(Likes::getLikeType,2)
                                        .eq(Likes::getFondId,comments.getId());
                                likesMapper.delete(lambdaQueryWrapper);

                                //更新喜欢数量
                                comments.setLikeCommentNumber(comments.getLikeCommentNumber()-1);
                                commentsMapper.updateById(comments);
                                commentControlsMapper.deleteById(commentControls1);

                                if(comments.getLikeWarn()==1){
                                    Users users1 = userMapper.selectById(comments.getUserId());
                                    if(users1.getLikeMessageWarn()==1&&!Objects.equals(comments.getUserId(), users1.getId()))
                                    {
                                        users1.setLikeAllNumber(users1.getLikeAllNumber() == 0 ? 0 : users1.getLikeAllNumber() - 1);
                                        users1.setAllMessageNumber(users1.getAllMessageNumber() == 0 ? 0 : users1.getAllMessageNumber() - 1);
                                        userMapper.updateById(users1);
                                    }
                                }
                            }
                            else //否则更新
                            {
                                commentControls.setId(commentControls1.getId());
                                commentControlsMapper.updateById(commentControls);
                                //增加评论喜欢数量
                                Comments comments = commentsMapper.selectById(commentControls.getCommentId());
                                if(comments==null)
                                    return null;
                                Likes likes=Likes.builder()
                                        .userId(users.getId())
                                        .fondId(comments.getId())
                                        .likeTime(LocalDateTime.now())
                                        .likeType(2)
                                        .likeUserId(comments.getUserId())
                                        .build();
                                likesMapper.insert(likes);
                                comments.setLikeCommentNumber(comments.getLikeCommentNumber()+1);
                                commentsMapper.updateById(comments);

                                if(comments.getLikeWarn()==1)
                                {
                                    Users users1 = userMapper.selectById(comments.getUserId());
                                    if(users1.getLikeMessageWarn()==1&& !Objects.equals(comments.getUserId(), users1.getId()))
                                    {
                                        users1.setLikeAllNumber(users1.getLikeAllNumber() + 1);
                                        users1.setAllMessageNumber(users1.getAllMessageNumber() + 1);
                                        userMapper.updateById(users1);
                                    }
                                }
                            }
                        }
                }
            }
            else return null;

            SelectComment selectComment = new SelectComment();
            // 查询用户是否对该评论操作
            if (commentControls.getUserId() != 0) {

                Comments comments=commentsMapper.selectById(commentControls.getCommentId());
                LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
                commentControlsLambdaQueryWrapper2.eq(CommentControls::getCommentId, commentControls.getCommentId())
                        .eq(CommentControls::getUserId, commentControls.getUserId());
                CommentControls commentControls2 = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper2);
                    selectComment.setCommentControls(commentControls2);

                // 查询评论的用户信息
                UserInfo2 userInfo = new UserInfo2();
                Users upUser = userMapper.selectById(comments.getUserId());
                BeanUtils.copyProperties(upUser, userInfo);
                    selectComment.setUserInfo(userInfo);
                    selectComment.setComments(comments);
            }

            //判断是否是回复评论
            if(selectComment.getComments().getMainCommentId()!=null)
            {
                Comments comments = commentsMapper.selectById(selectComment.getComments().getReplyCommentId());
                selectComment.setReplyUserName(comments.getUserName());
                cacheService.deleteCommentCacheByVideoId(commentControls.getVideoId(),commentControls.getDynamicId(),commentControls.getUserId());
                cacheService.deleteReplyCommentCacheByCommentId(selectComment.getComments().getVideoId(),selectComment.getComments().getDynamicId(),selectComment.getComments().getMainCommentId(),comments.getUserId());
            }
            else
                cacheService.deleteCommentCacheByVideoId(commentControls.getVideoId(),commentControls.getDynamicId(),commentControls.getUserId());

            return selectComment;
        }
        return null;
    }

    //获取图片后缀
    @Override
    public String getImageFormat(String base64) {
            if (base64.startsWith("data:image/")) {
                // 提取 MIME 类型中的格式
                String[] parts = base64.split(";");
                if (parts.length > 0) {
                    String mimeType = parts[0]; // e.g., "data:image/png"
                    return "."+mimeType.substring(mimeType.indexOf("/") + 1); // 提取 ".png"
                }
            }
            return null;
        }

    @Override
    public List<SelectReplyComment> selectReplyAll(Integer userId, Integer pageNum) {

        Page<Comments> commentsPage=new Page<>(pageNum,10);
        LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        commentsLambdaQueryWrapper
                .eq(Comments::getNotificationReplyFlag,1)
                .ne(Comments::getUserId,userId)
                .eq(Comments::getReplyUserId,userId)
                .orderByDesc(Comments::getCommentTime);

        Page<Comments> commentsPage1 = commentsMapper.selectPage(commentsPage, commentsLambdaQueryWrapper);
        List<Comments> records = commentsPage1.getRecords();
        List<SelectReplyComment> selectReplyCommentList=new ArrayList<>();

        for (Comments record : records) {
            SelectReplyComment selectReplyComment=new SelectReplyComment();
            LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            commentControlsLambdaQueryWrapper.eq(CommentControls::getUserId,userId)
                            .eq(CommentControls::getCommentId,record.getId());
            CommentControls replyCommentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);
            Users users = userMapper.selectById(record.getUserId());
            Comments mainComment = commentsMapper.selectById(record.getReplyCommentId());

            //视频
           if(record.getVideoId()!=null){
               Videos videos = videosMapper.selectById(record.getVideoId());
               if(videos==null)
                   continue;
               if(Objects.equals(videos.getUserId(), userId))
                   selectReplyComment.setIsAuthorFlag(1);
               else selectReplyComment.setIsAuthorFlag(0);

               if(record.getMainCommentId()==null) {
                   selectReplyComment.setCoverAddress(videos.getCoverAddress());
                   selectReplyComment.setVideoTitle(videos.getTitle());
               }
           }
            //动态
           else if (record.getDynamicId()!=null) {
               Dynamic dynamic = dynamicMapper.selectById(record.getDynamicId());
               if(dynamic==null)
                   continue;
               if(Objects.equals(dynamic.getFollowId(), userId))
                   selectReplyComment.setIsAuthorFlag(1);
               else selectReplyComment.setIsAuthorFlag(0);

               if(record.getMainCommentId()==null)
                   selectReplyComment.setDynamic(dynamic);
           }
           else continue;

            selectReplyComment.setId(record.getId());
            selectReplyComment.setUserId(users.getId());
            selectReplyComment.setReplyCommentControls(replyCommentControls);
            selectReplyComment.setAvatarAddress(users.getAvatarAddress());
            selectReplyComment.setUserName(users.getUserName());
            selectReplyComment.setReplyComments(record);
            selectReplyComment.setMainComments(mainComment);
            selectReplyCommentList.add(selectReplyComment);
        }
        return selectReplyCommentList;
    }

    @Override
    @Transactional
    public Boolean deleteCommentNotification(Integer commentId, Integer userId, Integer messageMenu,Integer type) {

        if(messageMenu!=2&&messageMenu!=3&&messageMenu!=4)
            return false;

        if(messageMenu==2)
        {
            Comments comments = commentsMapper.selectById(commentId);
            //确保是回复评论
            if(comments!=null&&Objects.equals(userId, comments.getReplyUserId())) {
                comments.setNotificationReplyFlag(0);
                commentsMapper.updateById(comments);
                return true;
            }
        }
        else if(messageMenu==3)
        {
            At at = atMapper.selectById(commentId);
            if(at==null)
                return false;
            atMapper.deleteById(at);
            return true;
        }
        else if(messageMenu==4)
        {
               LambdaQueryWrapper<Likes> lambdaQueryWrapper=new LambdaQueryWrapper<>();
               lambdaQueryWrapper.eq(Likes::getFondId,commentId)
                       .eq(Likes::getLikeType,type)
                       .eq(Likes::getLikeUserId,userId);
                List<Likes> likes = likesMapper.selectList(lambdaQueryWrapper);
            for (Likes like : likes) {
                like.setDeleteFlag(1);
                likesMapper.updateById(like);
            }
            return true;
        }

        return false;
    }

    @Override
    public List<SelectAt> selectAt(Integer userId, Integer pageNum) {

        Page<At> page=new Page<>(pageNum,10);
        LambdaQueryWrapper<At> atLambdaQueryWrapper=new LambdaQueryWrapper<>();
        atLambdaQueryWrapper.eq(At::getAtUserId,userId)
                .ne(At::getUserId,userId)
                .orderByDesc(At::getId);
        Page<At> atPage = atMapper.selectPage(page, atLambdaQueryWrapper);
        List<At> records = atPage.getRecords();

        List<SelectAt> selectAts=new ArrayList<>();
        for (At record : records) {

            Users users = userMapper.selectById(record.getUserId());
            if(users==null)
                return null;

            Comments atComment = commentsMapper.selectById(record.getCommentId());
            CommentControls commentControls=new CommentControls();
            if(atComment!=null){
                LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                commentControlsLambdaQueryWrapper.eq(CommentControls::getUserId,userId)
                        .eq(CommentControls::getCommentId,atComment.getId());
                 commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);
            }

            SelectAt selectAt=SelectAt.builder()
                    .userName(users.getUserName())
                    .avatarAddress(users.getAvatarAddress())
                    .at(record)
                    .userId(users.getId())
                    .commentControls(commentControls)
                    .comments(atComment)
                    .build();

            //视频
            if(record.getVideoId()!=null)
            {
                Videos videos=videosMapper.selectById(record.getVideoId());
                if(videos==null)
                    continue;
                selectAt.setIsAuthorFlag(videos.getUserId().equals(userId)?1:0);
                if(atComment!=null&&atComment.getMainCommentId()!=null){
                    Comments comments = commentsMapper.selectById(atComment.getReplyCommentId());
                    if(comments.getDeleteSign()==0)
                        selectAt.setMainComments(comments);
                }else {
                    selectAt.setVideos(videos);
                }
            }
            //动态
            else if (record.getDynamicId()!=null) {
                Dynamic dynamic = dynamicMapper.selectById(record.getDynamicId());
                if(dynamic==null)
                    continue;
                selectAt.setIsAuthorFlag(dynamic.getFollowId().equals(userId)?1:0);
                selectAt.setDynamic(dynamic);
            }
            else continue;

            selectAts.add(selectAt);
        }
        return selectAts;
    }

    @Override
    public List<SelectLike> selectLike(Integer userId, Integer pageNum) {

        Page<Likes> likesPage=new Page<>(pageNum,10);
        LambdaQueryWrapper<Likes> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(Likes::getLikeUserId, userId)
                .ne(Likes::getUserId, userId)
                .eq(Likes::getDeleteFlag, 0)
                .groupBy(Likes::getFondId)  // 按 fond_id 分组，保证唯一
                .orderByDesc(Likes::getLikeTime);

        Page<Likes> likesPage1 = likesMapper.selectPage(likesPage, lambdaQueryWrapper);
        List<Likes> records = likesPage1.getRecords();

        List<SelectLike> selectLikeList=new ArrayList<>();
        for (Likes record : records) {

            SelectLike selectLike=new SelectLike();
            //类型是视频
            if(record.getLikeType()==1)
            {
                Videos videos = videosMapper.selectById(record.getFondId());
                if(videos!=null)
                    selectLike.setVideos(videos);
                else continue;

                //查询点赞用户
                LambdaQueryWrapper<Likes> lambdaQueryWrapper2=new LambdaQueryWrapper<>();
                lambdaQueryWrapper2.eq(Likes::getLikeUserId,userId)
                        .ne(Likes::getUserId,userId)
                        .eq(Likes::getFondId,record.getFondId())
                        .eq(Likes::getLikeType,1)
                        .last("LIMIT 2")
                        .orderByDesc(Likes::getLikeTime);
                List<Likes> likes = likesMapper.selectList(lambdaQueryWrapper2);
                List<UserInfo2> userInfo2List=new ArrayList<>();
                for (Likes like : likes) {
                    Users users = userMapper.selectById(like.getUserId());
                    UserInfo2 userInfo2=new UserInfo2();
                    BeanUtils.copyProperties(users,userInfo2);
                    userInfo2List.add(userInfo2);
                }
                selectLike.setUserInfoList(userInfo2List);
                selectLike.setId(record.getId());
                selectLike.setLikeTime(record.getLikeTime());
            }
            //类型是评论
            else if(record.getLikeType()==2){
                Comments comments = commentsMapper.selectById(record.getFondId());
                if(comments!=null)
                    selectLike.setComments(comments);
                else continue;

                //查询点赞用户
                LambdaQueryWrapper<Likes> lambdaQueryWrapper2=new LambdaQueryWrapper<>();
                lambdaQueryWrapper2.eq(Likes::getLikeUserId,userId)
                        .ne(Likes::getUserId,userId)
                        .eq(Likes::getFondId,record.getFondId())
                        .eq(Likes::getLikeType,2)
                        .last("LIMIT 2")
                        .orderByDesc(Likes::getLikeTime);
                List<Likes> likes = likesMapper.selectList(lambdaQueryWrapper2);
                List<UserInfo2> userInfo2List=new ArrayList<>();
                for (Likes like : likes) {
                    Users users = userMapper.selectById(like.getUserId());
                    UserInfo2 userInfo2=new UserInfo2();
                    BeanUtils.copyProperties(users,userInfo2);
                    userInfo2List.add(userInfo2);
                }
                selectLike.setUserInfoList(userInfo2List);
                selectLike.setLikeTime(record.getLikeTime());
                selectLike.setId(record.getId());
            }
            //类型是动态
            else if (record.getLikeType()==3) {
                Dynamic dynamic = dynamicMapper.selectById(record.getFondId());
                if(dynamic!=null)
                    selectLike.setDynamic(dynamic);
                else continue;

                //查询点赞用户
                LambdaQueryWrapper<Likes> lambdaQueryWrapper2=new LambdaQueryWrapper<>();
                lambdaQueryWrapper2.eq(Likes::getLikeUserId,userId)
                        .ne(Likes::getUserId,userId)
                        .eq(Likes::getFondId,record.getFondId())
                        .eq(Likes::getLikeType,3)
                        .last("LIMIT 2")
                        .orderByDesc(Likes::getLikeTime);
                List<Likes> likes = likesMapper.selectList(lambdaQueryWrapper2);
                List<UserInfo2> userInfo2List=new ArrayList<>();
                for (Likes like : likes) {
                    Users users = userMapper.selectById(like.getUserId());
                    UserInfo2 userInfo2=new UserInfo2();
                    BeanUtils.copyProperties(users,userInfo2);
                    userInfo2List.add(userInfo2);
                }
                selectLike.setUserInfoList(userInfo2List);
                selectLike.setLikeTime(record.getLikeTime());
                selectLike.setId(record.getId());
            }

            selectLike.setLikes(record);
            selectLikeList.add(selectLike);
        }
        return selectLikeList;
    }

    @Override
    @Transactional
    public Boolean dndLikeNotification(Integer userId, Integer id, Integer type) {

        //视频
        if(type==1)
        {
            Videos videos = videosMapper.selectById(id);
            if(videos==null)
                return false;
            videos.setLikeWarn(videos.getLikeWarn()==0?1:0);
            videosMapper.updateById(videos);
        }
        //评论
        else if(type==2){
            Comments comments = commentsMapper.selectById(id);
            comments.setLikeWarn(comments.getLikeWarn()==0?1:0);
            commentsMapper.updateById(comments);
        }
        //动态
        else if (type==3) {
            Dynamic dynamic = dynamicMapper.selectById(id);
            dynamic.setLikeWarn(dynamic.getLikeWarn()==0?1:0);
            dynamicMapper.updateById(dynamic);
        }
        return true;
    }

    @Override
    public List<SelectLike> selectLikeUser(Integer userId, Integer id,Integer type, Integer pageNum) {

        Page<Likes> page=new Page<>(pageNum,10);
        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getLikeUserId,userId)
                .eq(Likes::getFondId,id)
                .eq(Likes::getLikeType,type)
                .orderByDesc(Likes::getLikeTime);
        Page<Likes> likesPage = likesMapper.selectPage(page, likesLambdaQueryWrapper);
        List<Likes> records = likesPage.getRecords();

        List<SelectLike> selectLikeList=new ArrayList<>();
        for (Likes record : records) {

            Users users = userMapper.selectById(record.getUserId());
            if(users==null)
                return null;
            UserInfo2 userInfo2=new UserInfo2();
            BeanUtils.copyProperties(users,userInfo2);

            LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
            fansLambdaQueryWrapper.eq(Fans::getUserId,record.getUserId())
                    .eq(Fans::getFansId,userId);
            Fans fans = fansMapper.selectOne(fansLambdaQueryWrapper);

            SelectLike selectLike=SelectLike
                    .builder()
                    .userInfo2(userInfo2)
                    .id(record.getId())
                    .likeTime(record.getLikeTime())
                    .isFansFlag(fans==null?0:1)
                    .build();
            selectLikeList.add(selectLike);
        }
        return selectLikeList;
    }

    @Override
    @Transactional
    public Boolean putUpStatus(Integer userId, Integer id) {

        Comments comments = commentsMapper.selectById(id);
        if(comments==null)
            return false;

        //必须是主评论才能置顶
        if(comments.getMainCommentId()!=null)
            return false;

        //判断置顶状态
        if(comments.getUpFlag()==0)
        {
            comments.setUpFlag(1);
            commentsMapper.updateById(comments);

            //如果有该视频下的置顶状态修改0
            LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            commentsLambdaQueryWrapper.eq(Comments::getVideoId,comments.getVideoId())
                    .eq(Comments::getUpFlag,1)
                    .isNull(Comments::getMainCommentId)
                    .eq(Comments::getDeleteSign,0)
                    .ne(Comments::getId,comments.getId());
            Comments comments1 = commentsMapper.selectOne(commentsLambdaQueryWrapper);
            if(comments1!=null) {
                comments1.setUpFlag(0);
                commentsMapper.updateById(comments1);
            }
        }
        else if(comments.getUpFlag()==1){
            comments.setUpFlag(0);
            commentsMapper.updateById(comments);
        }
        cacheService.deleteCommentCacheByVideoId(comments.getVideoId(),comments.getDynamicId(),userId);

        return true;
    }

    @Override
    public SelectComment selectOneComment(Integer userId, Integer mainCommentId, Integer replyCommentId) {

        Comments comments = commentsMapper.selectById(mainCommentId);
        if(comments==null||comments.getDeleteSign()==1||comments.getMainCommentId()!=null)
            return null;

        Users users = userMapper.selectById(comments.getUserId());
        UserInfo2 userInfo2=new UserInfo2();
        BeanUtils.copyProperties(users,userInfo2);

        LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        commentControlsLambdaQueryWrapper.eq(CommentControls::getCommentId,mainCommentId)
                .eq(CommentControls::getUserId,userId);
        CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

        Users replyUser = userMapper.selectById(comments.getReplyUserId());

        LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        commentsLambdaQueryWrapper.eq(Comments::getMainCommentId,mainCommentId)
                .eq(Comments::getDeleteSign,0);
        Long replyCount = commentsMapper.selectCount(commentsLambdaQueryWrapper);

        ReplyComment replyComment = this.replyComment(userId,mainCommentId,replyCommentId);
        List<ReplyComment> replyCommentList=new ArrayList<>();
        replyCommentList.add(replyComment);

        SelectComment selectComment=SelectComment
                .builder()
                .comments(comments)
                .userInfo(userInfo2)
                .replyCommentList(replyComment!=null?replyCommentList:null)
                .replyNumber(replyCount)
                .replyUserName(replyUser!=null?replyUser.getUserName():null)
                .commentControls(commentControls)
                .build();

        return selectComment;
    }

    public ReplyComment replyComment(Integer userId,Integer mainCommentId,Integer replyCommentId){

        Comments replyComment = commentsMapper.selectById(replyCommentId);

        if(replyComment==null||replyComment.getDeleteSign()==1||!Objects.equals(replyComment.getMainCommentId(), mainCommentId))
            return null;

        Users users = userMapper.selectById(replyComment.getUserId());
        UserInfo2 userInfo2=new UserInfo2();
        BeanUtils.copyProperties(users,userInfo2);

        Users replyUser = userMapper.selectById(replyComment.getReplyUserId());

        LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        commentControlsLambdaQueryWrapper.eq(CommentControls::getCommentId,replyCommentId)
                .eq(CommentControls::getUserId,userId);
        CommentControls replyCommentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

        ReplyComment replyComment1=ReplyComment
                .builder()
                .comments(replyComment)
                .userInfo(userInfo2)
                .replyUserName(replyUser.getUserName())
                .commentControls(replyCommentControls)
                .build();
        return replyComment1;
    }


    //分页查询回复的评论
    @Override
    public Page<ReplyComment> selectReplyById(Integer commentId,Integer userId,Integer pageNum){

        // 创建分页对象
        Page<Comments> page = new Page<>(pageNum, 10);

        LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
         commentsLambdaQueryWrapper.eq(Comments::getMainCommentId,commentId)
                 .lt(Comments::getDynamicFlag,2)
                 .ne(Comments::getDeleteSign,1)
                 .orderByDesc(Comments::getLikeCommentNumber)
                 .orderByDesc(Comments::getCommentTime);
        // 执行分页查询
        Page<Comments> commentsPage = commentsMapper.selectPage(page, commentsLambdaQueryWrapper);
        List<Comments> records = commentsPage.getRecords();

        List<ReplyComment> replyComments=new ArrayList<>();
        for (Comments comment : records) {

            UserInfo2 userInfo2=new UserInfo2();
            Users users = userMapper.selectById(comment.getUserId());
            BeanUtils.copyProperties(users,userInfo2);

            LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            commentControlsLambdaQueryWrapper.eq(CommentControls::getCommentId,comment.getId())
                    .ne(CommentControls::getControls,-1)
                    .eq(CommentControls::getUserId,userId);
            CommentControls commentControls = commentControlsMapper.selectOne(commentControlsLambdaQueryWrapper);

            ReplyComment replyComment=new ReplyComment();
            if(!comment.getMainCommentId().equals(comment.getReplyCommentId())){

                Comments comments1 = commentsMapper.selectById(comment.getReplyCommentId());
                replyComment.setReplyUserName(comments1.getUserName());
            }

            replyComment.setComments(comment);
            replyComment.setUserInfo(userInfo2);
            replyComment.setCommentControls(commentControls);
            replyComments.add(replyComment);
        }

        // 构造返回的分页对象
        Page<ReplyComment> resultPage = new Page<>(pageNum, 10);
        resultPage.setRecords(replyComments);
        resultPage.setTotal(commentsPage.getTotal());
        resultPage.setCurrent(commentsPage.getCurrent());
        resultPage.setSize(commentsPage.getSize());

        return resultPage;
    }


    //处理删除评论后回复评论的数据
    @Override
    public void deleteReply(List<Comments> commentsList,boolean flag,boolean flag2){

        int num=0;
        for (Comments comment : commentsList) {
            //删除评论相关的操作（点赞、踩）
            LambdaQueryWrapper<CommentControls> commentControlsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            commentControlsLambdaQueryWrapper.eq(CommentControls::getCommentId,comment.getId());
            commentControlsMapper.delete(commentControlsLambdaQueryWrapper);
            if(comment.getDeleteSign()==0)
                num++;
            if(comment.getDynamicFlag()==0){

                //删除评论图片
                if(!comment.getImgAddress().isEmpty()){
                    String imgAddress = comment.getImgAddress();
                    String[] split = imgAddress.split(",");
                    for (String commentImg : split) {

                        int lastIndexOf = commentImg.lastIndexOf("/");
                        String commentFile=(lastIndexOf!=-1)? commentImg.substring(lastIndexOf+1):commentImg;
                        Path commentPath= Paths.get(FilePathEnum.UPLOAD_COMMENT_IMG.getPath()+commentFile);
                        try {
                            Files.delete(commentPath);
                        }catch (Exception e){
                            System.out.println(e.getMessage());
                        }
                    }
                }

                //删除该评论
                if(flag2)
                commentsMapper.deleteById(comment);
                else {
                    comment.setDeleteSign(1);
                    comment.setContent(null);
                    commentsMapper.updateById(comment);
                }
            }else if(comment.getDynamicFlag() ==1){

                //更新评论动态标识
                comment.setDynamicFlag(2);
                commentsMapper.updateById(comment);
            }
        }

        //更新视频评论数量
        if(flag&&!commentsList.isEmpty()) {
            Videos videos = videosMapper.selectById(commentsList.get(0).getVideoId());
            if(videos!=null) {
                videos.setCommentNumber(videos.getCommentNumber() - num);
                videosMapper.updateById(videos);
            }
            else{
                Dynamic dynamic = dynamicMapper.selectById(commentsList.get(0).getDynamicId());
                dynamic.setCommentNumber(dynamic.getCommentNumber() - num);
                dynamicMapper.updateById(dynamic);
            }

        }
    }




}
