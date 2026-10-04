package com.qingmang.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一响应体。code == 1 成功，字段名与历史接口保持一致，前端无需改动。
 *
 * @param <T> 业务数据类型
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int CODE_SUCCESS = 1;
    /** 通用失败 */
    public static final int CODE_FAILURE = 0;

    private int code;
    private String msg;
    private T data;
    private String traceId;
    private long timestamp;

    public ApiResponse() {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = CODE_SUCCESS;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> fail(String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = CODE_FAILURE;
        r.msg = msg;
        return r;
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.msg = msg;
        return r;
    }

    public ApiResponse<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}