package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicDto implements Serializable {

    private Integer userId;
    private Integer videoId;//根据id进行降序排列
    private String userName;
    private String avatarAddress;
    private String coverAddress;
    private String title;
    private String createTime;
    private Integer newDynamicNumber;//标识  0是历史动态 其他值为新发布没有看过的动态
    private Integer waitWatch;

}
