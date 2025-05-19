package com.moji.dto;

import com.moji.po.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectAt {

    private At at;
    private Integer userId;
    private String userName;
    private String avatarAddress;
    private Comments mainComments;
    private Comments comments;
    private CommentControls commentControls;
    private Integer isAuthorFlag;//用户是否是该视频的作者
    private Videos videos;
    private Dynamic dynamic;

}
