package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 操作日志查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String module;//按模块筛选 空为全部
    private Integer success;//-1全部 1成功 0失败

}
