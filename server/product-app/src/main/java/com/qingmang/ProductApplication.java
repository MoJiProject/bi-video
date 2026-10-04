package com.qingmang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 青芒视频 · 应用入口。
 *
 * <p>Mapper 交给 mybatis-plus 的自动扫描：它从本类的包 {@code com.qingmang} 往下找所有带
 * {@code @Mapper} 的接口。这里不要再加 {@code @MapperScan} —— 两处同时注册会让打包进 jar 的
 * mapper 解析不出泛型返回值，启动直接报 factoryBeanObjectType。</p>
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.qingmang")
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }
}