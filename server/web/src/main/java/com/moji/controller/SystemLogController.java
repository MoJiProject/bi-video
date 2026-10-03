package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.SystemLogSearchDto;
import com.moji.po.SystemOperationLog;
import com.moji.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理 - 操作日志审计
 */
@RestController
@RequestMapping("/system/log")
public class SystemLogController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询操作日志
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchLogs")
    public R<Page<SystemOperationLog>> searchLogs(@RequestBody SystemLogSearchDto dto,
                                                  @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchLogs(dto));
    }

    /**
     * 统计符合条件的日志条数
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/countLogs")
    public R<Long> countLogs(@RequestBody SystemLogSearchDto dto,
                             @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.countLogs(dto));
    }

}
