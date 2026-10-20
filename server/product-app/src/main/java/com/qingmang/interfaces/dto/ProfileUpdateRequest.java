package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotNull
    @Size(max = 250, message = "签名最长 250 字")
    private String signature;
}
