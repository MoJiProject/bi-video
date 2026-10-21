package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BannerRequest {
    @NotBlank(message = "请上传Banner图片")
    @Size(max = 512, message = "图片地址过长")
    private String imageUrl;
    @Size(max = 200, message = "内容不能超过200个字符")
    private String content;
    @Size(max = 2000, message = "链接不能超过2000个字符")
    private String linkUrl;
    @NotNull
    @Min(0)
    @Max(999999)
    private Integer sortOrder;
    @NotNull
    private Boolean enabled;
}
