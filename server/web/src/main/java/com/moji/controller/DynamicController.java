package com.moji.controller;

import com.moji.R;
import com.moji.dto.AddDynamic;
import com.moji.dto.DynamicDto;
import com.moji.dto.HomeDynamicDto;
import com.moji.po.Dynamic;
import com.moji.po.UserInfo2;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.DynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dynamic")
public class DynamicController {

    @Autowired
    private DynamicService dynamicService;

    /**
     * 根据用户id查询关注的人发布的动态
     * @param userId
     * @return
     */
    @GetMapping("/getFollowDynamic/{userId}")
    public R<List<DynamicDto>> getFollowDynamic(@PathVariable Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");

      return R.success(dynamicService.getFollowDynamic(userId));

    }

    /**
     * 观看后，清除未观看的动态
     * @param userId
     * @return
     */
    @GetMapping("/cleanFollowDynamicAllNumber/{userId}")
    public R<String> cleanFollowDynamicAllNumber(@PathVariable Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = dynamicService.cleanFollowDynamicAllNumber(userId);
        if(b)
         return R.success("修改成功");
        return R.error("修改失败");
    }


    /**
     * 更新稍后再看
     * @param dynamicDto
     * @return
     */
    @Caching(evict = {
            @CacheEvict(value = "collect",key = "#dynamicDto.userId")
    })
    @PutMapping("/updateWaitWatch")
    public R<String> updateWaitWatch(@RequestBody DynamicDto dynamicDto,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(dynamicDto.getUserId(),token))
            return R.error("操作失败");

        int i  = dynamicService.updateWaitWatch(dynamicDto.getUserId(), dynamicDto.getVideoId());

            if (i == 1)
                return R.success("0");
            else if (i == 0)
                return R.success("1");
            else
              return R.success("-1");

    }


    /**
     * 修改动态置顶状态
     * @param userId
     * @param dynamicId
     * @param token
     * @return
     */
    @PutMapping("/putUpStatus")
    public R<String> putUpStatus(@RequestParam Integer userId,@RequestParam Integer dynamicId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = dynamicService.putUpStatus(dynamicId);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");

    }


    /**
     * 点赞该动态
     * @param userId
     * @param dynamicId
     * @param token
     * @return
     */
    @PutMapping("/likeDynamic")
    public R<String> likeDynamic(@RequestParam Integer userId,@RequestParam Integer dynamicId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = dynamicService.likeDynamic(userId, dynamicId);
        if(b)
            return R.success("操作成功");
        return R.error("操作失败");

    }


    /**
     * 删除动态
     * @param userId
     * @param dynamicId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteDynamic")
    public R<String> deleteDynamic(@RequestParam Integer userId,@RequestParam Integer dynamicId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("删除失败");

        Boolean b = dynamicService.deleteDynamic(dynamicId);
        if(b)
            return R.success("删除成功");
        return R.error("删除失败");

    }


    /**
     * 新增动态
     * @param addDynamic
     * @param token
     * @return
     */
    @PostMapping("/addDynamic")
    public R<String> addDynamic(@RequestBody AddDynamic addDynamic, @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(addDynamic.getDynamic().getFollowId(),token))
            return R.error("发布失败");

        boolean b = dynamicService.addDynamic(addDynamic);
        if(b)
            return R.success("发布成功");
        return R.error("发布失败");
    }


    /**
     * 清除动态提醒
     * @param userId
     * @param dynamicUserId
     * @param token
     */
    @DeleteMapping("/cleanDynamicMessage")
    public void cleanDynamicMessage(@RequestParam Integer userId,@RequestParam Integer dynamicUserId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return;
        dynamicService.cleanDynamicMessage(userId,dynamicUserId);
    }


    /**
     * 查询关注的动态用户
     * @param userId
     * @param pageNum
     * @param token
     * @return
     */
    @GetMapping("/usersDynamic")
    public R<List<UserInfo2>> usersDynamic(@RequestParam Integer userId,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        List<UserInfo2> userInfo2List = dynamicService.usersDynamic(userId, pageNum);
        return R.success(userInfo2List);
    }



    /**
     * 查询用户主页的动态(动态页)
     * @param dynamicUserId
     * @param userId
     * @param pageNum
     * @param type
     * @return
     */
    @GetMapping("/getDynamic")
    public R<List<HomeDynamicDto>> getDynamic(@RequestParam Integer dynamicUserId, @RequestParam Integer userId, @RequestParam Integer pageNum, @RequestParam Integer type,@RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(dynamicService.getDynamic(dynamicUserId,userId,pageNum,type));
    }


    /**
     * 查询是否有新动态
     * @param userId
     * @param token
     * @return
     */
    @GetMapping("/allDynamicMessage")
    public R<Boolean> allDynamicMessage(@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

       return R.success(dynamicService.allDynamicMessage(userId));
    }


    /**
     * 根据动态id查询动态
     * @param dynamicId
     * @return
     */
    @GetMapping("/selectDynamicById")
    public R<HomeDynamicDto> selectDynamicById(@RequestParam Integer dynamicId){

        return R.success(dynamicService.selectDynamicById(dynamicId));
    }



    /**
     * 根据动态id查询动态用户是否点赞
     * @param dynamicId
     * @param userId
     * @return
     */
    @GetMapping("/selectDynamicByIdLike")
    public R<Boolean> selectDynamicByIdLike(@RequestParam Integer dynamicId,@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(dynamicService.selectDynamicByIdLike(dynamicId,userId));
    }


    /**
     * 查询喜欢该动态的用户
     * @param userId
     * @param pageNum
     * @param dynamicId
     * @return
     */
    @GetMapping("/likeDynamicUser")
    public R<List<UserInfo2>> likeDynamicUser(@RequestParam(required = false) Integer userId,@RequestParam Integer pageNum,@RequestParam Integer dynamicId){

        return R.success(dynamicService.likeDynamicUser(userId,pageNum,dynamicId));
    }


}
