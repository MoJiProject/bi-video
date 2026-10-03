package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理 - 回收站列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemRecycleBinVo implements Serializable {

    private Integer id;//回收站记录id
    private String bizType;//video/comment
    private Integer bizId;//业务主键id
    private String title;
    private String coverAddress;
    private String videoAddress;
    private String reason;
    private Integer status;//0在回收站 1已还原
    private LocalDateTime deleteTime;
    private Integer operatorId;
    private String operatorName;
    private LocalDateTime restoreTime;
    //列表展示用的补充信息
    private String ownerName;//归属用户
    private Integer relatedNumber;//关联数据数量(评论/收藏等)

}
