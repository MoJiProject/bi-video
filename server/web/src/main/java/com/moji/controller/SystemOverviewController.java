package com.moji.controller;

import com.moji.R;
import com.moji.service.SystemService;
import com.moji.vo.SystemOverviewVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理 - 数据概览仪表盘
 */
@RestController
@RequestMapping("/system/overview")
public class SystemOverviewController {

    @Autowired
    private SystemService systemService;

    /**
     * 获取仪表盘概览数据
     * @param operatorId 操作人id
     * @param token
     * @return
     */
    @GetMapping("/getOverview")
    public R<SystemOverviewVo> getOverview(@RequestParam Integer operatorId,
                                           @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(operatorId, token))
            return R.error("查询失败");

        return R.success(systemService.getOverview());
    }

}
