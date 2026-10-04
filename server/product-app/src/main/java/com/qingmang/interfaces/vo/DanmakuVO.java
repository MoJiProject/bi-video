package com.qingmang.interfaces.vo;

import lombok.Data;

@Data
public class DanmakuVO {

    private Long id;
    private Long videoId;
    private String content;
    private String color;
    private Integer fontSize;
    private Integer mode;
    private Integer videoTimeMs;
    private Long userId;
    private String userNickname;
    private Boolean mine = false;
}