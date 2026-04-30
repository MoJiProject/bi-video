package com.moji.handler;

import cn.dev33.satoken.exception.NotLoginException;
import com.moji.R;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     */
    @ExceptionHandler(Exception.class)
    public R<String> exceptionHandler(Exception ex){
        log.error("发生异常：", ex);
        return R.error(ex.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<String> exceptionHandler(NotLoginException ex){
        return R.error("您还没有登录");
    }

}
