package com.qingmang.interfaces.dto;

import lombok.Data;

@Data
public class AdminQuery {
    private Integer pageNum = 1;
    private String keyword;
    private Integer type = -1;
    private Integer loginFlag = -1;
    private String userId;
    private String receiverId;
    private String videoId;
    private String subZoneKey;
    private Integer status = -1;
    private Integer sortWay = 0;
    private String module;
    private Integer success;
}
