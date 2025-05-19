package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Collects implements Serializable {

    private Integer id;
    private Integer videoId;
    private Integer userId;
    private String collectName;
    private LocalDateTime collectTime;
    private Integer deleteFlag;


}
