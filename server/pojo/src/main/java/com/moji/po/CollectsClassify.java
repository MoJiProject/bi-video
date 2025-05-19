package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectsClassify implements Serializable {

    private Integer id;
    private Integer userId;
    private String coverAddress;
    private String collectName;
    private Integer status;//0私密 1公开
    private String content;
    private Integer videoNumber;



}
