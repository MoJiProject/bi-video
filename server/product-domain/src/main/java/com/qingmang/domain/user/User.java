package com.qingmang.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户账号
 *
 * <p>由 sql/product/01_schema.sql 生成，对应表 {@code user}。</p>
 */
@Data
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，唯一 */
    private String username;

    /** 密码哈希，禁止存明文 */
    @TableField("password_hash")
    private String passwordHash;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    @TableField("avatar_url")
    private String avatarUrl;

    /** 主页背景图地址 */
    @TableField("background_url")
    private String backgroundUrl;

    /** 个性签名 */
    private String signature;

    /** 性别 0未知 1男 2女 */
    private Integer gender;

    /** 生日 */
    private LocalDate birthday;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 角色 0普通用户 1管理员 */
    private Integer role;

    /** 状态 0禁用 1正常 */
    private Integer status;

    /** 等级 0-6 */
    private Integer level;

    /** 当前等级经验 */
    private Integer exp;

    /** 硬币余额（可负，投币会扣） */
    @TableField("coin_balance")
    private Integer coinBalance;

    /** 注册时间 */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /** 最后登录时间 */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /** 软删除时间，NULL 表示未删除 */
    @TableLogic
    @TableField("deleted_at")
    private LocalDateTime deletedAt;
}
