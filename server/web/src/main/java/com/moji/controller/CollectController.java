package com.moji.controller;

import com.moji.R;
import com.moji.dto.CollectDto;
import com.moji.vo.CollectVo;
import com.moji.po.AcceptCollect;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collect")
public class CollectController {

    @Autowired
    private CollectService collectService;


    /**
     *
     * @param userId
     * @return 根据id获得用户所有收藏夹
     */
    @Cacheable(value = "collect",key = "#userId")
    @GetMapping("/getCollect/{userId}")
    public R<List<CollectVo>> getAllCollect(@PathVariable Integer userId, @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");

        return R.success(collectService.getAllCollect(userId));
    }


    /**
     * 添加视频至收藏夹
     * @param acceptCollect
     * @return 不一定是收藏或者是取消收藏
     */
    @CacheEvict(value = "collect",key = "#acceptCollect.userId")
    @PostMapping("/addCollect")
    public R<String> addCollect(@RequestBody AcceptCollect acceptCollect,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(acceptCollect.getUserId(),token))
            return R.error("操作失败");

         collectService.insertCollect(acceptCollect);
         return R.success("收藏成功");

    }


    /**
     * 删除失效收藏的视频
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteFailure")
    public R<String> deleteFailure(@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("清除失败");

        boolean b = collectService.deleteFailure(userId);
        if (b)
            return R.success("清除成功");
        return R.success("没有要清除的视频");

    }


    /**
     * 取消收藏
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteCollect")
    @CacheEvict(value = "collect",key = "#userId")
    public R<String> deleteCollect(@RequestParam Integer userId, @RequestParam List<Integer> ids, @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("取消收藏失败");

        boolean b = collectService.deleteCollect(userId,ids);
        if(b)
            return R.success("取消收藏成功");
        return R.error("取消收藏失败");

    }


    /**
     * 复制或移动收藏的视频
     * @param userId
     * @param collectIds
     * @param collectClassifyIds
     * @param controls
     * @param token
     * @return
     */
    @PutMapping("/controlCollect")
    @CacheEvict(value = "collect",key = "#userId")
    public R<String> controlCollect(@RequestParam Integer userId,@RequestParam List<Integer> collectIds,
                                    @RequestParam List<Integer> collectClassifyIds,
                                    @RequestParam Integer controls,
                                    @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("操作失败");

        boolean b = collectService.controlCollect(userId, collectIds, collectClassifyIds, controls);

        if(b)
            return R.success(controls==1?"复制成功":"移动成功");
        return R.error(controls==1?"复制失败":"移动失败");
    }


    /**
     * 查询稍后再看的视频
     * @param userId
     * @param sort
     * @param sort2
     * @param startTime
     * @param endTime
     * @param keyWord
     * @param token
     * @param pageNum
     * @return
     */
    @GetMapping("/selectWaitWatch")
    public R<List<CollectDto>> selectWaitWatch(@RequestParam Integer userId,@RequestParam Integer sort,
                                               @RequestParam Integer sort2,@RequestParam(required = false) String startTime,
                                               @RequestParam(required = false) String endTime,
                                               @RequestParam(required = false) String keyWord,
                                               @RequestParam Integer pageNum,
                                               @RequestHeader("Authorization") String token){


        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

         return R.success(collectService. selectWaitWatch(userId,sort,sort2,keyWord,startTime,endTime,pageNum));
    }


    /**
     * 清除所有稍后再看
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/cleanAllWaitWatch")
    @CacheEvict(value = "collect",key = "#userId")
    public R<String> cleanAllWaitWatch(@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("清除失败");

        int i = collectService.cleanAllWaitWatch(userId);
        if(i>0)
            return R.success("清除成功");
        return R.success("没有要清除的视频");
    }



}
