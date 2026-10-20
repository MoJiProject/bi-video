package com.moji.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Banner implements Serializable {
    private Integer id;
    private String imageUrl;
    private String content;
    private String linkUrl;
    private Integer sortOrder;
    private Integer enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
