package com.qingmang.interfaces.vo;

import lombok.Data;

import java.time.LocalDateTime;

/** 登录校验只需要这几列，不整个实体查出来。 */
@Data
public class LoginRow {

    private Long userId;
    private String username;
    private String passwordHash;
    private Integer role;
    private Integer status;
    private LocalDateTime deletedAt;
}