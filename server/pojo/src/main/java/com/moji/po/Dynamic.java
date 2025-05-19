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
public class Dynamic implements Serializable {

    private Integer id;
    private Integer fansId;
    private Integer followId;
    private Integer videoId;//发送视频的
    private Integer dynamicId;
    private Integer fansFlag;//1现在是粉丝 0以前是粉丝
    private Integer watchDynamicFlag;//0没有观看过该动态 1观看过
    private Integer waitWatch; //0是没有添加稍后观看
    private Integer commentId;//发送评论的动态
    private String imgAddress;
    private String content;
    private Integer likeNumber;
    private Integer commentNumber;
    private Integer shareNumber;
    private LocalDateTime publishTime;
    private Integer upFlag;//是否置顶
    private String title;
    private Integer likeWarn;


}
