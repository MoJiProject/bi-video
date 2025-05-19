package com.moji.controller;

import com.moji.R;
import com.moji.mapper.CollectClassifyMapper;
import com.moji.po.CollectsClassify;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.CollectClassifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collectClassify")
public class CollectClassifyController {

    @Autowired
    private CollectClassifyService collectClassifyService;


    /**
     * 新建收藏夹
     * @param collectsClassify
     * @return
     */
    @PostMapping("/addCollectClassify")
    public R<String> addCollectClassify(@RequestBody CollectsClassify collectsClassify,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(collectsClassify.getUserId(),token))
            return R.error("新增失败");

        boolean b = collectClassifyService.addCollectClassify(collectsClassify);
        if(b)
            return R.success("新增成功");
        return R.error("新增失败");
    }


    /**
     * 删除收藏夹
     * @param id
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteCollectClassify")
    public R<String> deleteCollectClassify(@RequestParam Integer id,@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("删除失败");

        boolean b = collectClassifyService.deleteCollectClassify(id);
        if(b)
            return R.success("删除成功");
        return R.error("删除失败");
    }


    /**
     * 修改收藏夹信息
     * @param collectsClassify
     * @param token
     * @return
     */
    @PutMapping("/putCollectClassify")
    public R<String> putCollectClassify(@RequestBody CollectsClassify collectsClassify,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(collectsClassify.getUserId(),token))
            return R.error("修改失败");

        boolean b = collectClassifyService.putCollectClassify(collectsClassify);
        if (b)
            return R.success("修改成功");
        return R.error("修改失败");
    }



}
