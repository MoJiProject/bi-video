package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 近N日趋势数据点
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemTrendVo implements Serializable {

    private String date;//日期 yyyy-MM-dd
    private Long number;//当日数量

}
