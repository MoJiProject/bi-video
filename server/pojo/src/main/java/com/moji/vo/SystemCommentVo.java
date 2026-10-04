package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理 - 评论列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemCommentVo implements Serializable {

    private Integer id;
    private Integer userId;
    private String userName;
    private String userAvatar;//评论人头像
    private Integer videoId;
    private String videoTitle;//所属视频标题
    private Integer dynamicId;
    private String content;//评论正文，本身就是富文本HTML，前端需用v-html渲染
    private LocalDateTime commentTime;
    private Integer likeCommentNumber;
    private String imgAddress;
    private Integer replyCommentId;//回复的评论id
    private String replyUserName;//被回复人用户名
    private Integer mainCommentId;
    private Integer upFlag;
    private Integer deleteSign;//用户自己删除标记 0正常 1已删除
    private Integer status;//管理员下架标记 0正常 1已下架
    private Integer dynamicFlag;

}
