package com.qingmang.common.exception;

import java.io.Serial;

/**
 * 业务异常。
 *
 * <p>Service 层遇到「可预期的失败」一律抛这个，由
 * {@link com.qingmang.common.web.GlobalExceptionHandler} 统一转成响应体。
 * 只有「不可预期」的才让它抛成运行时异常走兜底分支。</p>
 *
 * <p>不要再用 {@code new BaseException("...")} 那种只有一句话的做法：
 * 那样既没法按错误码分支，错误文案也散落在各处。</p>
 */
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