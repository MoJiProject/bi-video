package com.moji.controller;

import com.moji.R;
import com.moji.dto.SystemOperateDto;
import com.moji.dto.SystemVideoSearchDto;
import com.moji.service.SystemService;
import com.moji.vo.SystemVideoListVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 系统管理 - 视频管理/审核
 */
@RestController
@RequestMapping("/system/video")
public class SystemVideoController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询视频(含各状态数量)
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchVideos")
    public R<SystemVideoListVo> searchVideos(@RequestBody SystemVideoSearchDto dto,
                                              @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchVideos(dto));
    }

    /**
     * 获取已有分区列表
     * @param operatorId
     * @param token
     * @return
     */
    @GetMapping("/getSubZoneKeys")
    public R<List<String>> getSubZoneKeys(@RequestParam Integer operatorId,
                                          @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(operatorId, token))
            return R.error("查询失败");

        return R.success(systemService.getSubZoneKeys());
    }

    /**
     * 审核通过
     * @param dto
     * @param videoId
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/examineVideo")
    public R<String> examineVideo(@RequestBody SystemOperateDto dto,
                                  @RequestParam Integer videoId,
                                  HttpServletRequest request,
                                  @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.examineVideo(dto, videoId);
        return R.success("审核通过");
    }

    /**
     * 审核退回(仅限未审核的视频)
     * @param dto
     * @param videoId
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/rejectVideo")
    public R<String> rejectVideo(@RequestBody SystemOperateDto dto,
                                 @RequestParam Integer videoId,
                                 HttpServletRequest request,
                                 @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.rejectVideo(dto, videoId);
        return R.success("已退回");
    }

    /**
     * 强制下架已通过的视频
     * @param dto
     * @param videoId
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/takeDownVideo")
    public R<String> takeDownVideo(@RequestBody SystemOperateDto dto,
                                   @RequestParam Integer videoId,
                                   HttpServletRequest request,
                                   @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.takeDownVideo(dto, videoId);
        return R.success("已强制下架，相关动态与计数已同步回滚");
    }

    /**
     * 删除视频(支持单个或批量)
     * @param dto ids为待删除的视频id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/deleteVideo")
    public R<Map<String, Integer>> deleteVideo(@RequestBody SystemOperateDto dto,
                                               HttpServletRequest request,
                                               @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.deleteVideos(dto, dto.getIds());
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
