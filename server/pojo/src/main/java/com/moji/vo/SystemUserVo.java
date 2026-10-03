package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理 - 用户列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserVo implements Serializable {

    private Integer id;
    private String userName;
    private String avatarAddress;
    private String phone;
    private Integer gender;
    private LocalDateTime createTime;
    private LocalDateTime loginDateTime;//最近登录时间
    private Integer videoNumber;
    private Integer fansNumber;
    private Integer followNumber;
    private Integer likeNumber;
    private Integer coinNumber;
    private Integer grade;
    private String introduce;
    private Integer adminFlag;//是否管理员
    private Integer banFlag;//是否被封禁
    private String banReason;//封禁原因
    private LocalDateTime banTime;//封禁时间

}
