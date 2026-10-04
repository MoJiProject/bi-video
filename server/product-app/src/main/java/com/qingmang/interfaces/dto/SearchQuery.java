package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchQuery {

    @Size(max = 64, message = "关键词最长 64 字")
    private String keyword;

    /** 分区ID，留空表示全部分类 */
    private Long categoryId;

    /** newest 按发布时间 / hot 按播放量 */
    private String sort = "hot";

    private Long cursorId;

    private Long pageNum = 1L;

    @Min(1)
    @Max(100)
    private Integer pageSize = 20;
}