package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class AccountUpdateRequest {
    @NotBlank @Size(max = 20)
    private String nickname;
    @Size(max = 250)
    private String signature;
    @NotNull @Min(0) @Max(2)
    private Integer gender;
    @PastOrPresent
    private LocalDate birthday;
    /** 名字颜色：#RRGGBB 或 rainbow；null 表示清除恢复默认黑 */
    @Pattern(regexp = "^#[0-9a-fA-F]{6}$|^rainbow$", message = "名字颜色格式不合法")
    @Size(max = 16)
    private String nameColor;
}
