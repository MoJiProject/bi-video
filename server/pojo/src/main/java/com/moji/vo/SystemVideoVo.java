package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理 - 视频列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemVideoVo implements Serializable {

    private Integer id;
    private Integer userId;
    private String userName;
    private String userAvatar;//UP主头像
    private String title;
    private String content;//简介
    private String tag;//标签 逗号分隔
    private String coverAddress;
    private String videoAddress;
    private String videoTime;//时长
    private String subZoneKey;//分区
    private String subZoneValue;//子分区
    private Integer type;//0自制 1转载
    private Integer allowTwo;//是否允许二创 1允许
    private Integer status;//0未审核 1已通过 2未通过
    private String examineFiledMessage;//审核不通过原因
    private Integer playNumber;
    private Integer likeNumber;
    private Integer commentNumber;
    private Integer collectNumber;
    private Integer coinThrowNumber;
    private LocalDateTime createTime;

}
