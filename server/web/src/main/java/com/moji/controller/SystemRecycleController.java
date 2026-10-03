package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.SystemOperateDto;
import com.moji.dto.SystemRecycleSearchDto;
import com.moji.service.SystemService;
import com.moji.vo.SystemRecycleBinVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统管理 - 回收站
 * 删除先进回收站，可还原；确认不再需要再彻底清除
 */
@RestController
@RequestMapping("/system/recycle")
public class SystemRecycleController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询回收站
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchRecycleBin")
    public R<Page<SystemRecycleBinVo>> searchRecycleBin(@RequestBody SystemRecycleSearchDto dto,
                                                       @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchRecycleBin(dto));
    }

    /**
     * 把视频移入回收站
     * @param dto ids为待删除的视频id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/recycleVideo")
    public R<Map<String, Integer>> recycleVideo(@RequestBody SystemOperateDto dto,
                                               HttpServletRequest request,
                                               @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.recycleVideos(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 还原视频
     * @param dto ids为待还原的视频id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/restoreVideo")
    public R<Map<String, Integer>> restoreVideo(@RequestBody SystemOperateDto dto,
                                                HttpServletRequest request,
                                                @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.restoreVideos(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 把评论移入回收站
     * @param dto ids为待删除的评论id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/recycleComment")
    public R<Map<String, Integer>> recycleComment(@RequestBody SystemOperateDto dto,
                                                  HttpServletRequest request,
                                                  @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.recycleComments(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 还原评论
     * @param dto ids为待还原的评论id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/restoreComment")
    public R<Map<String, Integer>> restoreComment(@RequestBody SystemOperateDto dto,
                                                  HttpServletRequest request,
                                                  @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.restoreComments(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 把动态移入回收站
     * @param dto ids为待删除的动态id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/recycleDynamic")
    public R<Map<String, Integer>> recycleDynamic(@RequestBody SystemOperateDto dto,
                                                  HttpServletRequest request,
                                                  @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.recycleDynamics(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 还原动态
     * @param dto ids为待还原的动态id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/restoreDynamic")
    public R<Map<String, Integer>> restoreDynamic(@RequestBody SystemOperateDto dto,
                                                  HttpServletRequest request,
                                                  @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.restoreDynamics(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 彻底清除(不可恢复)
     * @param dto ids为回收站记录id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/purgeRecycleBin")
    public R<Map<String, Integer>> purgeRecycleBin(@RequestBody SystemOperateDto dto,
                                                   HttpServletRequest request,
                                                   @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.purgeRecycleBin(dto, dto.getIds());
        return R.success(Map.of("number", number));
    }

    /**
     * 清理已还原的记录
     * @param dto
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/cleanRestored")
    public R<Map<String, Integer>> cleanRestored(@RequestBody SystemOperateDto dto,
                                                 HttpServletRequest request,
                                                 @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.cleanRestoredRecycleBin(dto);
        return R.success(Map.of("number", number));
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
