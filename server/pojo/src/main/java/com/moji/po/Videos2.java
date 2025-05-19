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
public class Videos2 implements Serializable {

    private Integer id;
    private Integer userId;
    private String tag;
    private String title;
    private String content;
    private String contentHtml;
    private String videoAddress;
    private Integer likeNumber;
    private Integer commentNumber;
    private Integer collectNumber;//收藏数量
    private Integer shareNumber;
    private LocalDateTime createTime;
    private String coverAddress;//封面
    private Integer coinThrowNumber;
    private Integer allowTwo;//是否允许二创 1是允许
    private Integer type;// 0是自制 1是转载
    private String subZoneKey;//分区的键
    private String subZoneValue;//分区的值
    private Integer playNumber;//播放量
    private Integer status;//作品状态 0是未审核 1是通过 2是未通过
    private Integer newFansNumber;//在该视频下新增粉丝的数量
    private Integer scrollingNumber;//弹幕数量
    private String videoTime;//视频时长
    private String examineFiledMessage;
    private String userName;
    private Integer likeWarn;//用户点赞该视频是否提示
    private Integer waitWatch;


}
