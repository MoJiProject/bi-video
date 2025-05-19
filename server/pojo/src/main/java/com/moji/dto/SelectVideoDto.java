package com.moji.dto;

import io.swagger.models.auth.In;
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
public class SelectVideoDto implements Serializable {


    private Integer videoId;
    private Integer userId;
    private String userName;
    private Integer videoPlayNumber;
    private Integer videoScrollingNumber;
    private Integer videoShareNumber;
    private Integer videoCommentNumber;
    private Integer videoLikeNumber;
    private String second;
    private String minutes;
    private String hour;
    private String content;
    private String contentHtml;
    private String createTime;
    private String videoTitle;
    private String coverAddress;
    private String videoAddress;
    private Integer collectNumber;
    private Integer waitWatch;
    private String videoTime;
    private Integer watchCurrentTime;


}
