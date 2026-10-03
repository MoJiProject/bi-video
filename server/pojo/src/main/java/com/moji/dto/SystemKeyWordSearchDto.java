package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 搜索热词管理查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemKeyWordSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String keyword;//搜索词模糊搜索

}
