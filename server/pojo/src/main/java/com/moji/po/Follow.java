package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow implements Serializable {

    private Integer id;
    private String followUserName;
    private Integer userId;
    private Integer followId;


}
