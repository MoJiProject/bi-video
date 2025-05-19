package com.moji.dto;

import com.moji.po.*;
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
public class HomeDynamicDto implements Serializable {

    private Comments comment;
    private Dynamic dynamic;
    private UserInfo2 upUserInfo2;//视频的UP主信息
    private SelectVideoDto video;
    private String publishTime;
    private Integer likeFlag;
    private List<UserInfo2> userInfo2s;//点赞用户信息
    private Comments replyComment;
    private UserInfo2 replyUserInfo2;//评论用户信息
    private UserInfo2 dynamicUserInfo2;//动态的用户信息

}
