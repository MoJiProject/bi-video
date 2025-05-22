package com.moji.controller;
import com.moji.R;
import com.moji.dto.AddFollowDto;
import com.moji.dto.SelectVideoDto;
import com.moji.dto.VideoExamineDto;
import com.moji.dto.VideosDto;
import com.moji.mapper.VideosMapper;
import com.moji.po.*;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.ScrollingService;
import com.moji.service.UserService;
import com.moji.service.VideosService;
import com.moji.vo.SelectVideoByIdVo;
import com.moji.vo.UsersVideosVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/video")
public class VideosController {

    @Autowired
    private VideosService videosService;

    @Autowired
    private VideosMapper videosMapper;

    @Autowired
    private UserService userService;

   @Autowired
   private ScrollingService scrollingService;


    /**
     * 获得视频数据(投稿页)
     * @param videoDto
     * @return
     */
    @PostMapping("/selectVideoData")
    public R<VideosDto> selectVideoData(@RequestBody VideosDto videoDto,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(videoDto.getUserId(),token))
            return R.error("操作失败");

        return R.success(videosService.selectDtoData(videoDto.getUserId()));
    }

    /**
     * 获得用户视频总数据
     * @param userId
     * @param videoTitle
     * @param subZoneKey
     * @param sortWay
     * @param videoStatus
     * @return
     */
    @GetMapping("/userUploadVideo")
    public R<UsersVideosVo> selectByUserIdVideo(@RequestParam Integer userId,@RequestParam String videoTitle,@RequestParam String subZoneKey,@RequestParam String sortWay,@RequestParam Integer videoStatus,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        return R.success(videosService.selectByUserIdVideo(userId,videoTitle,subZoneKey,sortWay,videoStatus,pageNum));
    }

    /**
     * 查询用户总视频进行管理
     * @param userId
     * @param videoTitle
     * @param subZoneKey
     * @param sortWay
     * @param videoStatus
     * @return
     */
    @GetMapping("/userExamineVideo")
    public R<UsersVideosVo> selectByUserIExaminedVideo(@RequestParam Integer userId,@RequestParam String videoTitle,@RequestParam String subZoneKey,@RequestParam String sortWay,@RequestParam Integer videoStatus,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        return R.success(videosService.selectByUserExamineVideo(userId,videoTitle,subZoneKey,sortWay,videoStatus,pageNum));
    }

    /**
     * 删除视频
     * @param videoId
     * @param videoStatus
     * @return
     */
    @Caching(evict = {
            @CacheEvict(value = "collect",allEntries = true,condition = "#videoStatus == 1"),
            @CacheEvict(value = "videoTitle",key = "#videoId",condition = "#videoStatus == 1"),
    })
    @DeleteMapping("/deleteVideo")
    public R<String> deleteVideo(@RequestParam Integer videoId,@RequestParam Integer userId,@RequestParam Integer videoStatus,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        Boolean flag = videosService.deleteVideo(videoId);
        if (flag)
            return R.success("删除成功");

        return R.error("删除失败");

    }

    /**
     * 获得视频信息
     * @param videoId
     * @return
     */
    @GetMapping("/getVideoInfo")
    public R<Videos> getVideoInfo(@RequestParam int videoId){

        Videos videos = videosMapper.selectById(videoId);
        try{
            if (videos.getId()==videoId)
                return R.success(videos);
        }catch (Exception e){
            return R.error("");
        }
        return R.error("");
    }

    /**
     * 首页展示视频（换一换）
     * @param userId
     * @param sort
     * @param pageNum
     * @return
     */
    @GetMapping("/selectVideo")
    public R<List<SelectVideoDto>> selectVideo(@RequestParam Integer userId,@RequestParam Integer sort,@RequestParam Integer pageNum){

        List<SelectVideoDto> videoDtos = videosService.getVideo(userId,sort,pageNum);
        return R.success(videoDtos);
    }

    /**
     * 审核视频通过
     * @param video
     * @return
     */
    @Caching(evict = {
            @CacheEvict(value = "collect", allEntries = true),
            @CacheEvict(value = "videoTitle",key = "#video.id",condition = "#video.status == 1")
    })
    @PutMapping("/examineVideo")
    public R<String> examineVideo(@RequestBody Videos video,@RequestHeader("Authorization") String token){

        Users users = userService.getById(video.getUserId());
        if(users==null||(users.getAdminFlag()==0&&!users.getUserName().equals("MoJi")))
            return R.error("操作失败");

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(video.getUserId(),token))
            return R.error("操作失败");

        Boolean b = videosService.examineVideo(video.getId());

        if (b)
         return R.success("审核成功");
        return R.error("审核失败");
    }

    /**
     * 视频退回
     * @param videoExamineDto
     * @return
     */
    @PostMapping("/examineVideoFiled")
    public R<String> examineVideoFiled(@RequestBody VideoExamineDto videoExamineDto,@RequestHeader("Authorization") String token){

        Users users = userService.getById(videoExamineDto.getUserId());
        if(users==null||(users.getAdminFlag()==0&&!users.getUserName().equals("MoJi")))
            return R.error("操作失败");

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(videoExamineDto.getUserId(),token))
            return R.error("操作失败");

        Videos videos = videosMapper.selectById(videoExamineDto.getVideoId());
        videos.setExamineFiledMessage(videoExamineDto.getExamineFiledMessage());
        videos.setStatus(2);
        int i = videosMapper.updateById(videos);

        if (i>0)
            return R.success("审核成功");

        return R.error("审核失败");


    }

    /**
     * 关注
     * @param addFollowDto
     * @return
     */
    @PostMapping("/addFollow")
    public R<String> addFollow(@RequestBody AddFollowDto addFollowDto,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(addFollowDto.getFansId(),token))
            return R.error("操作失败");

        Boolean b = userService.addFollow(addFollowDto);
        if (b)
         return R.success("关注成功");

        return R.error("关注失败");

    }


    /**
     * 取消关注
     * @param addFollowDto
     * @return
     */
    @PostMapping("/deleteFollow")
    public R<String> deleteFollow(@RequestBody AddFollowDto addFollowDto,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(addFollowDto.getFansId(),token))
            return R.error("操作失败");

        Boolean b = userService.daleteFollow(addFollowDto);
        if (b)
          return R.success("取消关注成功");

       return R.error("取消关注失败");

    }


    /**
     * 根据id查询视频
     * @param videoId
     * @param userId
     * @return
     */
    @GetMapping("/selectByVideoId")
    public R<SelectVideoByIdVo> selectByVideoId(@RequestParam Integer videoId,@RequestParam Integer userId){

       SelectVideoByIdVo selectVideoByIdVo = videosService.selectByVideoId(videoId,userId);

       if(selectVideoByIdVo==null)
           return R.error("视频id错误");

        return R.success(selectVideoByIdVo);
    }

    /**
     * 保存弹幕
     * @param scrolling
     * @return
     */
    @PostMapping("/sendScrolling")
    public R<String> sendScrolling(@RequestBody Scrolling scrolling,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(scrolling.getUserId(),token))
            return R.error("操作失败");

        Boolean b = scrollingService.insterScrolling(scrolling);
        if(b)
         return R.success("弹幕发送成功！");
        return R.error("弹幕发送失败");
    }

    /**
     * 撤回弹幕
     * @param scrollingId
     * @return
     */
    @DeleteMapping("/revocationScrolling")
    public R<String> revocationScrolling(@RequestParam Integer scrollingId,@RequestHeader("Authorization") String token){

        Boolean b = scrollingService.revocationScrolling(scrollingId,token);

        if(b)
         return R.success("撤回成功");
        return R.error("撤回失败");

    }

    /**
     * 三连
     * @param selectVideoByIdVo
     * @return
     */
    @PostMapping("/getLTCAxios")
    @CacheEvict(value = "collect" ,key = "#selectVideoByIdVo.userId", condition = "#selectVideoByIdVo.videoCollectClickFlag==true")
    public R<SelectVideoByIdVo> getLTCAxios(@RequestBody SelectVideoByIdVo selectVideoByIdVo,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(selectVideoByIdVo.getUserId(),token))
            return R.error("操作失败");

        return R.success(videosService.getLTCAxios(selectVideoByIdVo));
    }

    /**
     *根据id 获取收藏的信息
     * @param userId
     * @return
     */
    @GetMapping("/getByIdCollectClassify")
    public R<List<ResponseCollectClassify>> getByIdCollectClassify(Integer userId,Integer videoId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        List<ResponseCollectClassify> byIdCollectClassify = videosService.getByIdCollectClassify(userId, videoId);

        return R.success(byIdCollectClassify);


    }

    /**
     * 更新视频播放量
     * @param videoId
     */
    @PutMapping("/updateVideoPlay")
    @Transactional
    public void updateVideoPlay(HttpServletRequest httpServletRequest, @RequestParam Integer videoId){

        Videos videos = videosMapper.selectById(videoId);
        if(videos!=null)
        {

            LoginLimiterServer limiterServer=new LoginLimiterServer();
            boolean b=limiterServer.updateVideoPlayer(videos.getVideoTime(),httpServletRequest.getRemoteAddr(),videoId);
            if(b) {
               videos.setPlayNumber(videos.getPlayNumber() + 1);
               videosMapper.updateById(videos);
           }
        }
    }


    /**
     * 获取视频单页面的跟视频相关的视频
     * @param videos
     * @return
     */
    @PostMapping("/getVideoPageByVideo")
    public R<List<SelectVideoDto>> getVideoPageByVideo(@RequestBody Videos videos){

        List<SelectVideoDto> videoPageByVideo = videosService.getVideoPageByVideo(videos);

        return R.success(videoPageByVideo);

    }


    /**
     * 获取视频标题
     * @param id
     * @return
     */
    @GetMapping("/getVideoTitle/{id}")
    @Cacheable(value = "videoTitle",key = "#id")
    public R<String> getVideoTitle(@PathVariable Integer id){

        Videos videos = videosMapper.selectById(id);
        if(videos!=null&&videos.getStatus()==1)
            return R.success(videos.getTitle());
        return R.error("视频不存在");
    }








}
