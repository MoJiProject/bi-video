package com.moji.controller;

import com.moji.R;
import com.moji.dto.HistoryDto;
import com.moji.dto.SelectHistoryDto;
import com.moji.po.History;
import com.moji.po.Videos;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
public class HistoryController {


    @Autowired
    private HistoryService historyService;

    /**
     * 查询历史记录
     * @param userId
     * @return
     */
    @GetMapping("/getHistory/{userId}")
    public R<List<SelectHistoryDto>> getHistory(@PathVariable Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");

        return R.success(historyService.getHistory(userId));
    }


    /**
     * 新增历史记录
     * @param acceptHistory
     * @return
     */
    @PostMapping(value = "/addHistory")
    public void addHistory(@RequestBody String acceptHistory){

        historyService.addHistory(acceptHistory);

    }


    /**
     * 获取单个视频历史记录
     * @param videos
     * @return
     */
    @PostMapping("/getHistoryById")
    public R<History> getHistoryById(@RequestBody Videos videos,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(videos.getUserId(),token))
            return R.error("获取失败");

        History history = historyService.getHistryById(videos);
        if(history!=null)
         return R.success(history);
        return R.error("查询失败");
    }


    /**
     * 删除所有历史记录
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteAllHistory")
    public R<String> deleteAllHistory(@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");

        int i = historyService.deleteAllHistory(userId);
        if(i>0)
            return R.success("清除成功");
        else return R.success("没有要清除的历史记录");
    }


    /**
     * 查询历史记录
     * @param userId
     * @param pageNum
     * @param keyWord
     * @param sort
     * @param startTime
     * @param endTime
     * @param token
     * @return
     */
    @GetMapping("/selectHistoryList")
    public R<List<HistoryDto>> selectHistoryList(@RequestParam Integer userId, @RequestParam Integer pageNum,
                                              @RequestParam(required = false) String keyWord,
                                              @RequestParam Integer sort,
                                              @RequestParam(required = false) String startTime,
                                              @RequestParam(required = false) String endTime,
                                              @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");

      return R.success(historyService.selectHistoryList(userId, pageNum, sort, keyWord, startTime, endTime));
    }


    /**
     * 批量删除历史记录
     * @param ids
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteHistory")
    public R<String> deleteHistory(@RequestParam List<Integer> ids,@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("删除失败");

        boolean b = historyService.deleteHistory(userId, ids);
        if(b)
            return R.success("删除成功");
        return R.error("删除失败");
    }




}
