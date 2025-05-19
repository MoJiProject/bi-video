package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.*;
import com.moji.po.CommentControls;
import com.moji.serve.LoginLimiterServer;
import com.moji.service.CommentService;
import com.moji.vo.SelectComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 添加评论
     * @param addComment
     * @return
     */
    @PostMapping("/addComment")
    public R<SelectComment> addComment(@RequestBody AddComment addComment, @RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(addComment.getComments().getUserId(),token))
            return R.error("发送失败");
        SelectComment selectComment = commentService.addComment(addComment);
        if(selectComment!=null)
            return R.success(selectComment);
        return R.error("发送失败");

    }


    /**
     * 根据id查询评论
     * @param videoId
     * @return
     */
    @GetMapping("/selectCommentByVideoId")
    @Cacheable(value = "selectCommentByVideoId",
            key = "'videoId:' + #videoId + 'dynamicId:' + #dynamicId + 'userId:' + #userId + 'commentSortFlag:' + #commentSortFlag + 'pageNum:' + #pageNum")
    public R<Page<SelectComment>> selectCommentByVideoId(@RequestParam(required = false) Integer videoId,@RequestParam(required = false) Integer dynamicId,@RequestParam Integer userId, @RequestParam(defaultValue = "1") Integer commentSortFlag,@RequestParam Integer pageNum) {
        if (videoId != null || dynamicId!=null)
            return R.success(commentService.selectCommentByVideoId(videoId,dynamicId,userId,commentSortFlag,pageNum));
        else
            return R.error("查询失败");
    }


    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @DeleteMapping("/deleteComment")
    public R<String> deleteComment(@RequestParam Integer commentId,@RequestParam Integer userId,@RequestHeader("Authorization") String token){

        if(commentId!=null) {
            Boolean flag=commentService.deleteComment(commentId,userId,token);
            if(flag)
            return R.success("删除成功");
            else
                return R.error("删除失败");
        }
        else
            return R.error("删除失败");

    }


    /**
     * 操作评论（点赞，踩）
     * @param commentControls
     * @param token
     * @return
     */
    @PostMapping("/controlsComment")
    public R<SelectComment> controlsComment(@RequestBody CommentControls commentControls,@RequestHeader("Authorization") String token){

        SelectComment selectComment = commentService.controlsComment(commentControls, token);
        if(selectComment!=null)
            return R.success(selectComment);
        return R.error("操作失败");

    }


    /**
     * 根据评论id查询该评论的回复
     * @return
     */
    @GetMapping("/selectReplyById")
    @Cacheable(value = "selectReplyById", key = "'videoId:' + #videoId + 'dynamicId:' + #dynamicId + 'commentId:' + #commentId + 'userId:' + #userId + 'pageNum:' + #pageNum")
    public R<Page<ReplyComment>> selectReplyById(@RequestParam(required = false) Integer videoId,@RequestParam(required = false) Integer dynamicId,@RequestParam Integer commentId,@RequestParam Integer userId,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        Page<ReplyComment> replyCommentPage = commentService.selectReplyById(commentId, userId, pageNum);
        if(!replyCommentPage.getRecords().isEmpty())
            return R.success(replyCommentPage);
        return R.error("查询失败");

    }


    /**
     * 查询所有回复用户的评论
     * @param userId
     * @param pageNum
     * @param token
     * @return
     */
    @GetMapping("/selectReplayComment")
    public R<List<SelectReplyComment>> selectReplayComment(@RequestParam Integer userId, @RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

    LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(commentService.selectReplyAll(userId,pageNum));

    }


    /**
     * 删除评论通知
      * @param commentId
     * @param userId
     * @param token
     * @return
     */
    @DeleteMapping("/deleteCommentNotification")
    public R<String> deleteCommentNotification(@RequestParam Integer commentId,@RequestParam Integer userId,@RequestParam Integer messageMenu,@RequestParam Integer type,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("删除失败");

        Boolean b = commentService.deleteCommentNotification(commentId,userId,messageMenu,type);
        if(b)
            return R.success("删除成功");
        return R.error("删除失败");
    }


    /**
     * 查询at用户的评论
     * @param userId
     * @param token
     * @return
     */
    @GetMapping("/selectAt")
    public R<List<SelectAt>> selectAt(@RequestParam Integer userId,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(commentService.selectAt(userId,pageNum));
    }


    /**
     * 查询给用户点赞的信息（评论、视频）
     * @param userId
     * @param pageNum
     * @param token
     * @return
     */
    @GetMapping("/selectLike")
    public R<List<SelectLike>> selectLike(@RequestParam Integer userId,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("查询失败");

        return R.success(commentService.selectLike(userId,pageNum));

    }


    /**
     * 更改点赞消息提示状态
     * @param userId
     * @param id
     * @param type
     * @param token
     * @return
     */
    @PutMapping("/dndLikeNotification")
    public R<String> dndLikeNotification(@RequestParam Integer userId,@RequestParam Integer id,@RequestParam Integer type,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        Boolean b = commentService.dndLikeNotification(userId, id, type);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");

    }


    /**
     * 查询喜欢评论或视频的用户
     * @return
     */
    @GetMapping("/selectLikeUser")
    public R<List<SelectLike>> selectLikeUser(@RequestParam Integer userId,@RequestParam Integer id,@RequestParam Integer type,@RequestParam Integer pageNum,@RequestHeader("Authorization") String token){

        LoginLimiterServer limiterServer=new LoginLimiterServer();
        if(!limiterServer.checkUser(userId,token))
            return R.error("修改失败");

        List<SelectLike> selectLikeList = commentService.selectLikeUser(userId, id, type, pageNum);
        return R.success(selectLikeList);
    }


    /**
     * 修改评论的置顶状态
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

        Boolean b = commentService.putUpStatus(userId, id);
        if(b)
            return R.success("修改成功");
        return R.error("修改失败");
    }


    /**
     * 查询单个评论
     * @param userId
     * @param mainCommentId
     * @param replyCommentId
     * @param token
     * @return
     */
    @GetMapping("/selectOneComment")
    public R<SelectComment> selectOneComment(@RequestParam Integer userId,@RequestParam Integer mainCommentId,@RequestParam(required = false) Integer replyCommentId,@RequestHeader("Authorization") String token){

        return R.success(commentService.selectOneComment(userId,mainCommentId,replyCommentId));
    }





}
