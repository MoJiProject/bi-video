package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.CollectDto;
import com.moji.service.CollectClassifyService;
import com.moji.vo.CollectVo;
import com.moji.dto.HomeDynamicDto;
import com.moji.dto.SelectVideoDto;
import com.moji.po.CollectsClassify;
import com.moji.po.Dynamic;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.CollectService;
import com.moji.service.DynamicService;
import com.moji.service.VideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/home")
public class HomeController {


    @Autowired
    private VideosService videosService;
    @Autowired
    private DynamicService dynamicService;
    @Autowired
    private CollectService collectService;
    @Autowired
    private CollectClassifyService collectClassifyService;


    /**
     * 根据用户id查询主页投稿的视频
     * @param userId
     * @return
     */
    @GetMapping("/contribute")
    public R<Page<SelectVideoDto>> homeContributeVideos(@RequestParam(required = false) Integer userId,@RequestParam Integer homeUserId, @RequestParam Integer sort,@RequestParam(required = false,defaultValue = "") String keyWord,@RequestParam Integer pageNum) {

        return R.success(videosService.homeContributeVideos(userId, sort, pageNum, keyWord, homeUserId));
    }



    /**
     * 根据用户id查询用户最近投币的的视频
     * @param userId
     * @return
     */
    @GetMapping("/throwCoin")
    public R<List<SelectVideoDto>> homeThrowCoinVideos(@RequestParam(required = false) Integer userId, @RequestParam Integer homeUserId) {

        return R.success(videosService.homeThrowCoinVideos(userId,homeUserId));
    }


    /**
     * 根据用户id查询用户最近投币的的视频
     * @param userId
     * @return
     */
    @GetMapping("/love")
    public R<List<SelectVideoDto>> homeLoveVideos(@RequestParam(required = false) Integer userId, @RequestParam Integer homeUserId) {

        return R.success(videosService.homeLoveVideos(userId,homeUserId));
    }


    /**
     * 获取有图片的动态、评论
     * @return
     */
    @GetMapping("/dynamicImg")
    public R<Page<Dynamic>> dynamicImg(@RequestParam Integer homeUserId, @RequestParam Integer type, @RequestParam Integer pageNum) {

        return R.success(dynamicService.dynamicImg(homeUserId, type, pageNum));
    }


    /**
     * 查询用户主页的动态
     * @param homeUserId
     * @param userId
     * @param pageNum
     * @param type
     * @return
     */
    @GetMapping("/dynamic")
    public R<Page<HomeDynamicDto>> homeDynamic(@RequestParam Integer homeUserId,@RequestParam(required = false) Integer userId,@RequestParam Integer pageNum,@RequestParam Integer type,@RequestParam(required = false) String keyWord) {

        return R.success(dynamicService.homeDynamic(homeUserId,userId,pageNum,type,keyWord));
    }


    /**
     * 查询up主的代表作
     * @param homeUserId
     * @param userId
     * @return
     */
    @GetMapping("/magnum")
    public R<List<SelectVideoDto>> magnum(@RequestParam Integer homeUserId,@RequestParam(required = false) Integer userId){

        return R.success(videosService.magnum(homeUserId,userId));

    }


    /**
     * 获取用户所有收藏夹
     * @param homeUserId
     * @return
     */
    @GetMapping("/collectsClassify")
    public R<List<CollectsClassify>> getCollectsClassify(@RequestParam Integer homeUserId,@RequestParam(required = false) Integer userId,@RequestHeader(value = "Authorization",required = false) String token){

        if(Objects.equals(userId, homeUserId)){
            LoginLimiterServer limiterServer=new LoginLimiterServer();
            if(!limiterServer.checkUser(userId,token))
                return R.error("查询失败");
        }
        return R.success(collectClassifyService.getCollectsClassify(homeUserId,userId));
    }


    /**
     * 根据收藏夹名称获取收藏
     * @param collectName
     * @param homeUserId
     * @param userId
     * @return
     */
    @GetMapping("/collectByName")
    public R<Page<CollectDto>> getCollectByName(@RequestParam String collectName,@RequestParam Integer homeUserId,@RequestParam Integer pageNum,
                                                @RequestParam Integer type,@RequestParam Integer sort,@RequestParam(required = false) String keyWord,@RequestParam(required = false) Integer userId ){
       return R.success(collectService.getCollectByName(collectName,homeUserId,userId,pageNum,type,sort,keyWord));
    }

}
