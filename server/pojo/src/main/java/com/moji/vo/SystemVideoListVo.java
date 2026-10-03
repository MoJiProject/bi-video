package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 系统管理 - 视频列表(带各状态数量，供页签角标展示)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemVideoListVo implements Serializable {

    private List<SystemVideoVo> records;
    private Long total;//当前筛选条件的总条数
    private Long current;
    private Long size;
    private Long waitNumber;//未审核数量
    private Long passNumber;//已通过数量
    private Long rejectNumber;//未通过/已下架数量

}
