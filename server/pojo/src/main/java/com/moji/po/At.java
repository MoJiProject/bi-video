package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class At {

    private Integer id;
    private Integer userId;
    private Integer atUserId;
    private Integer videoId;
    private Integer dynamicId;
    private Integer commentId;
}
