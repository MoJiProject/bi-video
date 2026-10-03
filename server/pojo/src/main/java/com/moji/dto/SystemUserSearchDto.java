package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 用户管理查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemUserSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String keyword;//用户名/手机号模糊搜索
    private Integer type;//-1全部 0普通用户 1管理员 2已封禁
    private Integer loginFlag;//-1全部 0最近7天未登录 1最近7天已登录

}
