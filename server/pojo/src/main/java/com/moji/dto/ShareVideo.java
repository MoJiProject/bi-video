package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareVideo {

    private Integer userId;
    private List<Integer> userIdList;
    private Integer videoId;
    private String content;

}
