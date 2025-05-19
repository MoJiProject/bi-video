package com.moji.controller;

import com.moji.R;
import com.moji.serve.LoginLimiterServer;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/userIp")
public class UserIpController {


    /**
     * 获取用户ip地址
      * @param request
     * @return
     */
    @GetMapping("/getUserIp")
    public R<Map<String,String>> getIp(HttpServletRequest request){

        String userIp = getClientIp(request);
        Map<String,String> map=new HashMap<>();
        map.put("userIp",userIp);
        LoginLimiterServer limiterServer=new LoginLimiterServer();
        String token = limiterServer.isAutoLogin(userIp);
        map.put("token",token);

        return R.success(map);
    }

    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 可能是多个 IP，第一个是用户真实 IP
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
