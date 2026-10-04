package com.qingmang.common.exception;

import com.qingmang.common.api.ApiResponse;
import com.qingmang.common.api.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/** 全局异常处理：任何异常都不把堆栈或 SQL 泄漏给前端，同一种异常返回结构一致。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String TRACE_ID = "traceId";

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBiz(BizException e, HttpServletRequest request) {
        log.warn("[biz] {} {} -> {} {}", request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return ApiResponse.<Void>fail(e.getCode(), e.getMessage()).withTraceId(MDC.get(TRACE_ID));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), "缺少必要参数：" + e.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), "请求参数格式不正确");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethod(HttpRequestMethodNotSupportedException e) {
        return ApiResponse.fail(ErrorCode.PARAM_INVALID.getCode(), "不支持的请求方法：" + e.getMethod());
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NoHandlerFoundException e) {
        return ApiResponse.fail(ErrorCode.NOT_FOUND.getCode(), "接口不存在");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Void> handleDuplicateKey(DuplicateKeyException e, HttpServletRequest request) {
        log.warn("[dup] {} {} -> {}", request.getMethod(), request.getRequestURI(), e.getMessage());
        return ApiResponse.fail(ErrorCode.OPERATION_FAILED.getCode(), "重复操作，请勿提交过快");
    }

    @ExceptionHandler(Throwable.class)
    public ApiResponse<Void> handleUnexpected(Throwable e, HttpServletRequest request) {
        log.error("[unexpected] {} {}", request.getMethod(), request.getRequestURI(), e);
        return ApiResponse.<Void>fail(ErrorCode.INTERNAL_ERROR.getCode(), ErrorCode.INTERNAL_ERROR.getMessage())
                .withTraceId(MDC.get(TRACE_ID));
    }

    public static <T> ApiResponse<PageResult<T>> page(PageResult<T> pageResult) {
        return ApiResponse.ok(pageResult);
    }
}