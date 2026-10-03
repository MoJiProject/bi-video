package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.SystemKeyWordSearchDto;
import com.moji.dto.SystemOperateDto;
import com.moji.po.KeyWord;
import com.moji.service.SystemService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 系统管理 - 搜索热词管理
 */
@RestController
@RequestMapping("/system/keyWord")
public class SystemKeyWordController {

    @Autowired
    private SystemService systemService;

    /**
     * 分页查询搜索热词
     * @param dto
     * @param token
     * @return
     */
    @PostMapping("/searchKeyWords")
    public R<Page<KeyWord>> searchKeyWords(@RequestBody SystemKeyWordSearchDto dto,
                                           @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("查询失败");

        return R.success(systemService.searchKeyWords(dto));
    }

    /**
     * 新增搜索热词
     * @param dto word为新增的搜索词
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/addKeyWord")
    public R<String> addKeyWord(@RequestBody SystemOperateDto dto,
                                @RequestParam String word,
                                HttpServletRequest request,
                                @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.addKeyWord(dto, word);
        return R.success("新增成功");
    }

    /**
     * 修改搜索热词
     * @param dto word为修改后的搜索词
     * @param keyWordId
     * @param request
     * @param token
     * @return
     */
    @PutMapping("/putKeyWord")
    public R<String> putKeyWord(@RequestBody SystemOperateDto dto,
                                @RequestParam Integer keyWordId,
                                @RequestParam String word,
                                HttpServletRequest request,
                                @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.putKeyWord(dto, keyWordId, word);
        return R.success("修改成功");
    }

    /**
     * 修改搜索热词次数
     * @param dto count为修改后的搜索次数
     * @param keyWordId
     * @param request
     * @param token
     * @return
     */
    @PutMapping("/putKeyWordCount")
    public R<String> putKeyWordCount(@RequestBody SystemOperateDto dto,
                                     @RequestParam Integer keyWordId,
                                     @RequestParam Integer count,
                                     HttpServletRequest request,
                                     @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        systemService.putKeyWordCount(dto, keyWordId, count);
        return R.success("修改成功");
    }

    /**
     * 删除搜索热词(支持单个或批量)
     * @param dto ids为待删除的搜索词id集合
     * @param request
     * @param token
     * @return
     */
    @PostMapping("/deleteKeyWord")
    public R<Map<String, Integer>> deleteKeyWord(@RequestBody SystemOperateDto dto,
                                                 HttpServletRequest request,
                                                 @RequestHeader("Authorization") String token) {

        if (!systemService.checkAdmin(dto.getOperatorId(), token))
            return R.error("操作失败");

        dto.setIp(resolveIp(request));
        Integer number = systemService.deleteKeyWords(dto, dto.getIds());
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
