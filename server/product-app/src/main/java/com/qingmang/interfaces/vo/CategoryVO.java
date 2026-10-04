package com.qingmang.interfaces.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CategoryVO implements Serializable {

    private Long id;
    private String code;
    private String name;
    private Integer sortOrder;
}