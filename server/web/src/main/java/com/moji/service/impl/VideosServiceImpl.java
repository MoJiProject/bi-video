package com.moji.service.impl;

import ch.qos.logback.core.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moji.FilePathEnum;
import com.moji.dto.SelectVideoDto;
import com.moji.dto.VideosDto;
import com.moji.mapper.*;
import com.moji.po.*;
import com.moji.service.CacheService;
import com.moji.service.CommentService;
import com.moji.service.VideosService;
import com.moji.vo.SelectVideoByIdVo;
import com.moji.vo.UsersVideosVo;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VideosServiceImpl extends ServiceImpl<VideosMapper, Videos> implements VideosService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private DynamicMapper dynamicMapper;

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private CollectClassifyMapper collectClassifyMapper;

    @Autowired
    private HistoryMapper historyMapper;

    @Autowired
    private ThrowCoinMapper throwCoinMapper;

    @Autowired
    private LikesMapper likesMapper;

    @Autowired
    private ScrollingMapper scrollingMapper;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CacheService cacheService;

    @Override
    public Boolean insertVideo(String videoName, String coverName, Videos videos) {

          String videoAddress="/upload/video/"+videoName;
          String coverAddress="/upload/video/cover/"+coverName;
        String durationString=null;
        try {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(FilePathEnum.UPLOAD_VIDEO.getPath() + videoName)) {
                grabber.start();
                // 获取视频时长（以秒为单位）
                double durationInSeconds = grabber.getLengthInTime() / 1000000.0; // 转换为秒
                durationString = formatDuration(durationInSeconds);
                grabber.stop();
            }
        } catch (Exception e) {
        }

        Users users = userMapper.selectById(videos.getUserId());
         videos.setUserName(users.getUserName());
         videos.setVideoTime(durationString);
         videos.setVideoAddress(videoAddress);
         videos.setCoverAddress(coverAddress);
         videos.setCreateTime(LocalDateTime.now());
         videos.setStatus(0);

        int insert = videosMapper.insert(videos);
        return insert > 0;
    }

    @Override
    public VideosDto selectDtoData(Integer userId) {

        LambdaQueryWrapper<Videos> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(Videos::getStatus,1)
                .eq(Videos::getUserId,userId);

        List<Videos> videos = videosMapper.selectList(wrapper);

        int likeNumbers=0;
        int commentNumbers=0;//收藏数量
        int collectNumbers=0;
        int shareNumbers=0;
        int coinThrowNumbers=0;
        int newFansNumbers=0;//在该视频下新增粉丝的数量
        int scrollingNumbers=0;//弹幕数量
        int playNumbers=0;//播放量

        for (Videos video : videos) {

          likeNumbers=likeNumbers+video.getLikeNumber();
          commentNumbers=commentNumbers+video.getCommentNumber();
          shareNumbers=shareNumbers+video.getShareNumber();
          collectNumbers=collectNumbers+video.getCollectNumber();
          coinThrowNumbers=coinThrowNumbers+video.getCoinThrowNumber();
          newFansNumbers=newFansNumbers+video.getNewFansNumber();
          scrollingNumbers=scrollingNumbers+video.getScrollingNumber();
          playNumbers=playNumbers+video.getPlayNumber();
        }
        VideosDto videosDto=new VideosDto();
        videosDto.setUserId(userId);
        videosDto.setCollectNumber(collectNumbers);
        videosDto.setCommentNumber(commentNumbers);
        videosDto.setLikeNumber(likeNumbers);
        videosDto.setPlayNumber(playNumbers);
        videosDto.setCoinThrowNumber(coinThrowNumbers);
        videosDto.setScrollingNumber(scrollingNumbers);
        videosDto.setNewFansNumber(newFansNumbers);
        videosDto.setShareNumber(shareNumbers);
        return videosDto;
    }

    @Override
    public UsersVideosVo selectByUserIdVideo(Integer userId, String videoTitle, String subZoneKey, String sortWay, Integer videoStatus, Integer pageNum) {
        long videoAllNumber;
        long videoSuccessNumber;
        long videoErrorNumber;
        long videoWaitNumber;
        UsersVideosVo usersVideosVo=new UsersVideosVo();
        Page<Videos> videosPage=new Page<>(pageNum,5);

        Users user = userMapper.selectById(userId);

        LambdaQueryWrapper<Videos> videoStatusWrapper0=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper0.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper0.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper0.eq(Videos::getUserId,userId);

        videoAllNumber=videosMapper.selectCount(videoStatusWrapper0);


        LambdaQueryWrapper<Videos> videoStatusWrapper=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper.eq(Videos::getUserId,userId);
        videoStatusWrapper.eq(Videos::getStatus,0);

        videoWaitNumber=videosMapper.selectCount(videoStatusWrapper);

        LambdaQueryWrapper<Videos> videoStatusWrapper1=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper1.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper1.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper1.eq(Videos::getUserId,userId);
        videoStatusWrapper1.eq(Videos::getStatus,1);

        videoSuccessNumber=videosMapper.selectCount(videoStatusWrapper1);

        LambdaQueryWrapper<Videos> videoStatusWrapper2=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper2.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper2.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper2.eq(Videos::getUserId,userId);
        videoStatusWrapper2.eq(Videos::getStatus,2);

        videoErrorNumber=videosMapper.selectCount(videoStatusWrapper2);


        LambdaQueryWrapper<Videos> userByIdWrapper=new LambdaQueryWrapper<>();

        userByIdWrapper.eq(Videos::getUserId,userId);
        //判断查找作品的状态
        if (videoStatus==-1){

            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==0){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);

            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==1){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);
            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==2){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);
            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }
        UserInfo2 userInfo=new UserInfo2();
        BeanUtils.copyProperties(user,userInfo);

        Page<Videos> videosPage1 = videosMapper.selectPage(videosPage, userByIdWrapper);
        usersVideosVo.setVideos(videosPage1.getRecords());
        usersVideosVo.setUsers(userInfo);
        usersVideosVo.setVideoWaitNumber(videoWaitNumber);
        usersVideosVo.setVideoSuccessNumber(videoSuccessNumber);
        usersVideosVo.setVideoErrorNumber(videoErrorNumber);
        usersVideosVo.setVideoAllNumber(videoAllNumber);
        usersVideosVo.setTotal(videosPage1.getTotal());

        return usersVideosVo;
    }
    @Override
    public UsersVideosVo selectByUserExamineVideo(Integer userId, String videoTitle, String subZoneKey, String sortWay, Integer videoStatus,Integer pageNum) {

        Page<Videos> videosPage=new Page<>(pageNum,5);

        long videoAllNumber;
        long videoSuccessNumber;
        long videoErrorNumber;
        long videoWaitNumber;
        UsersVideosVo usersVideosVo=new UsersVideosVo();

        Users user = userMapper.selectById(userId);

        LambdaQueryWrapper<Videos> videoStatusWrapper0=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper0.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper0.eq(Videos::getSubZoneKey,subZoneKey);
        }
        if(userId!=1&&userId!=2)
            videoStatusWrapper0.eq(Videos::getUserId,userId);

        videoAllNumber=videosMapper.selectCount(videoStatusWrapper0);


        LambdaQueryWrapper<Videos> videoStatusWrapper=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper.eq(Videos::getSubZoneKey,subZoneKey);
        }
        if(userId!=1&&userId!=2)
            videoStatusWrapper.eq(Videos::getUserId,userId);
        videoStatusWrapper.eq(Videos::getStatus,0);

        videoWaitNumber=videosMapper.selectCount(videoStatusWrapper);

        LambdaQueryWrapper<Videos> videoStatusWrapper1=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper1.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper1.eq(Videos::getSubZoneKey,subZoneKey);
        }
        if(userId!=1&&userId!=2)
            videoStatusWrapper1.eq(Videos::getUserId,userId);
        videoStatusWrapper1.eq(Videos::getStatus,1);

        videoSuccessNumber=videosMapper.selectCount(videoStatusWrapper1);

        LambdaQueryWrapper<Videos> videoStatusWrapper2=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper2.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分区")){
            videoStatusWrapper2.eq(Videos::getSubZoneKey,subZoneKey);
        }
        if(userId!=1&&userId!=2)
            videoStatusWrapper2.eq(Videos::getUserId,userId);
        videoStatusWrapper2.eq(Videos::getStatus,2);

        videoErrorNumber=videosMapper.selectCount(videoStatusWrapper2);


        LambdaQueryWrapper<Videos> userByIdWrapper=new LambdaQueryWrapper<>();
        if(userId!=1&&userId!=2)
            userByIdWrapper.eq(Videos::getUserId,userId);
        //判断查找作品的状态
        if (videoStatus==-1){

            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==0){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);

            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==1){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);
            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==2){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);
            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分区")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "投稿时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放数排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏数排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕数排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }
        UserInfo2 userInfo=new UserInfo2();
        BeanUtils.copyProperties(user,userInfo);
        Page<Videos> videosPage1 = videosMapper.selectPage(videosPage, userByIdWrapper);
        usersVideosVo.setVideos(videosPage1.getRecords());
        usersVideosVo.setUsers(userInfo);
        usersVideosVo.setVideoWaitNumber(videoWaitNumber);
        usersVideosVo.setVideoSuccessNumber(videoSuccessNumber);
        usersVideosVo.setVideoErrorNumber(videoErrorNumber);
        usersVideosVo.setVideoAllNumber(videoAllNumber);
        usersVideosVo.setTotal(videosPage1.getTotal());

        return usersVideosVo;
    }

    @Override
    @Transactional
    public Boolean deleteVideo(Integer videoId) {

        if (videoId==null)
            return false;

        Videos video = videosMapper.selectById(videoId);
        String coverAddress = video.getCoverAddress();
        String videoAddress = video.getVideoAddress();

        int indexOf = coverAddress.lastIndexOf('/');
        int indexOf1 = videoAddress.lastIndexOf('/');
        String coverFile=(indexOf!=-1)? coverAddress.substring(indexOf+1):coverAddress;
        String videoFile=(indexOf1!=-1)? videoAddress.substring(indexOf1+1):videoAddress;

        Path coverPath= Paths.get(FilePathEnum.UPLOAD_VIDEO_COVER.getPath()+coverFile);
        Path videoPath= Paths.get(FilePathEnum.UPLOAD_VIDEO.getPath()+videoFile);

        int i = videosMapper.deleteById(videoId);
        if (i>0)
        {
            if(video.getStatus()==1){
                Users users = userMapper.selectById(video.getUserId());
                users.setVideoNumber(users.getVideoNumber()-1);
                users.setOwnDynamicNumber(users.getOwnDynamicNumber()-1);
                userMapper.updateById(users);
            }
            //删除动态
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getVideoId,videoId)
                    .isNull(Dynamic::getCommentId);
            dynamicMapper.delete(dynamicLambdaQueryWrapper);
            //删除收藏
            LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsLambdaQueryWrapper.eq(Collects::getVideoId,videoId);
            List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);
            for (Collects collect : collects) {
                collect.setDeleteFlag(1);
                collectMapper.updateById(collect);
            }
            //删除历史
            LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            historyLambdaQueryWrapper.eq(History::getVideoId,videoId);
            historyMapper.delete(historyLambdaQueryWrapper);
            //删除喜欢
            LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
            likesLambdaQueryWrapper.eq(Likes::getFondId,videoId)
                    .eq(Likes::getLikeType,1);
            likesMapper.delete(likesLambdaQueryWrapper);
            //删除投币
            LambdaQueryWrapper<ThrowCoin> throwCoinLambdaQueryWrapper=new LambdaQueryWrapper<>();
            throwCoinLambdaQueryWrapper.eq(ThrowCoin::getVideoId,videoId);
            throwCoinMapper.delete(throwCoinLambdaQueryWrapper);
            //删除弹幕
            LambdaQueryWrapper<Scrolling> scrollingLambdaQueryWrapper=new LambdaQueryWrapper<>();
            scrollingLambdaQueryWrapper.eq(Scrolling::getVideoId,videoId);
            List<Scrolling> scrollings = scrollingMapper.selectList(scrollingLambdaQueryWrapper);
            if(!scrollings.isEmpty())
             for (Scrolling scrolling : scrollings) {
                scrollingMapper.deleteById(scrolling);
            }
            //删除评论
            LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            commentsLambdaQueryWrapper.eq(Comments::getVideoId,videoId);
            List<Comments> comments = commentsMapper.selectList(commentsLambdaQueryWrapper);
            commentService.deleteReply(comments,false,true);

            int flag=0;
            try{
                Files.delete(coverPath);
                Files.delete(videoPath);
                flag=1;
            }catch (IOException e){
            }
            if (flag==0) {
                throw new RuntimeException("视频或视频封面删除失败");
            }

            cacheService.deleteCommentCacheByVideoId(videoId,null,null);
            cacheService.deleteReplyCommentCacheByCommentId(videoId,null,null,null);

            return true;
        }
        else return i > 0;
    }

    @Override
    @Transactional
    public Boolean updateVideo(String videoName, String coverName, Videos videos, Boolean vFlag, Boolean cFlag) {

        String videoAddress="/upload/video/"+videoName;
        String coverAddress="/upload/video/cover/"+coverName;
        String durationString=null;
        try {
            try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(FilePathEnum.UPLOAD_VIDEO.getPath() + videoName)) {
                grabber.start();
                // 获取视频时长（以秒为单位）
                double durationInSeconds = grabber.getLengthInTime() / 1000000.0; // 转换为秒
                durationString = formatDuration(durationInSeconds);
                grabber.stop();
            }
        } catch (Exception e) {
        }
        Videos videos1 = videosMapper.selectById(videos.getId());
        videos.setVideoTime(durationString);
        if (vFlag)
         videos.setVideoAddress(videoAddress);
        if (cFlag)
         videos.setCoverAddress(coverAddress);
        videos.setExamineFiledMessage(null);
        videos.setCreateTime(LocalDateTime.now());
        videos.setCreateTime(videos1.getCreateTime());
        videos.setStatus(0);
        videos.setId(videos1.getId());

        if(videos1.getStatus()==1){
            Users users = userMapper.selectById(videos.getUserId());
            users.setVideoNumber(users.getVideoNumber()-1);
            users.setOwnDynamicNumber(users.getOwnDynamicNumber()-1);
            userMapper.updateById(users);

            //删除收藏
            LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsLambdaQueryWrapper.eq(Collects::getVideoId,videos1.getId());
            List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);
            for (Collects collect : collects) {
                collect.setDeleteFlag(1);
                collectMapper.updateById(collect);
            }

            //清除动态
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getVideoId,videos1.getId())
                    .isNull(Dynamic::getCommentId);
            dynamicMapper.delete(dynamicLambdaQueryWrapper);
        }

        int i = videosMapper.updateById(videos);
        return i > 0;
    }

    @Override
    public List<SelectVideoDto> getVideo(Integer userId, Integer sort, Integer pageNum) {

        Page<Videos> videosPage=new Page<>(pageNum,10);
        LambdaQueryWrapper<Videos> videoWrapper=new LambdaQueryWrapper<>();
        videoWrapper.eq(Videos::getStatus,1);

        //1升序视频标题
        if(sort == 1){
            videoWrapper.orderByDesc(Videos::getTitle);
        }
        //2降序视频标题
        else if (sort == 2) {
            videoWrapper.orderByAsc(Videos::getTitle);
        }
        //3升序播放量
        else if (sort == 3) {
            videoWrapper.orderByDesc(Videos::getPlayNumber);
        }
        //4降序播放量
        else if (sort == 4) {
            videoWrapper.orderByAsc(Videos::getPlayNumber);
        }
        //5升序发布时间
        else if (sort == 5) {
            videoWrapper.orderByDesc(Videos::getCreateTime);
        }
        //6降序发布时间
        else if (sort == 6) {
            videoWrapper.orderByAsc(Videos::getCreateTime);
        }

        Page<Videos> videosPage1 = videosMapper.selectPage(videosPage, videoWrapper);
        List<Videos> videos = videosPage1.getRecords();
        List<SelectVideoDto> selectVideoDto;
        if(userId!=0)
            selectVideoDto = getSelectVideoDto(videos,userId,false);
        else {
            selectVideoDto = getSelectVideoDto(videos);
        }
        Collections.shuffle(selectVideoDto);
        return selectVideoDto;
    }

    @Override
    public List<SelectVideoDto> getSelectVideoDto(List<Videos> videos, Integer userId,boolean flag) {

        List<SelectVideoDto> videoDtos=new ArrayList<>();
        for (Videos video : videos) {

            String second = null;
            String minutes = null;
            String hour = null;
            Users users = userMapper.selectById(video.getUserId());

            String time = String.valueOf(video.getVideoTime());
            String[] parts = time.split(":"); // 拆分字符串

            if (parts.length == 2) { // 确保拆分成功
                minutes = parts[0]; // 获取小时
                second = parts[1]; // 获取分钟
            }
            if (parts.length == 3) { // 确保拆分成功
                hour = parts[0]; // 获取小时
                minutes = parts[1]; // 获取分钟
                second = parts[2];
            }

            String substring;
            Duration duration = Duration.between(video.getCreateTime(), LocalDateTime.now());

            if (duration.toDays() < 1) {
                if (duration.toHours() < 1) {
                    if (duration.toMinutes() < 1) {
                        substring = "1分钟前";
                    } else {
                        substring = duration.toMinutes() + "分钟前";
                    }
                } else {
                    substring = duration.toHours() + "小时前";
                }
            } else if (duration.toDays() == 1) {
                substring = "昨天";
            } else if (duration.toDays() == 2) {
                substring = "前天";
            }
              else{
                int nowYear=LocalDateTime.now().getYear();
                LocalDateTime startTIme=LocalDateTime.of(nowYear,1,1,0,0,0);
                LocalDateTime endTIme=LocalDateTime.of(nowYear,12,31,23,59,59);
                if(video.getCreateTime().isAfter(startTIme)&&video.getCreateTime().isBefore(endTIme) ) {
                    String createTime = String.valueOf(video.getCreateTime());
                    substring = createTime.charAt(5) == '0' ? createTime.substring(6, 10) : createTime.substring(5, 10);
                }
                else
                    substring= String.valueOf(video.getCreateTime()).substring(0,10);

            }

          LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
          collectsLambdaQueryWrapper.eq(Collects::getVideoId,video.getId())
                  .eq(Collects::getUserId,userId)
                  .eq(Collects::getCollectName,"稍后再看");

            Collects collects = collectMapper.selectOne(collectsLambdaQueryWrapper);
            int waitWatch=0;
            if(collects!=null&&userId!=null)
                waitWatch=1;

            SelectVideoDto selectVideoDto=SelectVideoDto.builder()
                    .videoId(video.getId())
                    .userId(video.getUserId())
                    .second(second)
                    .minutes(minutes)
                    .hour(hour)
                    .waitWatch(waitWatch)
                    .createTime(substring)
                    .videoTime(video.getVideoTime())
                    .coverAddress(video.getCoverAddress())
                    .videoTitle(video.getTitle())
                    .content(video.getContent())
                    .videoShareNumber(video.getShareNumber())
                    .videoCommentNumber(video.getCommentNumber())
                    .videoLikeNumber(video.getLikeNumber())
                    .contentHtml(video.getContentHtml())
                    .userName(users.getUserName())
                    .videoPlayNumber(video.getPlayNumber())
                    .videoScrollingNumber(video.getScrollingNumber())
                    .videoAddress(video.getVideoAddress())
                    .collectNumber(video.getCollectNumber())
                    .build();

            if(flag){
                LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
                historyLambdaQueryWrapper.eq(History::getVideoId,video.getId())
                        .eq(History::getUserId,userId);
                History history = historyMapper.selectOne(historyLambdaQueryWrapper);
                if(history!=null)
                    selectVideoDto.setWatchCurrentTime(history.getWatchCurrentTime());
            }

            videoDtos.add(selectVideoDto);
        }

        return videoDtos;
    }

    @Override
    public List<SelectVideoDto> getSelectVideoDto2(List<Videos> videos, Integer userId,boolean flag) {

        List<SelectVideoDto> videoDtos=new ArrayList<>();
        for (Videos video : videos) {

            String second = null;
            String minutes = null;
            String hour = null;
            Users users = userMapper.selectById(video.getUserId());

            String time = String.valueOf(video.getVideoTime());
            String[] parts = time.split(":"); // 拆分字符串

            if (parts.length == 2) { // 确保拆分成功
                minutes = parts[0]; // 获取小时
                second = parts[1]; // 获取分钟
            }
            if (parts.length == 3) { // 确保拆分成功
                hour = parts[0]; // 获取小时
                minutes = parts[1]; // 获取分钟
                second = parts[2];
            }

            String substring;
            Duration duration = Duration.between(video.getCreateTime(), LocalDateTime.now());

            if (duration.toDays() < 1) {
                if (duration.toHours() < 1) {
                    if (duration.toMinutes() < 1) {
                        substring = "1分钟前";
                    } else {
                        substring = duration.toMinutes() + "分钟前";
                    }
                } else {
                    substring = duration.toHours() + "小时前";
                }
            } else if (duration.toDays() == 1) {
                substring = "昨天";
            } else if (duration.toDays() == 2) {
                substring = "前天";
            }
            else{
                int nowYear=LocalDateTime.now().getYear();
                LocalDateTime startTIme=LocalDateTime.of(nowYear,1,1,0,0,0);
                LocalDateTime endTIme=LocalDateTime.of(nowYear,12,31,23,59,59);
                if(video.getCreateTime().isAfter(startTIme)&&video.getCreateTime().isBefore(endTIme) ) {
                    String createTime = String.valueOf(video.getCreateTime());
                    substring = createTime.substring(5, 10);
                }
                else
                    substring= String.valueOf(video.getCreateTime()).substring(0,10);

            }

            LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsLambdaQueryWrapper.eq(Collects::getVideoId,video.getId())
                    .eq(Collects::getUserId,userId)
                    .eq(Collects::getCollectName,"稍后再看");

            Collects collects = collectMapper.selectOne(collectsLambdaQueryWrapper);
            int waitWatch=0;
            if(collects!=null&&userId!=null)
                waitWatch=1;

            SelectVideoDto selectVideoDto=SelectVideoDto.builder()
                    .videoId(video.getId())
                    .userId(video.getUserId())
                    .second(second)
                    .minutes(minutes)
                    .hour(hour)
                    .waitWatch(waitWatch)
                    .createTime(substring)
                    .videoTime(video.getVideoTime())
                    .coverAddress(video.getCoverAddress())
                    .videoTitle(video.getTitle())
                    .content(video.getContent())
                    .videoShareNumber(video.getShareNumber())
                    .videoCommentNumber(video.getCommentNumber())
                    .videoLikeNumber(video.getLikeNumber())
                    .contentHtml(video.getContentHtml())
                    .userName(users.getUserName())
                    .videoPlayNumber(video.getPlayNumber())
                    .videoScrollingNumber(video.getScrollingNumber())
                    .videoAddress(video.getVideoAddress())
                    .collectNumber(video.getCollectNumber())
                    .build();

            if(flag){
                LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
                historyLambdaQueryWrapper.eq(History::getVideoId,video.getId())
                        .eq(History::getUserId,userId);
                History history = historyMapper.selectOne(historyLambdaQueryWrapper);
                if(history!=null)
                    selectVideoDto.setWatchCurrentTime(history.getWatchCurrentTime());
            }

            videoDtos.add(selectVideoDto);
        }

        return videoDtos;
    }

    @Override
    public SelectVideoDto getSelectVideo(Videos video, Integer userId,boolean flag) {

            String second = null;
            String minutes = null;
            String hour = null;
            Users users = userMapper.selectById(video.getUserId());

            String time = String.valueOf(video.getVideoTime());
            String[] parts = time.split(":"); // 拆分字符串

            if (parts.length == 2) { // 确保拆分成功
                minutes = parts[0]; // 获取小时
                second = parts[1]; // 获取分钟
            }
            if (parts.length == 3) { // 确保拆分成功
                hour = parts[0]; // 获取小时
                minutes = parts[1]; // 获取分钟
                second = parts[2];
            }

            String substring;
            Duration duration = Duration.between(video.getCreateTime(), LocalDateTime.now());

            if (duration.toDays() < 1) {
                if (duration.toHours() < 1) {
                    if (duration.toMinutes() < 1) {
                        substring = "1分钟前";
                    } else {
                        substring = duration.toMinutes() + "分钟前";
                    }
                } else {
                    substring = duration.toHours() + "小时前";
                }
            } else if (duration.toDays() == 1) {
                substring = "昨天";
            } else if (duration.toDays() == 2) {
                substring = "前天";
            }
            else{
                int nowYear=LocalDateTime.now().getYear();
                LocalDateTime startTIme=LocalDateTime.of(nowYear,1,1,0,0,0);
                LocalDateTime endTIme=LocalDateTime.of(nowYear,12,31,23,59,59);
                if(video.getCreateTime().isAfter(startTIme)&&video.getCreateTime().isBefore(endTIme) ) {
                    String createTime = String.valueOf(video.getCreateTime());
                    substring = createTime.substring(5, 10);
                }
                else
                    substring= String.valueOf(video.getCreateTime()).substring(0,10);

            }

            int waitWatch=0;
            if(userId!=0){
                LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectsLambdaQueryWrapper.eq(Collects::getVideoId,video.getId())
                        .eq(Collects::getUserId,userId)
                        .eq(Collects::getCollectName,"稍后再看");

                Collects collects = collectMapper.selectOne(collectsLambdaQueryWrapper);

                if(collects!=null&&userId!=null)
                    waitWatch=1;
            }

            SelectVideoDto selectVideoDto=SelectVideoDto.builder()
                    .videoId(video.getId())
                    .userId(video.getUserId())
                    .second(second)
                    .minutes(minutes)
                    .hour(hour)
                    .waitWatch(waitWatch)
                    .createTime(substring)
                    .videoTime(video.getVideoTime())
                    .coverAddress(video.getCoverAddress())
                    .videoTitle(video.getTitle())
                    .content(video.getContent())
                    .videoShareNumber(video.getShareNumber())
                    .videoCommentNumber(video.getCommentNumber())
                    .videoLikeNumber(video.getLikeNumber())
                    .contentHtml(video.getContentHtml())
                    .userName(users.getUserName())
                    .videoPlayNumber(video.getPlayNumber())
                    .videoScrollingNumber(video.getScrollingNumber())
                    .videoAddress(video.getVideoAddress())
                    .collectNumber(video.getCollectNumber())
                    .build();

        if(flag){
            LambdaQueryWrapper<History> historyLambdaQueryWrapper=new LambdaQueryWrapper<>();
            historyLambdaQueryWrapper.eq(History::getVideoId,video.getId())
                    .eq(History::getUserId,userId);
            History history = historyMapper.selectOne(historyLambdaQueryWrapper);
            if(history!=null)
                selectVideoDto.setWatchCurrentTime(history.getWatchCurrentTime());
        }

        return selectVideoDto;
    }
    @Override
    public List<SelectVideoDto> getSelectVideoDto(List<Videos> videos) {

        List<SelectVideoDto> videoDtos=new ArrayList<>();
        for (Videos video : videos) {

            String second=null;
            String minutes=null;
            String hour=null;
            Users users = userMapper.selectById(video.getUserId());

            String time = String.valueOf(video.getVideoTime());
            String[] parts = time.split(":"); // 拆分字符串

            if (parts.length == 2) { // 确保拆分成功
                minutes = parts[0]; // 获取小时
                second = parts[1]; // 获取分钟
            }
            if (parts.length == 3) { // 确保拆分成功
                hour = parts[0]; // 获取小时
                minutes = parts[1]; // 获取分钟
                second=parts[2];
            }

            String substring;
            Duration duration=Duration.between(video.getCreateTime(),LocalDateTime.now());
            if (duration.toDays()<1){

                if(duration.toHours()<1){
                    if(duration.toMinutes()<1)
                        substring="1分钟前";
                    else
                        substring=duration.toMinutes()+"分钟前";
                }
                else
                    substring=duration.toHours()+"小时前";

            } else if (duration.toHours()<2) {

                substring="昨天";

            }else if (duration.toHours()<3) {

                substring="前天";
            }

            else{
                int nowYear=LocalDateTime.now().getYear();
                LocalDateTime startTIme=LocalDateTime.of(nowYear,1,1,0,0,0);
                LocalDateTime endTIme=LocalDateTime.of(nowYear,12,31,23,59,59);
                if(video.getCreateTime().isAfter(startTIme)&&video.getCreateTime().isBefore(endTIme) ) {
                    String createTime = String.valueOf(video.getCreateTime());
                    substring = createTime.charAt(5) == '0' ? createTime.substring(6, 10) : createTime.substring(5, 10);
                }
                else
                    substring= String.valueOf(video.getCreateTime()).substring(0,10);

            }

            SelectVideoDto selectVideoDto=SelectVideoDto.builder()
                    .videoId(video.getId())
                    .userId(video.getUserId())
                    .second(second)
                    .minutes(minutes)
                    .hour(hour)
                    .videoTime(video.getVideoTime())
                    .createTime(substring)
                    .coverAddress(video.getCoverAddress())
                    .videoTitle(video.getTitle())
                    .userName(users.getUserName())
                    .videoPlayNumber(video.getPlayNumber())
                    .videoScrollingNumber(video.getScrollingNumber())
                    .videoAddress(video.getVideoAddress())
                    .collectNumber(video.getCollectNumber())
                    .build();

            videoDtos.add(selectVideoDto);

        }

        return videoDtos;

    }

    @Override
    public Boolean examineVideo(int videoId) {

        Videos videos = videosMapper.selectById(videoId);
        videos.setStatus(1);
        videos.setCreateTime(LocalDateTime.now());
        int i = videosMapper.updateById(videos);

        Users videoUser = userMapper.selectById(videos.getUserId());
        videoUser.setOwnDynamicNumber(videoUser.getOwnDynamicNumber()+1);
        videoUser.setVideoNumber(videoUser.getVideoNumber()+1);
        int i1 = userMapper.updateById(videoUser);

        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Fans::getUserId,videoUser.getId());

        List<Fans> fans = fansMapper.selectList(fansLambdaQueryWrapper);

        for (Fans fan : fans) {

            Users users = userMapper.selectById(fan.getFansId());
            users.setDynamicNumber(users.getDynamicNumber()+1);
            userMapper.updateById(users);

            Dynamic dynamic=Dynamic.builder()
                    .watchDynamicFlag(0)
                    .videoId(videoId)
                    .fansId(fan.getFansId())
                    .followId(videoUser.getId())
                    .fansFlag(1)
                    .build();
            dynamicMapper.insert(dynamic);
        }

        //更新收藏
        LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsLambdaQueryWrapper.eq(Collects::getVideoId,videoId);
        List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);
        for (Collects collect : collects) {
            collect.setDeleteFlag(0);
            collectMapper.updateById(collect);
        }

        Dynamic dynamic=Dynamic
                .builder()
                .videoId(videos.getId())
                .followId(videos.getUserId())
                .publishTime(videos.getCreateTime())
                .build();
        dynamicMapper.insert(dynamic);

        return i > 0 && i1 > 0;
    }

    @Override
    public SelectVideoByIdVo selectByVideoId(Integer videoId, Integer userId){

        boolean likeVideoClickFlag=false;
        boolean videoThrowCoinClickFlag=false;
        boolean videoShareClickFlag=false;
        boolean videoCollectClickFlag=false;

        Videos videos = videosMapper.selectById(videoId);
        if (videos==null||videos.getStatus()!=1)
            return null;
        Users upUsers = userMapper.selectById(videos.getUserId());
        int isFansFlag=0;

        if(userId!=0) {
            LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
            fansLambdaQueryWrapper.eq(Fans::getUserId,videos.getUserId())
                    .eq(Fans::getFansId,userId);
             Fans upFans = fansMapper.selectOne(fansLambdaQueryWrapper);
               if(upFans!=null)
                isFansFlag=1;
        }
        //查询用户是否点赞
        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getFondId,videoId)
                        .eq(Likes::getLikeType,1)
                        .eq(Likes::getUserId,userId);
        if(likesMapper.selectOne(likesLambdaQueryWrapper)!=null)
            likeVideoClickFlag=true;
        //查询用户是否投币
        LambdaQueryWrapper<ThrowCoin> throwCoinLambdaQueryWrapper=new LambdaQueryWrapper<>();
        throwCoinLambdaQueryWrapper.eq(ThrowCoin::getVideoId,videoId)
                .eq(ThrowCoin::getUserId,userId);
        if(throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper)!=null)
            videoThrowCoinClickFlag=true;
        //查询用户是否收藏
        LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsLambdaQueryWrapper.eq(Collects::getVideoId,videoId)
                .eq(Collects::getUserId,userId);
        List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);
        if(collects.size()==1) {
            boolean collectFlag=true;
            for (Collects collect : collects) {
                if(collect.getCollectName().equals("稍后再看"))
                    collectFlag=false;
            }
            if (collectFlag)
             videoCollectClickFlag = true;
        }else if(collects.size()>1)
            videoCollectClickFlag = true;

        UserInfo2 upUserInfo=new UserInfo2();
        BeanUtils.copyProperties(upUsers,upUserInfo);

        return SelectVideoByIdVo.builder()
                .upVideo(videos)
                .upUser(upUserInfo)
                .isFansFlag(isFansFlag)
                .videoThrowCoinClickFlag(videoThrowCoinClickFlag)
                .videoShareClickFlag(videoShareClickFlag)
                .likeVideoClickFlag(likeVideoClickFlag)
                .videoCollectClickFlag(videoCollectClickFlag)
                .build();
    }

    @Override
    @Transactional
    public SelectVideoByIdVo getLTCAxios(SelectVideoByIdVo selectVideoByIdVo) {

       if(selectVideoByIdVo.getControlsType()==1||selectVideoByIdVo.getControlsType()==3)
       {
           //点赞
           if(selectVideoByIdVo.getLikeVideoClickFlag())
           {
               LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
               likesLambdaQueryWrapper.eq(Likes::getFondId,selectVideoByIdVo.getUpVideo().getId())
                       .eq(Likes::getLikeType,1)
                       .eq(Likes::getUserId,selectVideoByIdVo.getUserId());
               if(likesMapper.selectOne(likesLambdaQueryWrapper)==null)
               {
                   Likes likes=Likes.builder()
                           .fondId(selectVideoByIdVo.getUpVideo().getId())
                           .userId(selectVideoByIdVo.getUserId())
                           .likeTime(LocalDateTime.now())
                           .likeType(1)
                           .likeUserId(selectVideoByIdVo.getUpVideo().getUserId())
                           .build();
                   likesMapper.insert(likes);

                   Users users=userMapper.selectById(selectVideoByIdVo.getUpUser().getId());
                   users.setLikeNumber(users.getLikeNumber()+1);
                   //是否开启点赞提醒
                   if(selectVideoByIdVo.getUpVideo().getLikeWarn()==1&&users.getLikeMessageWarn()==1&& !Objects.equals(users.getId(), selectVideoByIdVo.getUserId()))
                   {
                       users.setLikeAllNumber(users.getLikeAllNumber()+1);
                       users.setAllMessageNumber(users.getAllMessageNumber()+1);
                   }
                   userMapper.updateById(users);

                   Videos videos=selectVideoByIdVo.getUpVideo();
                   videos.setLikeNumber(videos.getLikeNumber()+1);
                   videosMapper.updateById(videos);
               }
           }
           else
           {
               LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
               likesLambdaQueryWrapper.eq(Likes::getFondId,selectVideoByIdVo.getUpVideo().getId())
                       .eq(Likes::getLikeType,1)
                       .eq(Likes::getUserId,selectVideoByIdVo.getUserId());
               if(likesMapper.selectOne(likesLambdaQueryWrapper)!=null)
               {

                   likesMapper.deleteById(likesMapper.selectOne(likesLambdaQueryWrapper));

                   Users users=userMapper.selectById(selectVideoByIdVo.getUpUser().getId());
                   users.setLikeNumber(users.getLikeNumber()-1);
                   if(selectVideoByIdVo.getUpVideo().getLikeWarn()==1&&users.getLikeMessageWarn()==1)
                   {
                       users.setLikeAllNumber(users.getLikeAllNumber()>0?users.getLikeAllNumber()-1:0);
                       users.setAllMessageNumber(users.getAllMessageNumber()>0?users.getAllMessageNumber()-1:0);
                   }
                   userMapper.updateById(users);

                   Videos videos=selectVideoByIdVo.getUpVideo();
                   videos.setLikeNumber(videos.getLikeNumber()-1);
                   videosMapper.updateById(videos);
               }
           }
       }
        if(selectVideoByIdVo.getControlsType()==2||selectVideoByIdVo.getControlsType()==3) {
            //投币
            if (selectVideoByIdVo.getVideoThrowCoinClickFlag()) {
                LambdaQueryWrapper<ThrowCoin> throwCoinLambdaQueryWrapper = new LambdaQueryWrapper<>();
                throwCoinLambdaQueryWrapper.eq(ThrowCoin::getVideoId, selectVideoByIdVo.getUpVideo().getId())
                        .eq(ThrowCoin::getUserId, selectVideoByIdVo.getUserId());
                //查询用户硬币是否够1
                Users users = userMapper.selectById(selectVideoByIdVo.getUserId());
                if (throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper) == null && users.getCoinNumber() > 0) {
                    selectVideoByIdVo.setThrowCoinResult(1);

                    ThrowCoin throwCoin = ThrowCoin.builder()
                            .videoId(selectVideoByIdVo.getUpVideo().getId())
                            .userId(selectVideoByIdVo.getUserId())
                            .build();
                    throwCoinMapper.insert(throwCoin);

                    Videos videos = selectVideoByIdVo.getUpVideo();
                    videos.setCoinThrowNumber(videos.getCoinThrowNumber() + selectVideoByIdVo.getThrowCoinNumber());
                    videosMapper.updateById(videos);
                    //用户的
                    users.setCoinNumber(users.getCoinNumber() - selectVideoByIdVo.getThrowCoinNumber());
                    //投币成功没有满级增加经验
                    if (users.getGrade() < 6)
                        users.setExp(users.getExp() + 2);
                    if (users.getExp() >= 100 && users.getGrade() < 5)
                        users.setExp(users.getExp() - 100);
                    else if (users.getExp() >= 100 && users.getGrade() < 6) {
                        users.setExp(100);
                        users.setGrade(6);
                    }
                    userMapper.updateById(users);

                    //up的
                    UserInfo2 upUserInfo = selectVideoByIdVo.getUpUser();
                    Users upUser = userMapper.selectById(upUserInfo.getId());
                    upUser.setCoinNumber(upUser.getCoinNumber() + selectVideoByIdVo.getThrowCoinNumber());

                    if (users.getGrade() < 6)
                        upUser.setExp(users.getExp() + 2);
                    if (upUser.getExp() >= 100 && upUser.getGrade() < 5)
                        upUser.setExp(upUser.getExp() - 100);
                    else if (upUser.getExp() >= 100 && upUser.getGrade() < 6) {
                        upUser.setExp(100);
                        upUser.setGrade(6);
                    }
                    userMapper.updateById(upUser);

                } else if (throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper) == null && users.getCoinNumber() == 0)
                    selectVideoByIdVo.setThrowCoinResult(0);
                else if (throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper) != null)
                    selectVideoByIdVo.setThrowCoinResult(1);
            }
        }
        if(selectVideoByIdVo.getControlsType()==3)
        {
            //收藏
            if(selectVideoByIdVo.getVideoCollectClickFlag())
            {
                LambdaQueryWrapper<Collects> collectLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectLambdaQueryWrapper.eq(Collects::getVideoId,selectVideoByIdVo.getUpVideo().getId())
                        .eq(Collects::getUserId,selectVideoByIdVo.getUserId());
                List<Collects> collects = collectMapper.selectList(collectLambdaQueryWrapper);
                if(collects.isEmpty()||collects.get(0).getCollectName().equals("稍后再看"))
                {
                    collectLambdaQueryWrapper.eq(Collects::getCollectName,"默认收藏夹");
                    if(collectMapper.selectOne(collectLambdaQueryWrapper)==null) {
                        Collects collect = Collects.builder()
                                .videoId(selectVideoByIdVo.getUpVideo().getId())
                                .userId(selectVideoByIdVo.getUserId())
                                .collectName("默认收藏夹")
                                .collectTime(LocalDateTime.now())
                                .build();
                        collectMapper.insert(collect);
                    }
                    LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getCollectName,"默认收藏夹")
                            .eq(CollectsClassify::getUserId,selectVideoByIdVo.getUserId());

                    CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
                    if(collectsClassify==null)
                    {
                        CollectsClassify collectsClassify1=CollectsClassify.builder()
                                .collectName("默认收藏夹")
                                .videoNumber(1)
                                .userId(selectVideoByIdVo.getUserId())
                                .build();
                        collectClassifyMapper.insert(collectsClassify1);
                    }
                    else {

                        collectsClassify.setVideoNumber(collectsClassify.getVideoNumber()+1);
                        collectClassifyMapper.updateById(collectsClassify);
                    }


                    Videos videos=selectVideoByIdVo.getUpVideo();
                    videos.setCollectNumber(videos.getCollectNumber()+1);
                    videosMapper.updateById(videos);
                }
            }
        }

        //更新视频用户信息
        Users upUser = userMapper.selectById(selectVideoByIdVo.getUpUser().getId());
        UserInfo2 upUserInfo=new UserInfo2();
        BeanUtils.copyProperties(upUser,upUserInfo);
        Videos upVideo = videosMapper.selectById(selectVideoByIdVo.getUpVideo().getId());

        return SelectVideoByIdVo.builder()
                .upVideo(upVideo)
                .upUser(upUserInfo)
                .userId(selectVideoByIdVo.getUserId())
                .likeVideoClickFlag(selectVideoByIdVo.getLikeVideoClickFlag())
                .videoThrowCoinClickFlag(selectVideoByIdVo.getVideoThrowCoinClickFlag())
                .videoCollectClickFlag(selectVideoByIdVo.getVideoCollectClickFlag())
                .isFansFlag(selectVideoByIdVo.getIsFansFlag())
                .videoShareClickFlag(selectVideoByIdVo.getVideoShareClickFlag())
                .throwCoinResult(selectVideoByIdVo.getThrowCoinResult())
                .build();
    }

    @Override
    public List<ResponseCollectClassify> getByIdCollectClassify(Integer userId, Integer videoId) {

        LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId);
        List<CollectsClassify> collectsClassifies = collectClassifyMapper.selectList(collectsClassifyLambdaQueryWrapper);
        List<ResponseCollectClassify> newCollectClassify=new ArrayList<>();
        for (CollectsClassify collectsClassify : collectsClassifies) {
            if(!collectsClassify.getCollectName().equals("稍后再看"))
            {
                ResponseCollectClassify responseCollectClassify=new ResponseCollectClassify();
                BeanUtils.copyProperties(collectsClassify,responseCollectClassify);
                LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectsLambdaQueryWrapper.eq(Collects::getUserId,userId)
                        .eq(Collects::getCollectName,collectsClassify.getCollectName())
                        .eq(Collects::getVideoId,videoId);

                Collects collects = collectMapper.selectOne(collectsLambdaQueryWrapper);
                responseCollectClassify.setFlag(collects != null);
                newCollectClassify.add(responseCollectClassify);
            }
        }

        return newCollectClassify;
    }

    @Override
    public List<SelectVideoDto> getVideoPageByVideo(Videos videos) {

        Videos videos1 = videosMapper.selectById(videos.getId());
        if(videos1==null)
            return null;
        Set<Videos> videosList=new HashSet<>();
        if(videos1.getTag()!=null){

            String[] tag = videos1.getTag().split(",");
            for (String s : tag) {

                LambdaQueryWrapper<Videos> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper
                        .eq(Videos::getStatus,1)
                        .like(Videos::getTag, s);

                List<Videos> videos2 = videosMapper.selectList(lambdaQueryWrapper);
                if (!videos2.isEmpty()) {
                    for (Videos videos3 : videos2) {
                        if (!videos3.getId().equals(videos.getId()))
                            videosList.add(videos3);
                    }
                }
            }
        }
        //如果数量不足20个时填充别的视频
        if(videosList.size()<20){

            LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
            videosLambdaQueryWrapper.eq(Videos::getStatus,1);
            List<Videos> videos2 = videosMapper.selectList(videosLambdaQueryWrapper);
            //打乱顺序
            Collections.shuffle(videos2);
            for (Videos videos3 : videos2) {
                if((!videos3.getId().equals(videos.getId()))&&videosList.size()<20)
                    videosList.add(videos3);
            }
        }

        List<Videos> videosList1=new ArrayList<>(videosList);
        List<SelectVideoDto> selectVideoDto;
        if(videos.getUserId()!=0)
            selectVideoDto = getSelectVideoDto(videosList1,videos.getUserId(),false);
        else {
            selectVideoDto = getSelectVideoDto(videosList1);
        }
        return selectVideoDto;
    }

    @Override
    public String formatDuration(double seconds) {
        int minutes = (int) (seconds / 60);
        int secs = (int) (seconds % 60);

        // 使用 String.format 来确保两位数格式
        return String.format("%02d:%02d", minutes, secs);
    }

    @Override
    public Page<SelectVideoDto> homeContributeVideos(Integer userId, Integer sort, Integer pageNum, String keyWord, Integer homeUserId) {


        Page<Videos> videosPage=new Page<>(pageNum,20);
        LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
        videosLambdaQueryWrapper.eq(Videos::getUserId,homeUserId)
                .eq(Videos::getStatus,1);

        if(StringUtil.notNullNorEmpty(keyWord)) {
            videosLambdaQueryWrapper
                    .and(wrapper -> wrapper
                    .like(Videos::getTitle,keyWord)
                    .or()
                    .apply("LOWER({0}) LIKE CONCAT('%', LOWER(title), '%')", keyWord)
                    .or()
                    .like(Videos::getContent, keyWord)
                    .or()
                    .like(Videos::getTag, keyWord)
                    );
        }

        if(sort==1)
            videosLambdaQueryWrapper.orderByDesc(Videos::getCreateTime);
        else if(sort==2)
            videosLambdaQueryWrapper.orderByDesc(Videos::getPlayNumber);
        else
            videosLambdaQueryWrapper.orderByDesc(Videos::getCollectNumber);

        Page<Videos> videosPage1 = videosMapper.selectPage(videosPage, videosLambdaQueryWrapper);
        List<Videos> records = videosPage1.getRecords();

        List<SelectVideoDto> selectVideoDto = this.getSelectVideoDto(records, userId,true);
        Page<SelectVideoDto> selectVideoDtoPage=new Page<>();
        selectVideoDtoPage.setRecords(selectVideoDto);
        selectVideoDtoPage.setTotal(videosPage1.getTotal());
        return selectVideoDtoPage;
    }

    @Override
    public List<SelectVideoDto> homeThrowCoinVideos(Integer userId, Integer homeUserId) {

        Page<ThrowCoin> throwCoinPage=new Page<>(1,10);
        LambdaQueryWrapper<ThrowCoin> throwCoinLambdaQueryWrapper=new LambdaQueryWrapper<>();
        throwCoinLambdaQueryWrapper.eq(ThrowCoin::getUserId,homeUserId);

        Page<ThrowCoin> selectPage = throwCoinMapper.selectPage(throwCoinPage, throwCoinLambdaQueryWrapper);
        List<ThrowCoin> records = selectPage.getRecords();

        List<Videos> videosList=new ArrayList<>();
        for (ThrowCoin record : records) {
            videosList.add(videosMapper.selectById(record.getVideoId()));
        }

        return this.getSelectVideoDto(videosList, userId, true);
    }

    @Override
    public List<SelectVideoDto> homeLoveVideos(Integer userId, Integer homeUserId) {

        LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
        likesLambdaQueryWrapper.eq(Likes::getUserId,homeUserId)
                .eq(Likes::getLikeType,1)
                .eq(Likes::getDeleteFlag,0)
                .orderByDesc(Likes::getLikeTime)
                .last("LIMIT 10");

        List<Likes> likes = likesMapper.selectList(likesLambdaQueryWrapper);
        List<Videos> videosList=new ArrayList<>();
        for (Likes like : likes) {
           videosList.add( videosMapper.selectById(like.getFondId()));
        }

        return this.getSelectVideoDto(videosList, userId, true);
    }

    @Override
    public List<SelectVideoDto> magnum(Integer homeUserId, Integer userId) {

        LambdaQueryWrapper<Videos> videosLambdaQueryWrapper=new LambdaQueryWrapper<>();
        videosLambdaQueryWrapper.eq(Videos::getUserId,homeUserId)
                .eq(Videos::getStatus,1)
                .orderByDesc(Videos::getLikeNumber)
                .last("LIMIT 3");
        List<Videos> videosList = videosMapper.selectList(videosLambdaQueryWrapper);
        return this.getSelectVideoDto2(videosList, userId, false);
    }
}
