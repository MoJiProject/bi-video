package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** 视频列表查询条件。 */
@Data
public class VideoListQuery {

    private Long categoryId;
    private Long ownerId;

    /** 游标：只返回 id 小于它的数据。翻页靠它，不要用大 offset。 */
    private Long cursorId;

    /** hot=最热 / latest=最新 */
    private String sort = "latest";

    private Long pageNum = 1L;

    @Min(1)
    @Max(100)
    private Integer pageSize = 20;
}