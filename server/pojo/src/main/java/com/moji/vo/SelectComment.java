package com.moji.vo;

import com.moji.dto.ReplyComment;
import com.moji.po.CommentControls;
import com.moji.po.Comments;
import com.moji.po.UserInfo2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectComment implements Serializable {

    private Comments comments;
    private UserInfo2 userInfo;
    private CommentControls commentControls;
    private Long replyNumber;
    private String replyUserName;
    private Integer upLikeFlag;
    private List<ReplyComment> replyCommentList;

}
