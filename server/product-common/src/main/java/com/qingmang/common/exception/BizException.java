package com.qingmang.common.exception;

import java.io.Serial;

/** 业务异常。Service 层遇到可预期的失败抛这个，由 GlobalExceptionHandler 统一转换。 */
public class BizException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** 业务异常本身不是程序缺陷，按 WARN 级别记，不要打整段堆栈。 */
    public boolean isLogAsWarn() {
        return true;
    }

    public int getCode() {
        return code;
    }

    public static BizException of(ErrorCode errorCode) {
        return new BizException(errorCode);
    }
}