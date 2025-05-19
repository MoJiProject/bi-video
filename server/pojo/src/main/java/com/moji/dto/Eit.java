package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Eit {

    private Integer id;
    private String userName;
    private Integer fansNumber;
    private String avatarAddress;
}
