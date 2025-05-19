package com.moji.po;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comments implements Serializable {

    private Integer id;
    private Integer userId;
    private String userName;
    private Integer videoId;
    private Integer dynamicId;
    private String content;
    private LocalDateTime commentTime;
    private Integer likeCommentNumber;
    private String imgAddress;
    private Integer dynamicFlag;//0没有添到加动态 1添加到动态 2是评论被删除但是动态留着(评论不会被查到)
    private String dislike;
    private Integer replyCommentId;//回复的评论id
    private Integer mainCommentId;//在哪条评论回复的
    private Integer deleteSign;//删除回复评论标识
    private Integer replyUserId;
    private Integer notificationReplyFlag;//用于通知回复评论 0否 1是
    private Integer likeWarn;//是否消息提示
    private Integer upFlag;//是否置顶
}
