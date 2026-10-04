package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FolderRequest {

    @NotBlank(message = "收藏夹名称不能为空")
    @Size(max = 64, message = "收藏夹名称最长 64 字")
    private String name;

    @Size(max = 255, message = "简介最长 255 字")
    private String description;

    /** 0 私密 1 公开 */
    private Integer visibility = 1;
}