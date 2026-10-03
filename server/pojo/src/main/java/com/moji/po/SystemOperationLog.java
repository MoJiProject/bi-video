package com.moji.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台操作审计日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOperationLog implements Serializable {

    private Integer id;
    private Integer operatorId;//操作人id(管理员)
    private String operatorName;//操作人用户名快照
    //操作人头像，查询时从 users 表带出来，不落库
    @TableField(exist = false)
    private String operatorAvatar;
    private String module;//所属模块 user/comment/dynamic/message/keyWord
    private String action;//操作动作 如 deleteComment
    private String targetType;//操作对象类型
    private Integer targetId;//操作对象id
    private String targetName;//操作对象描述快照
    private String detail;//补充说明 如删除原因
    private Integer success;//1成功 0失败
    private String ip;//操作来源ip
    private LocalDateTime createTime;//操作时间

}
