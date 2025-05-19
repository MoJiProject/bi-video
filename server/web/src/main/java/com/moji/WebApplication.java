package com.moji;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ServletComponentScan
//用于启用Servlet组件扫描。当你在Spring Boot应用程序中使用Servlet组件（如Servlet、Filter、Listener）时，
// 通常需要在应用程序的主配置类上添加@ServletComponentScan注解。这样做可以让Spring Boot自动扫描并注册Servlet组件
// ，而无需显式配置在web.xml中。这样可以简化项目的配置和管理，提高开发效率。
@SpringBootApplication
@Slf4j
//开启事务管理注解支持
@EnableTransactionManagement
@EnableCaching//开启springCache
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);

        log.info("bi-video start!");
    }

}
