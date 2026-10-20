package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {

    @Size(max = 1000, message = "动态正文最长 1000 字")
    private String content;

    /** 转发视频时带上 */
    private Long videoId;

    @Size(max = 9, message = "最多 9 张图")
    private List<@NotBlank @Size(max = 512) String> imageUrls;

    /** 0 公开 1 仅自己可见 */
    @Min(0) @Max(1)
    private Integer visibility = 0;
}