package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCollectClassify{

    private Integer id;
    private Integer userId;
    private String coverAddress;
    private String collectName;
    private Integer status;
    private String content;
    private Integer videoNumber;
    private Boolean flag;
}
