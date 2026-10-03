package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统管理 - 通用操作参数(带原因与批量id)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemOperateDto {

    private Integer operatorId;//操作管理员id
    private String reason;//操作原因 会写入操作日志
    private String ip;//操作来源ip
    private List<Integer> ids;//批量操作的目标id集合

}
