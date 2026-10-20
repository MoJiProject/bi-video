package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class FolderRequest {

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 64, message = "收藏夹名称最长 64 字")
    private String name;

    @Size(max = 255, message = "简介最长 255 字")
    private String description;

    /** 0 私密 1 公开 */
    @Min(0)
    @Max(1)
    private Integer visibility;

    @Size(max = 512, message = "封面地址过长")
    private String coverUrl;
}