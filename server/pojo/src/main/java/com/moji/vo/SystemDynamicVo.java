package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理 - 动态列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDynamicVo implements Serializable {

    private Integer id;
    private Integer followId;//发布者id
    private String followUserName;//发布者用户名
    private String followUserAvatar;//发布者头像
    private Integer fansId;//粉丝id 为空表示是up主自己的动态
    private Integer videoId;
    private String videoTitle;//关联视频标题
    private String videoCover;//关联视频封面
    private Integer commentId;
    private String commentContent;//关联评论内容
    private String title;//动态自身标题(图文动态会填)
    private String imgAddress;
    private String content;
    private Integer likeNumber;
    private Integer commentNumber;
    private Integer shareNumber;
    private Integer upFlag;
    private LocalDateTime publishTime;
    private Integer dynamicFlag;//0视频动态 1评论动态 2图文动态

}
