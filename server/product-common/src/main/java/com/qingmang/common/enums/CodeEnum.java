package com.qingmang.common.enums;

/** 带数字编码的枚举实现这个接口，方便在 Service / MyBatis 之间互转。 */
public interface CodeEnum {

    int getCode();
}