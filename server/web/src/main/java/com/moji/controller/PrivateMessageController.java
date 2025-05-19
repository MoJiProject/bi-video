package com.moji.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.moji.R;
import com.moji.dto.SelectDialogue;
import com.moji.dto.SelectPrivateMessage;
import com.moji.dto.ShareVideo;
import com.moji.po.Dialogue;
import com.moji.po.PrivateMessage;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.PrivateMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/privateMessage")
public class PrivateMessageController {

    @Autowired
    private PrivateMessageService privateMessageService;


    /**
     * 获取私信对话列表
     * @param userId
     * @param token
     * @return
     */
    @GetMapping("/selectDialogue")
    @Cacheable(value = "selectDialogue",key = "'userId:' + #userId +'pageNum:' + #pageNum")
    public R<List<SelectDialogue>> selectDialogue(@RequestParam Integer userId,@RequestParam Integer pageNum, @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("获取失败");

        return R.success(privateMessageService.selectDialogueList(userId,pageNum));

    }


    /**
     * 修改私信对话的置顶状态
     * @param userId
     * @param id
     * @param token
     * @return
     */
    @PutMapping("/putUpStatus")
    public R<String> putUpStatus(@RequestParam Integer userId,@RequestParam Integer id,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = privateMessageService.putUpStatus(userId, id);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");
    }


    /**
     * 修改免打扰状态
     * @param userId
     * @param id
     * @param token
     * @return
     */
    @PutMapping("/putDndStatus")
    public R<String> putDndStatus(@RequestParam Integer userId,@RequestParam Integer id,@RequestHeader("Authorization") String token){
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = privateMessageService.putDndStatus(userId, id);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");
    }


    /**
     * 新增私信对话框
     * @param dialogue
     * @param token
     * @return
     */
    @PostMapping("/addDialogue")
    public R<String> addDialogue(@RequestBody Dialogue dialogue,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(dialogue.getUserId(),token))
            return R.error("新增对话失败");

        Boolean b = privateMessageService.addDialogue(dialogue);
        if(b)
            return R.success("新增对话成功");
        return R.error("新增对话失败");

    }


    /**
     * 删除私信对话
     * @param id
     * @param token
     * @return
     */
    @DeleteMapping("/deleteDialogue")
    public R<String> deleteDialogue(@RequestParam Integer id,@RequestHeader("Authorization") String token){

        Integer userId = Integer.valueOf(StpUtil.getLoginIdByToken(token).toString());
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("新增对话失败");

        Boolean b = privateMessageService.deleteDialogue(id);
        if(b)
            return R.success("删除成功");
        return R.error("删除失败");

    }


    /**
     * 发送消息
     * @param privateMessage
     * @return
     */
    @PostMapping("/sendMessage")
    public R<String> sendMessage(@RequestBody PrivateMessage privateMessage,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(privateMessage.getSenderId(),token))
            return R.error("发送失败");

        Boolean b1 = privateMessageService.checkFollowAndFans(privateMessage);
        if(!b1)
            return R.error("对方主动回复或关注你前，最多发送1条消息");

        Boolean b = privateMessageService.sendMessage(privateMessage);
        if(b)
            return R.success("发送成功");
        return R.error("发送失败");
    }


    /**
     * 撤回消息
     * @param id
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/revocationMessage")
    public R<String> revocationMessage(@RequestParam Integer id,@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("撤回失败");

        Boolean b = privateMessageService.revocationMessage(id,userId,1);
        if(b)
            return R.success("撤回成功");
        return R.error("撤回失败");
    }


    /**
     * 修改消息状态（已读未读）
     * @param userId
     * @param dialogueId
     * @return
     */
    @PutMapping("/putMessageStatus")
    public R<String> putMessageStatus(@RequestParam Integer userId,@RequestParam Integer dialogueId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = privateMessageService.putMessageStatus(userId, dialogueId);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");

    }


    /**
     * 删除私信
      * @param id
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteMessage")
    public R<String> deleteMessage(@RequestParam Integer id,@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("删除失败");

        Boolean b = privateMessageService.deleteMessage(id,userId);
        if(b)
            return R.success("删除成功");
        return R.error("删除失败");

    }


    /**
     * 查询私信
     * @param userId
     * @param dialogueId
     * @param pageNum
     * @param token
     * @return
     */
    @GetMapping("/selectMessage")
    @Cacheable(value = "selectMessage",key = "'userId:' + #userId + 'dialogueId:' + #dialogueId + 'pageNum:' + #pageNum")
    public R<SelectPrivateMessage> selectMessage(@RequestParam Integer userId,@RequestParam Integer dialogueId,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("删除失败");

        return R.success(privateMessageService.selectMessage(userId,dialogueId,pageNum));
    }


    /**
     * 修改用户的回复、@、被点赞的数量
     * @param userId
     * @param messageMenu
     * @param token
     * @return
     */
    @PutMapping("/putMessageNumber")
    public R<String> putMessageNumber(@RequestParam Integer userId,@RequestParam Integer messageMenu,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = privateMessageService.putMessageNumber(userId, messageMenu);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");
    }


    /**
     * 分享视频
     * @param shareVideo
     * @param token
     * @return
     */
    @PostMapping("/shareVideo")
    public R<String> shareVideo(@RequestBody ShareVideo shareVideo, @RequestHeader("Authorization") String token) {

        LoginLimiterServer limiterServer = new LoginLimiterServer();
        if (!limiterServer.checkUser(shareVideo.getUserId(), token))
            return R.error("分享失败");

        Boolean b = privateMessageService.shareVideo(shareVideo);
        if (b)
            return R.success("分享成功");
        return R.error("分享失败");
    }

}
