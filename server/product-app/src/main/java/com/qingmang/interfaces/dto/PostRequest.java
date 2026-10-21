package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PostRequest {

    @Size(max = 20000, message = "动态内容过长")
    private String content;
    @Size(max = 20, message = "动态标题最多 20 字")
    private String title;
    @Size(max = 100)
    private List<@jakarta.validation.constraints.Positive Long> mentionUserIds;

    /** 转发视频时带上 */
    private Long videoId;

    @Size(max = 9, message = "最多 9 张图")
    private List<@NotBlank @Size(max = 512) String> imageUrls;

    /** 0 公开 1 仅自己可见 */
    @Min(0) @Max(1)
    private Integer visibility = 0;
}