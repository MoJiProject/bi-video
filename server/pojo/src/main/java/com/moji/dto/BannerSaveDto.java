package com.moji.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BannerSaveDto extends SystemOperateDto {
    private String imageUrl;
    private String content;
    private String linkUrl;
    private Integer sortOrder;
    private Integer enabled;
}
