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
public class AcceptSearchDto implements Serializable {

    private Integer userId;
    private String keyWord;
    private Integer sort;
    private Integer date;
    private Integer time;
    private String classify;
    private String startTime;
    private String endTime;
    private Integer userSort;
    private String classifyIndex;
}
