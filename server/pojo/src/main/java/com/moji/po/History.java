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
public class History implements Serializable {

    private Integer id;
    private Integer userId;
    private Integer videoId;
    private Integer watchVideoFlag;
    private LocalDateTime watchVideoDate;
    private String watchVideoTime;
    private Integer watchCurrentTime;

}
