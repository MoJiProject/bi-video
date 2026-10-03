package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 视频管理查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemVideoSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String keyword;//标题或UP主昵称的模糊搜索
    private String subZoneKey;//分区筛选 空为全部
    private Integer status;//-1全部 0未审核 1已通过 2未通过/已下架
    private Integer userId;//按UP主筛选 空为全部
    private Integer sortWay;//0最新发布 1播放量最多 2点赞最多

}
