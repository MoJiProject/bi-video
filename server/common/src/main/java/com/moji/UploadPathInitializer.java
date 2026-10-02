package com.moji;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 启动时解析上传路径配置并初始化 {@link FilePathEnum}。
 * <p>
 * 必须在任何使用上传路径的接口被调用前完成初始化，
 * 因此在容器启动阶段（@PostConstruct）执行。
 */
@Component
public class UploadPathInitializer {

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        FilePathEnum.init(environment);
    }
}
