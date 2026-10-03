package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.SystemOperateDto;
import com.moji.dto.SystemUserSearchDto;
import com.moji.po.UserBan;
import com.moji.service.SystemService;
import com.moji.vo.SystemUserVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统管理 - 用户管理
 */
@RestController
@RequestMapping("/system/user")
public class SystemUserController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询用户
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchUsers")
    public R<Page<SystemUserVo>> searchUsers(@RequestBody SystemUserSearchDto dto,
                                             @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchUsers(dto));
    }

    /**
     * 设置/取消管理员
     * @param dto
     * @param targetId 目标用户id
     * @param token
     * @return
     */
    @PutMapping("/putAdmin")
    public R<String> putAdmin(@RequestBody SystemOperateDto dto,
                              @RequestParam Integer targetId,
                              HttpServletRequest request,
                              @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.putAdmin(dto, targetId);
        return R.success("修改成功");
    }

    /**
     * 封禁用户
     * @param dto
     * @param targetId 目标用户id
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/banUser")
    public R<String> banUser(@RequestBody SystemOperateDto dto,
                             @RequestParam Integer targetId,
                             HttpServletRequest request,
                             @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.banUser(dto, targetId);
        return R.success("封禁成功，该用户的登录状态已被强制下线");
    }

    /**
     * 解除封禁
     * @param dto
     * @param targetId 目标用户id
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/unbanUser")
    public R<String> unbanUser(@RequestBody SystemOperateDto dto,
                               @RequestParam Integer targetId,
                               HttpServletRequest request,
                               @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.unbanUser(dto, targetId);
        return R.success("解除封禁成功");
    }

    /**
     * 查询封禁记录
     * @param operatorId
     * @param pageNum
     * @param keyword
     * @param token
     * @return
     */
    @GetMapping("/searchBanList")
    public R<Page<UserBan>> searchBanList(@RequestParam Integer operatorId,
                                          @RequestParam(required = false) Integer pageNum,
                                          @RequestParam(required = false) String keyword,
                                          @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(operatorId, token))
            return R.error("查询失败");

        return R.success(systemService.searchBanList(operatorId, pageNum, keyword));
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
