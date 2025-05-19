package com.moji.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);

        registry.addInterceptor(new SaInterceptor(handle->{
            SaRouter
                    .match("/**")
                    .notMatch("/user/login","/user/sign","/user/forget","/user/checkLogin/**",
                            "/user/checkLoginFlag/**","/keyWord/**","/search/**","/userIp/**","/user/userInfo/**",
                            "/video/selectVideo/**","/video/updateVideoPlay","/video/selectByVideoId","/video/updateVideoPlay/**",
                            "/video/getVideoPageByVideo","/history/addHistory","/video/getVideoTitle/**",
                            "/home/*", "/comment/selectCommentByVideoId","/user/selectUserInfoById","/dynamic/selectDynamicById/**",
                            "/dynamic/likeDynamicUser/**"
                    )
                    .check(StpUtil::checkLogin);
        })).addPathPatterns("/**");
    }
}
