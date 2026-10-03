package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统管理 - 评论管理查询条件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemCommentSearchDto {

    private Integer operatorId;//操作管理员id
    private Integer pageNum;
    private String keyword;//评论内容的模糊搜索
    private String userId;//评论人ID或昵称，模糊匹配
    private String videoId;//所属视频ID或视频标题，模糊匹配
    private Integer type;//-1全部 0主评论 1回复评论

}
