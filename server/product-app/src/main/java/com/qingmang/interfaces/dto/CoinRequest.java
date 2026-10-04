package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CoinRequest {

    @Min(value = 1, message = "单次至少投 1 枚")
    @Max(value = 2, message = "单次最多投 2 枚")
    private Integer coinCount = 1;
}