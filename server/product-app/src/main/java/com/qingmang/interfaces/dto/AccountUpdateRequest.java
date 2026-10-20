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
}
