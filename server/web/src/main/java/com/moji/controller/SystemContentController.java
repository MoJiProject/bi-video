package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.SystemCommentSearchDto;
import com.moji.dto.SystemDynamicSearchDto;
import com.moji.dto.SystemMessageSearchDto;
import com.moji.dto.SystemOperateDto;
import com.moji.service.SystemService;
import com.moji.vo.SystemCommentVo;
import com.moji.vo.SystemDynamicVo;
import com.moji.vo.SystemMessageVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统管理 - 评论/动态/私信内容管理
 */
@RestController
@RequestMapping("/system/content")
public class SystemContentController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询评论
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchComments")
    public R<Page<SystemCommentVo>> searchComments(@RequestBody SystemCommentSearchDto dto,
                                                   @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchComments(dto));
    }

    /**
     * 删除评论(支持单个或批量)
     * @param dto ids为待删除的评论id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/deleteComment")
    public R<Map<String, Integer>> deleteComment(@RequestBody SystemOperateDto dto,
                                                 HttpServletRequest request,
                                                 @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.deleteComments(dto, dto.getIds());
        return R.success(Map.of("deleteNumber", number));
    }

    /**
     * 分页查询动态
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchDynamics")
    public R<Page<SystemDynamicVo>> searchDynamics(@RequestBody SystemDynamicSearchDto dto,
                                                   @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchDynamics(dto));
    }

    /**
     * 删除动态(支持单个或批量)
     * @param dto ids为待删除的动态id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/deleteDynamic")
    public R<Map<String, Integer>> deleteDynamic(@RequestBody SystemOperateDto dto,
                                                 HttpServletRequest request,
                                                 @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.deleteDynamics(dto, dto.getIds());
        return R.success(Map.of("deleteNumber", number));
    }

    /**
     * 分页查询私信
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchMessages")
    public R<Page<SystemMessageVo>> searchMessages(@RequestBody SystemMessageSearchDto dto,
                                                   @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchMessages(dto));
    }

    /**
     * 删除私信(支持单个或批量)
     * @param dto ids为待删除的私信id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/deleteMessage")
    public R<Map<String, Integer>> deleteMessage(@RequestBody SystemOperateDto dto,
                                                 HttpServletRequest request,
                                                 @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.deleteMessages(dto, dto.getIds());
        return R.success(Map.of("deleteNumber", number));
    }

    /**
     * 取真实客户端ip，兼容反向代理
     * @param request
     * @return
     */
    private String resolveIp(HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip))
            return ip.split(",")[0].trim();
        return request.getRemoteAddr();
    }

}
