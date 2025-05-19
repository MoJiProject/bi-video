package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.*;
import com.moji.po.CommentControls;
import com.moji.po.Comments;
import com.moji.vo.SelectComment;

import java.util.List;

public interface CommentService extends IService<Comments> {
    SelectComment addComment(AddComment addComment);

    Page<SelectComment> selectCommentByVideoId(Integer videoId,Integer dynamicId, Integer userId, Integer commentSortFlag, Integer pageNum);

    Boolean deleteComment(Integer commentId, Integer userId, String token);

    SelectComment controlsComment(CommentControls commentControls, String token);

    Page<ReplyComment> selectReplyById(Integer commentId, Integer userId, Integer pageNum);

    //处理删除评论后回复评论的数据
    void deleteReply(List<Comments> commentsList, boolean flag,boolean flag2);//第一个flag是是否更新数量 第二个flag是是否删除该评论

    String getImageFormat(String base64);

    List<SelectReplyComment> selectReplyAll(Integer userId, Integer pageNum);

    Boolean deleteCommentNotification(Integer commentId, Integer userId, Integer messageMenu,Integer type);

    List<SelectAt> selectAt(Integer userId, Integer pageNum);

    List<SelectLike> selectLike(Integer userId, Integer pageNum);

    Boolean dndLikeNotification(Integer userId, Integer id, Integer type);

    List<SelectLike> selectLikeUser(Integer userId, Integer id, Integer type, Integer pageNum);

    Boolean putUpStatus(Integer userId, Integer id);

    SelectComment selectOneComment(Integer userId, Integer mainCommentId, Integer replyCommentId);
}
