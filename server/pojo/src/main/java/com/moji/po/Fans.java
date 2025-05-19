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
public class Fans implements Serializable {

    private Integer id;
    private Integer userId;
    private String fansUserName;
    private Integer fansId;
    private Integer newFansId;
}
