package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 回收站查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemRecycleSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String bizType;//按类型筛选 空为全部
    private Integer status;//-1全部 0在回收站 1已还原
    private String keyword;//标题模糊搜索

}
