package com.qingmang.common.enums;

/**
 * 带数字编码的枚举统一实现这个接口，
 * 方便在 Service / MyBatis 之间互转，不用到处写 {@code switch}。
 */
public interface CodeEnum {

    int getCode();
}