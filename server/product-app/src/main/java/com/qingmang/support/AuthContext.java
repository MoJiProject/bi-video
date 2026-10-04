package com.qingmang.support;

import cn.dev33.satoken.stp.StpUtil;
import com.qingmang.common.exception.BizException;
import com.qingmang.common.exception.ErrorCode;

/** 当前登录用户上下文。 */
public final class AuthContext {

    private AuthContext() {
    }

    /** 已登录则返回 userId，未登录返回 null。 */
    public static Long currentUserIdOrNull() {
        Object id = StpUtil.getLoginIdDefaultNull();
        return id == null ? null : Long.valueOf(String.valueOf(id));
    }

    /** 未登录直接抛异常。 */
    public static Long requireUserId() {
        Long id = currentUserIdOrNull();
        if (id == null) {
            throw new BizException(ErrorCode.NOT_LOGIN);
        }
        return id;
    }
}