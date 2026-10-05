package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 用户搜索条件。 */
@Data
public class UserSearchQuery {

    @Size(max = 64, message = "关键词最长 64 字")
    private String keyword;

    /** default / fans_desc / fans_asc / level_desc / level_asc */
    private String sort = "default";

    private Long pageNum = 1L;

    @Min(1)
    @Max(100)
    private Integer pageSize = 20;
}
