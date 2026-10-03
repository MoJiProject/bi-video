package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 私信管理查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String keyword;//私信内容的模糊搜索
    private String userId;//发送人ID或昵称，模糊匹配
    private String receiverId;//接收人ID或昵称，模糊匹配

}
