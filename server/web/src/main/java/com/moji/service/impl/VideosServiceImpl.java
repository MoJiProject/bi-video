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
import com.moji.util.RemoteVideoUtil;
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
import java.util.stream.Collectors;

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

    @Autowired
    private org.springframework.cache.CacheManager cacheManager;

    /**
     * 视频信息变更后清理缓存，与 VideosController 上的 @CacheEvict 语义保持一致。
     * 这里放在服务层，是为了让所有调用方（包括后台与创作中心）都能正确失效缓存。
     */
    private void evictVideoCache(Integer videoId) {

        org.springframework.cache.Cache videoTitleCache = cacheManager.getCache("videoTitle");
        if (videoTitleCache != null && videoId != null)
            videoTitleCache.evict(videoId);

        //收藏夹按 userId 聚合，视频状态变化会影响展示，整体失效
        org.springframework.cache.Cache collectCache = cacheManager.getCache("collect");
        if (collectCache != null)
            collectCache.clear();
    }

    @Override
    public Boolean insertVideo(String videoName, String coverName, Videos videos) {

        Users users = userMapper.selectById(videos.getUserId());
        videos.setUserName(users.getUserName());
        videos.setCreateTime(LocalDateTime.now());
        videos.setStatus(0);

        //远程视频：不落本地文件，只保存视频直链
        if (videos.getVideoSource() != null && videos.getVideoSource() == 1) {
            String remoteUrl = RemoteVideoUtil.parseRemoteUrl(videos.getRemoteUrl());
            if (remoteUrl == null)
                throw new RuntimeException("视频直链无效");
            videos.setRemoteUrl(remoteUrl);
            videos.setVideoAddress("");
            if (videos.getVideoTime() == null || videos.getVideoTime().isBlank())
                videos.setVideoTime("00:00");
            videos.setCoverAddress("/upload/video/cover/" + coverName);
        } else {
            String videoAddress = "/upload/video/" + videoName;
            String coverAddress = "/upload/video/cover/" + coverName;
            String durationString = null;
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
            videos.setVideoTime(durationString);
            videos.setVideoAddress(videoAddress);
            videos.setCoverAddress(coverAddress);
        }

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
        if (!subZoneKey.equals("全部分类")){
            videoStatusWrapper0.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper0.eq(Videos::getUserId,userId);

        videoAllNumber=videosMapper.selectCount(videoStatusWrapper0);


        LambdaQueryWrapper<Videos> videoStatusWrapper=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分类")){
            videoStatusWrapper.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper.eq(Videos::getUserId,userId);
        videoStatusWrapper.eq(Videos::getStatus,0);

        videoWaitNumber=videosMapper.selectCount(videoStatusWrapper);

        LambdaQueryWrapper<Videos> videoStatusWrapper1=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper1.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分类")){
            videoStatusWrapper1.eq(Videos::getSubZoneKey,subZoneKey);
        }

        videoStatusWrapper1.eq(Videos::getUserId,userId);
        videoStatusWrapper1.eq(Videos::getStatus,1);

        videoSuccessNumber=videosMapper.selectCount(videoStatusWrapper1);

        LambdaQueryWrapper<Videos> videoStatusWrapper2=new LambdaQueryWrapper<>();
        if (videoTitle!=null){
            videoStatusWrapper2.like(Videos::getTitle,videoTitle);
        }
        if (!subZoneKey.equals("全部分类")){
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
            if (!subZoneKey.equals("全部分类")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "发布时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放量排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏量排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕量排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==0){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);

            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分类")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "发布时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放量排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏量排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕量排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==1){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);
            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分类")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "发布时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放量排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏量排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕量排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
                default -> userByIdWrapper.orderByDesc(Videos::getCommentNumber);
            }
        }

        if (videoStatus==2){

            userByIdWrapper.eq(Videos::getStatus,videoStatus);
            if (videoTitle!=null){
                userByIdWrapper.like(Videos::getTitle,videoTitle);
            }
            if (!subZoneKey.equals("全部分类")){
                userByIdWrapper.eq(Videos::getSubZoneKey,subZoneKey);
            }
            switch (sortWay) {
                case "发布时间排序" -> userByIdWrapper.orderByDesc(Videos::getCreateTime);
                case "播放量排序" -> userByIdWrapper.orderByDesc(Videos::getPlayNumber);
                case "收藏量排序" -> userByIdWrapper.orderByDesc(Videos::getCollectNumber);
                case "弹幕量排序" -> userByIdWrapper.orderByDesc(Videos::getScrollingNumber);
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

        if(video==null)
            return false;

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
            if(!collects.isEmpty()){
                List<Integer> collectIds = collects.stream()
                        .map(Collects::getId)
                        .collect(Collectors.toList());
                Collects collectEntry = new Collects();
                collectEntry.setDeleteFlag(1);
                if(!collectIds.isEmpty()) {
                    collectMapper.update(collectEntry, new LambdaQueryWrapper<Collects>()
                            .in(Collects::getId, collectIds));
                }
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
            List<Scrolling> scrollingList = scrollingMapper.selectList(scrollingLambdaQueryWrapper);
            List<Integer> scrollIds = scrollingList.stream()
                    .map(Scrolling::getId)
                    .collect(Collectors.toList());
            if(!scrollIds.isEmpty()) {
                scrollingMapper.deleteBatchIds(scrollIds);
            }
            //删除评论
            LambdaQueryWrapper<Comments> commentsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            commentsLambdaQueryWrapper.eq(Comments::getVideoId,videoId);
            List<Comments> comments = commentsMapper.selectList(commentsLambdaQueryWrapper);
            commentService.deleteReply(comments,false,true);

            //远程视频没有本地视频文件，只需删除封面
            boolean remoteVideo = video.getVideoSource() != null && video.getVideoSource() == 1;
            int flag = 0;
            try {
                Files.deleteIfExists(coverPath);
                if (!remoteVideo)
                    Files.deleteIfExists(videoPath);
                flag = 1;
            } catch (IOException e) {
            }
            if (flag == 0) {
                throw new RuntimeException("视频或视频封面删除失败");
            }

            cacheService.deleteCommentCacheByVideoId(videoId,null,null);
            cacheService.deleteReplyCommentCacheByCommentId(videoId,null,null,null);

            return true;
        }
        else return false;
    }

    @Override
    @Transactional
    public Boolean updateVideo(String videoName, String coverName, Videos videos, Boolean vFlag, Boolean cFlag) {

        String videoAddress="/upload/video/"+videoName;
        String coverAddress="/upload/video/cover/"+coverName;
        String durationString=null;
        Videos videos1 = videosMapper.selectById(videos.getId());
        if(videos1==null)
            return false;
        //videos是由前端表单直接绑定的，不能整体落库，否则调用方可以顺带改写
        //user_id/play_number/like_number/status 等任意字段，这里只取允许编辑的字段
        Videos update = copyEditableFields(videos, videos1);
        //远程视频不解析本地文件时长
        boolean remoteVideo = update.getVideoSource()!=null&&update.getVideoSource()==1;
        if(remoteVideo){
            String remoteUrl=RemoteVideoUtil.parseRemoteUrl(update.getRemoteUrl());
            if(remoteUrl==null)
                throw new RuntimeException("视频直链无效");
            update.setRemoteUrl(remoteUrl);
            update.setVideoAddress("");
            if(update.getVideoTime()==null||update.getVideoTime().isBlank())
                update.setVideoTime("00:00");
        }else{
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
        update.setVideoTime(durationString);
        if (vFlag)
         update.setVideoAddress(videoAddress);
        }
        if (cFlag)
         update.setCoverAddress(coverAddress);
        update.setCreateTime(videos1.getCreateTime());
        update.setStatus(0);

        if(videos1.getStatus()==1){
            Users users = userMapper.selectById(videos1.getUserId());
            if(users!=null){
                users.setVideoNumber(Math.max(0,nullToZero(users.getVideoNumber())-1));
                users.setOwnDynamicNumber(Math.max(0,nullToZero(users.getOwnDynamicNumber())-1));
                userMapper.updateById(users);
            }

            //删除收藏
            LambdaQueryWrapper<Collects> collectsLambdaQueryWrapper=new LambdaQueryWrapper<>();
            collectsLambdaQueryWrapper.eq(Collects::getVideoId,videos1.getId());
            List<Collects> collects = collectMapper.selectList(collectsLambdaQueryWrapper);
            if(!collects.isEmpty()){
                List<Integer> collectIds = collects.stream()
                        .map(Collects::getId)
                        .collect(Collectors.toList());
                Collects collectEntry = new Collects();
                collectEntry.setDeleteFlag(1);
                if(!collectIds.isEmpty()) {
                    collectMapper.update(collectEntry, new LambdaQueryWrapper<Collects>()
                            .in(Collects::getId, collectIds));
                }
            }

            //清除动态
            LambdaQueryWrapper<Dynamic> dynamicLambdaQueryWrapper=new LambdaQueryWrapper<>();
            dynamicLambdaQueryWrapper.eq(Dynamic::getVideoId,videos1.getId())
                    .isNull(Dynamic::getCommentId);
            dynamicMapper.delete(dynamicLambdaQueryWrapper);
        }

        int i = videosMapper.updateById(update);
        //updateById会忽略null字段，驳回原因必须用UpdateWrapper显式清空，否则UP会一直看到上一次的驳回理由
        videosMapper.update(null,new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Videos>()
                .eq(Videos::getId,videos1.getId())
                .set(Videos::getExamineFiledMessage,null));
        evictVideoCache(videos1.getId());
        return i > 0;
    }

    /**
     * 只拷贝允许创作者编辑的字段，归属与统计类字段一律以库里的记录为准
     */
    private Videos copyEditableFields(Videos form, Videos db) {

        Videos update = Videos.builder()
                .id(db.getId())
                .userId(db.getUserId())
                .userName(db.getUserName())
                .title(form.getTitle())
                .content(form.getContent())
                .contentHtml(form.getContentHtml())
                .tag(form.getTag())
                .subZoneKey(form.getSubZoneKey())
                .subZoneValue(form.getSubZoneValue())
                .type(form.getType())
                .allowTwo(form.getAllowTwo())
                .videoSource(form.getVideoSource())
                .remoteUrl(form.getRemoteUrl())
                .videoAddress(db.getVideoAddress())
                .coverAddress(db.getCoverAddress())
                .videoTime(db.getVideoTime())
                //统计类字段全部沿用库里的值，杜绝前端伪造
                .playNumber(db.getPlayNumber())
                .likeNumber(db.getLikeNumber())
                .commentNumber(db.getCommentNumber())
                .collectNumber(db.getCollectNumber())
                .shareNumber(db.getShareNumber())
                .coinThrowNumber(db.getCoinThrowNumber())
                .newFansNumber(db.getNewFansNumber())
                .scrollingNumber(db.getScrollingNumber())
                .likeWarn(db.getLikeWarn())
                .build();

        return update;
    }

    private int nullToZero(Integer value) {
        return value==null?0:value;
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
                  .eq(Collects::getCollectName,"待看清单");

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
                    .videoSource(video.getVideoSource())
                    .remoteUrl(video.getRemoteUrl())
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
                    .eq(Collects::getCollectName,"待看清单");

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
                    .videoSource(video.getVideoSource())
                    .remoteUrl(video.getRemoteUrl())
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
                        .eq(Collects::getCollectName,"待看清单");

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
                    .videoSource(video.getVideoSource())
                    .remoteUrl(video.getRemoteUrl())
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
                    .videoSource(video.getVideoSource())
                    .remoteUrl(video.getRemoteUrl())
                    .collectNumber(video.getCollectNumber())
                    .build();

            videoDtos.add(selectVideoDto);

        }

        return videoDtos;

    }

    @Override
    @Transactional
    public Boolean examineVideo(int videoId) {

        Videos videos = videosMapper.selectById(videoId);
        if(videos==null)
            return false;

        videos.setStatus(1);
        //按既有约定：审核通过时间即为发布时间（创作中心与系统后台保持一致）
        videos.setCreateTime(LocalDateTime.now());
        int i = videosMapper.updateById(videos);
        if(i<=0)
            return false;

        //创作者计数改用SQL自增，避免读改写在并发下丢计数，也兜住计数为null的情况
        userMapper.update(null,new com.baomidou.mybatisplus.core.conditions.update
                .LambdaUpdateWrapper<Users>()
                .eq(Users::getId,videos.getUserId())
                .setSql("own_dynamic_number = IFNULL(own_dynamic_number,0) + 1")
                .setSql("video_number = IFNULL(video_number,0) + 1"));

        //向所有粉丝推送动态：原来是对每个粉丝「查用户 + 改用户 + 插动态」三条SQL，
        //粉丝多时是明显的N+1，这里改成按id集合批量更新 + 批量插入
        LambdaQueryWrapper<Fans> fansLambdaQueryWrapper=new LambdaQueryWrapper<>();
        fansLambdaQueryWrapper.eq(Fans::getUserId,videos.getUserId());
        List<Fans> fans = fansMapper.selectList(fansLambdaQueryWrapper);
        if(!fans.isEmpty()){

            List<Integer> fansIds=fans.stream()
                    .map(Fans::getFansId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            if(!fansIds.isEmpty()){
                userMapper.update(null,new com.baomidou.mybatisplus.core.conditions.update
                        .LambdaUpdateWrapper<Users>()
                        .in(Users::getId,fansIds)
                        .setSql("dynamic_number = IFNULL(dynamic_number,0) + 1"));

                List<Dynamic> fanDynamics=fansIds.stream()
                        .map(fansId->Dynamic.builder()
                                .watchDynamicFlag(0)
                                .videoId(videoId)
                                .fansId(fansId)
                                .followId(videos.getUserId())
                                .fansFlag(1)
                                .build())
                        .collect(Collectors.toList());
                dynamicMapper.insertBatch(fanDynamics,videos.getCreateTime());
            }
        }

        //更新收藏
        collectMapper.update(Collects.builder().deleteFlag(0).build(),
                new LambdaQueryWrapper<Collects>().eq(Collects::getVideoId,videoId));

        Dynamic dynamic=Dynamic
                .builder()
                .videoId(videos.getId())
                .followId(videos.getUserId())
                .publishTime(videos.getCreateTime())
                .build();
        dynamicMapper.insert(dynamic);

        evictVideoCache(videoId);

        return true;
    }

    @Override
    public SelectVideoByIdVo selectByVideoId(Integer videoId, Integer userId){

        Users user = userMapper.selectById(userId);
        Videos videos = videosMapper.selectById(videoId);
        if (videos==null||(videos.getStatus()!=1&&user.getAdminFlag()==0))
            return null;
        Users upUsers = userMapper.selectById(videos.getUserId());

        int isFansFlag=0;
        boolean likeVideoClickFlag=false;
        boolean videoThrowCoinClickFlag=false;
        boolean videoShareClickFlag=false;
        boolean videoCollectClickFlag=false;

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
                if(collect.getCollectName().equals("待看清单"))
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

        Integer operatorId=selectVideoByIdVo.getUserId();
        Integer controlsType=nullToZero(selectVideoByIdVo.getControlsType());
        if(operatorId==null||operatorId<=0||selectVideoByIdVo.getUpVideo()==null)
            return selectVideoByIdVo;

        //upVideo/upUser 都是前端整体回传的实体，不能直接落库，否则调用方可以顺带改写
        //任意字段(比如把status改成1、把播放量刷高)。这里只认videoId，其余全部以库为准。
        Videos dbVideo=videosMapper.selectById(selectVideoByIdVo.getUpVideo().getId());
        if(dbVideo==null)
            return selectVideoByIdVo;
        Integer videoId=dbVideo.getId();
        //创作者一律取视频真正的归属，避免前端伪造upUser.id把点赞/硬币算到别人头上
        Integer upUserId=dbVideo.getUserId();

        if(controlsType==1||controlsType==3)
        {
            //点赞
            if(Boolean.TRUE.equals(selectVideoByIdVo.getLikeVideoClickFlag()))
            {
                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper.eq(Likes::getFondId,videoId)
                        .eq(Likes::getLikeType,1)
                        .eq(Likes::getUserId,operatorId);
                Likes existLike=likesMapper.selectOne(likesLambdaQueryWrapper);
                if(existLike==null)
                {
                    likesMapper.insert(Likes.builder()
                            .fondId(videoId)
                            .userId(operatorId)
                            .likeTime(LocalDateTime.now())
                            .likeType(1)
                            .likeUserId(upUserId)
                            .build());

                    Users upUsers=userMapper.selectById(upUserId);
                    //是否开启点赞提醒，由视频与创作者的真实设置决定
                    boolean warn=dbVideo.getLikeWarn()!=null&&dbVideo.getLikeWarn()==1
                            &&upUsers!=null&&upUsers.getLikeMessageWarn()!=null&&upUsers.getLikeMessageWarn()==1
                            &&!Objects.equals(upUserId,operatorId);
                    if(upUsers!=null){
                        addLikeNumber(upUserId,warn?1:0,warn?1:0);
                    }
                    addVideoCounter(videoId,"like_number",1);
                }
            }
            else
            {
                LambdaQueryWrapper<Likes> likesLambdaQueryWrapper=new LambdaQueryWrapper<>();
                likesLambdaQueryWrapper.eq(Likes::getFondId,videoId)
                        .eq(Likes::getLikeType,1)
                        .eq(Likes::getUserId,operatorId);
                Likes existLike=likesMapper.selectOne(likesLambdaQueryWrapper);
                if(existLike!=null)
                {
                    likesMapper.deleteById(existLike);

                    Users upUsers=userMapper.selectById(upUserId);
                    boolean warn=dbVideo.getLikeWarn()!=null&&dbVideo.getLikeWarn()==1
                            &&upUsers!=null&&upUsers.getLikeMessageWarn()!=null&&upUsers.getLikeMessageWarn()==1;
                    if(upUsers!=null){
                        addLikeNumber(upUserId,-1,warn?-1:0,warn?-1:0);
                    }
                    addVideoCounter(videoId,"like_number",-1);
                }
            }
        }
        if(controlsType==2||controlsType==3) {
            //投币
            if (Boolean.TRUE.equals(selectVideoByIdVo.getVideoThrowCoinClickFlag())) {
                //前端只会传1或2，这里做范围收敛，避免被构造超大值把硬币扣成负数
                int coinCount=nullToZero(selectVideoByIdVo.getThrowCoinNumber());
                if(coinCount<1)coinCount=1;
                if(coinCount>2)coinCount=2;

                LambdaQueryWrapper<ThrowCoin> throwCoinLambdaQueryWrapper = new LambdaQueryWrapper<>();
                throwCoinLambdaQueryWrapper.eq(ThrowCoin::getVideoId,videoId)
                        .eq(ThrowCoin::getUserId,operatorId);

                Users users = userMapper.selectById(operatorId);
                //必须是硬币够投的数量，否则不能扣
                if (throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper) == null
                        && users!=null && nullToZero(users.getCoinNumber())>=coinCount) {
                    selectVideoByIdVo.setThrowCoinResult(1);

                    throwCoinMapper.insert(ThrowCoin.builder()
                            .videoId(videoId)
                            .userId(operatorId)
                            .build());

                    addVideoCounter(videoId,"coin_throw_number",coinCount);
                    //投币者扣硬币并加经验
                    users.setCoinNumber(nullToZero(users.getCoinNumber())-coinCount);
                    addExpAndLevelUp(users,2);
                    userMapper.updateById(users);

                    //创作者收硬币并加经验
                    Users upUser = userMapper.selectById(upUserId);
                    if(upUser!=null){
                        upUser.setCoinNumber(nullToZero(upUser.getCoinNumber())+coinCount);
                        addExpAndLevelUp(upUser,2);
                        userMapper.updateById(upUser);
                    }
                } else if (throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper) == null && users!=null && nullToZero(users.getCoinNumber())<coinCount)
                    selectVideoByIdVo.setThrowCoinResult(0);
                else if (throwCoinMapper.selectOne(throwCoinLambdaQueryWrapper) != null)
                    selectVideoByIdVo.setThrowCoinResult(1);
            }
        }
        if(controlsType==3)
        {
            //收藏
            if(Boolean.TRUE.equals(selectVideoByIdVo.getVideoCollectClickFlag()))
            {
                LambdaQueryWrapper<Collects> collectLambdaQueryWrapper=new LambdaQueryWrapper<>();
                collectLambdaQueryWrapper.eq(Collects::getVideoId,videoId)
                        .ne(Collects::getCollectName,"待看清单")
                        .eq(Collects::getUserId,operatorId);
                List<Collects> collects = collectMapper.selectList(collectLambdaQueryWrapper);
                if(collects.isEmpty())
                {
                    LambdaQueryWrapper<Collects> collectLambdaQueryWrapper2=new LambdaQueryWrapper<>();
                    collectLambdaQueryWrapper2.eq(Collects::getVideoId,videoId)
                            .eq(Collects::getCollectName,"默认收藏夹")
                            .eq(Collects::getUserId,operatorId);
                    if(collectMapper.selectOne(collectLambdaQueryWrapper2)==null) {
                        Collects collect = Collects.builder()
                                .videoId(videoId)
                                .userId(operatorId)
                                .collectName("默认收藏夹")
                                .collectTime(LocalDateTime.now())
                                .build();
                        collectMapper.insert(collect);
                    }
                    LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
                    collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getCollectName,"默认收藏夹")
                            .eq(CollectsClassify::getUserId,operatorId);

                    CollectsClassify collectsClassify = collectClassifyMapper.selectOne(collectsClassifyLambdaQueryWrapper);
                    if(collectsClassify==null)
                    {
                        collectClassifyMapper.insert(CollectsClassify.builder()
                                .collectName("默认收藏夹")
                                .videoNumber(1)
                                .userId(operatorId)
                                .build());
                    }
                    else {
                        //收藏夹条数用SQL自增，避免并发下的读改写丢失
                        collectClassifyMapper.update(null,new com.baomidou.mybatisplus.core.conditions.update
                                .LambdaUpdateWrapper<CollectsClassify>()
                                .eq(CollectsClassify::getId,collectsClassify.getId())
                                .setSql("video_number = IFNULL(video_number,0) + 1"));
                    }
                    addVideoCounter(videoId,"collect_number",1);
                }
            }
        }

        //更新视频用户信息
        Users upUser = userMapper.selectById(upUserId);
        UserInfo2 upUserInfo=new UserInfo2();
        if(upUser!=null)
            BeanUtils.copyProperties(upUser,upUserInfo);
        Videos upVideo = videosMapper.selectById(videoId);

        return SelectVideoByIdVo.builder()
                .upVideo(upVideo)
                .upUser(upUserInfo)
                .userId(operatorId)
                .likeVideoClickFlag(selectVideoByIdVo.getLikeVideoClickFlag())
                .videoThrowCoinClickFlag(selectVideoByIdVo.getVideoThrowCoinClickFlag())
                .videoCollectClickFlag(selectVideoByIdVo.getVideoCollectClickFlag())
                .isFansFlag(selectVideoByIdVo.getIsFansFlag())
                .videoShareClickFlag(selectVideoByIdVo.getVideoShareClickFlag())
                .throwCoinResult(selectVideoByIdVo.getThrowCoinResult())
                .build();
    }

    /**
     * 原子增减视频的计数字段，避免「读-改-写」在并发下丢计数，也顺带兜住字段为null的情况
     * @param videoId 视频id
     * @param column 计数字段名
     * @param delta 增量
     */
    private void addVideoCounter(Integer videoId, String column, int delta) {

        videosMapper.update(null,new com.baomidou.mybatisplus.core.conditions.update
                .LambdaUpdateWrapper<Videos>()
                .eq(Videos::getId,videoId)
                .setSql(column+" = GREATEST(IFNULL("+column+",0) + "+delta+", 0)"));
    }

    /**
     * 原子增减创作者的获赞与互动计数
     */
    private void addLikeNumber(Integer upUserId, int likeDelta, int likeAllDelta) {
        addLikeNumber(upUserId,likeDelta,likeAllDelta,0);
    }

    /**
     * 原子增减创作者的获赞与互动计数
     * @param likeDelta like_number增量
     * @param likeAllDelta like_all_number增量
     * @param allMessageDelta all_message_number增量
     */
    private void addLikeNumber(Integer upUserId, int likeDelta, int likeAllDelta, int allMessageDelta) {

        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Users> wrapper
                =new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Users>()
                .eq(Users::getId,upUserId);

        if(likeDelta!=0)
            wrapper.setSql("like_number = GREATEST(IFNULL(like_number,0) + "+likeDelta+", 0)");
        if(likeAllDelta!=0)
            wrapper.setSql("like_all_number = GREATEST(IFNULL(like_all_number,0) + "+likeAllDelta+", 0)");
        if(allMessageDelta!=0)
            wrapper.setSql("all_message_number = GREATEST(IFNULL(all_message_number,0) + "+allMessageDelta+", 0)");

        userMapper.update(null,wrapper);
    }

    /**
     * 增加经验并按需升级。
     * 原实现里「经验满100减100」没有提升等级，导致5级永远上不去；
     * 并且给UP加经验时误用了投币者的exp，会把UP的经验改小。这里统一修正。
     */
    private void addExpAndLevelUp(Users users, int addExp) {

        int grade=Math.max(1,nullToZero(users.getGrade()));
        if(grade>=6)
            return;

        int exp=nullToZero(users.getExp())+addExp;
        while(exp>=100&&grade<6){
            exp-=100;
            grade++;
        }
        //满级后经验条保持在满格，与原有展示一致
        if(grade>=6)
            exp=100;

        users.setExp(exp);
        users.setGrade(grade);
    }

    @Override
    public List<ResponseCollectClassify> getByIdCollectClassify(Integer userId, Integer videoId) {

        LambdaQueryWrapper<CollectsClassify> collectsClassifyLambdaQueryWrapper=new LambdaQueryWrapper<>();
        collectsClassifyLambdaQueryWrapper.eq(CollectsClassify::getUserId,userId);
        List<CollectsClassify> collectsClassifies = collectClassifyMapper.selectList(collectsClassifyLambdaQueryWrapper);
        List<ResponseCollectClassify> newCollectClassify=new ArrayList<>();
        for (CollectsClassify collectsClassify : collectsClassifies) {
            if(!collectsClassify.getCollectName().equals("待看清单"))
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
        List<Integer> videoIds = records.stream()
                .map(ThrowCoin::getVideoId)
                .distinct()
                .collect(Collectors.toList());
        if (!videoIds.isEmpty()) {
            videosList.addAll(videosMapper.selectBatchIds(videoIds));
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
        List<Integer> videoIds = likes.stream()
                .map(Likes::getFondId)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, Videos> videosMap = videosMapper.selectBatchIds(videoIds)
                .stream()
                .collect(Collectors.toMap(Videos::getId, v -> v));
        for (Likes like : likes) {
            Videos video = videosMap.get(like.getFondId());
            if (video != null) {
                videosList.add(video);
            }
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
