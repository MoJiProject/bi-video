package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DanmakuPostRequest {

    @NotNull(message = "缺少视频ID")
    private Long videoId;

    @NotBlank(message = "弹幕内容不能为空")
    @Size(max = 100, message = "弹幕最多 100 字")
    private String content;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色必须是 #RRGGBB")
    private String color;

    @Min(12)
    @Max(48)
    private Integer fontSize;

    @Min(0)
    @Max(4)
    private Integer mode;

    private Integer videoTimeMs;
}