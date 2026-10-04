package com.qingmang.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/** 用户账号 */
@Data
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    @TableField("password_hash")
    private String passwordHash;

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("background_url")
    private String backgroundUrl;

    private String signature;

    private Integer gender;

    private LocalDate birthday;

    private String phone;

    private String email;

    private Integer role;

    private Integer status;

    private Integer level;

    private Integer exp;

    @TableField("coin_balance")
    private Integer coinBalance;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
