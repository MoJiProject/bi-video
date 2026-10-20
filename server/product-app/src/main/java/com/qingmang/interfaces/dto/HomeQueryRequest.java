package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HomeQueryRequest {
    @NotNull
    @Positive
    private Long homeUserId;
    @Min(1)
    @Max(10000)
    private int pageNum = 1;
    @Min(1)
    @Max(3)
    private int sort = 1;
    @Min(1)
    @Max(3)
    private int type = 1;
    @Size(max = 50)
    private String keyWord;
    @Size(max = 64)
    private String collectName;
}
