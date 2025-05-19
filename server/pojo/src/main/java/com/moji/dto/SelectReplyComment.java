package com.moji.dto;

import com.moji.po.CommentControls;
import com.moji.po.Comments;
import com.moji.po.Dynamic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectReplyComment implements Serializable {

    private String userName;
    private Integer userId;
    private String avatarAddress;
    private Comments replyComments;
    private Comments mainComments;
    private CommentControls replyCommentControls;
    private Integer isAuthorFlag;//用户是否是该视频的作者
    private Integer id;
    private String coverAddress;
    private String videoTitle;
    private Dynamic dynamic;
}
