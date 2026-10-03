package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仪表盘热门视频
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHotVideoVo implements Serializable {

    private Integer id;
    private String title;
    private String userName;
    private String coverAddress;
    private Integer playNumber;
    private Integer likeNumber;
    private Integer commentNumber;
    private LocalDateTime createTime;

}
