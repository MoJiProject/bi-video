package com.moji.dto;

import com.moji.po.CommentControls;
import com.moji.po.Comments;
import com.moji.po.UserInfo2;
import com.moji.po.Videos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyComment implements Serializable {

    private Comments comments;
    private String replyUserName;
    private UserInfo2 userInfo;
    private CommentControls commentControls;

}
