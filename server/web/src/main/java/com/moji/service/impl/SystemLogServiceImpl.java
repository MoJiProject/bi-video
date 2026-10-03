package com.moji.service.impl;

import com.moji.dto.SystemOperateDto;
import com.moji.mapper.SystemOperationLogMapper;
import com.moji.mapper.UserMapper;
import com.moji.po.SystemOperationLog;
import com.moji.po.Users;
import com.moji.service.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 系统管理 - 操作审计日志服务实现
 */
@Slf4j
@Service
public class SystemLogServiceImpl implements SystemLogService {

    @Autowired
    private SystemOperationLogMapper systemOperationLogMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 用独立事务写日志，保证业务回滚时审计记录仍然保留
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void write(SystemOperateDto dto, String module, String action, String targetType,
                      Integer targetId, String targetName, String detail, Integer success) {

        if (dto == null || dto.getOperatorId() == null)
            return;

        try {
            systemOperationLogMapper.insert(SystemOperationLog.builder()
                    .operatorId(dto.getOperatorId())
                    .operatorName(operatorName(dto.getOperatorId()))
                    .module(module)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetName(truncate(targetName, 500))
                    .detail(truncate(detail, 2000))
                    .success(success)
                    .ip(dto.getIp())
                    .createTime(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.error("写入操作日志失败 module={} action={} err={}", module, action, e.getMessage());
        }
    }

    private String operatorName(Integer operatorId) {
        Users operator = userMapper.selectById(operatorId);
        return operator == null ? null : operator.getUserName();
    }

    /**
     * 截断超长文本，避免超出字段长度导致日志写入失败
     */
    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength)
            return value;
        return value.substring(0, maxLength);
    }

}
