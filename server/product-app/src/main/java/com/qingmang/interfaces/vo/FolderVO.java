package com.qingmang.interfaces.vo;

import lombok.Data;

@Data
public class FolderVO {

    private Long id;
    private Long ownerId;
    private String name;
    private String description;
    private String coverUrl;
    private Integer visibility;
    private Boolean isDefault;
    private Integer sortOrder;
    private Integer itemCount;
}