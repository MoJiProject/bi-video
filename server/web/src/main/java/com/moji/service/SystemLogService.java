package com.moji.service;

import com.moji.dto.SystemOperateDto;

/**
 * 系统管理 - 操作审计日志服务
 * 独立成 Bean 是为了让写入日志走独立事务，
 * 这样即便业务操作因异常回滚，被拒绝的操作依然能被记录
 */
public interface SystemLogService {

    /**
     * 写入一条操作审计日志，写入失败不影响主流程
     * @param dto 操作人信息
     * @param module 所属模块
     * @param action 操作动作
     * @param targetType 操作对象类型
     * @param targetId 操作对象id
     * @param targetName 操作对象描述
     * @param detail 补充说明
     * @param success 1成功 0失败
     */
    void write(SystemOperateDto dto, String module, String action, String targetType,
               Integer targetId, String targetName, String detail, Integer success);

}
