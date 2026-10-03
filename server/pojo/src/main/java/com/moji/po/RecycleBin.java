package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 回收站记录。
 * 删除业务数据前先把内容快照落到这里，支持还原与彻底清除，避免误删无法挽回。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleBin implements Serializable {

    private Integer id;
    private String bizType;//业务类型 video/comment/user
    private Integer bizId;//业务主键id
    private String title;//标题快照
    private String coverAddress;//封面快照
    private String videoAddress;//视频地址快照
    private String payload;//完整数据快照(JSON)
    private String reason;//删除原因
    private Integer status;//0在回收站 1已还原
    private LocalDateTime deleteTime;//进入回收站时间
    private Integer operatorId;
    private String operatorName;
    private LocalDateTime restoreTime;//还原时间

}
