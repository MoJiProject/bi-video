package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 动态管理查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemDynamicSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String keyword;//动态标题/正文或发布者昵称的模糊搜索
    private Integer type;//-1全部 0视频动态 1评论动态
    private String userId;//发布者ID或昵称，模糊匹配
    //来源 -1全部 0创作者自己发布的 1粉丝收到的副本
    private Integer source;

}
