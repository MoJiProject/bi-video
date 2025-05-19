package com.moji.dto;

import com.moji.po.Fans;
import com.moji.po.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectUserDto implements Serializable {

    private Integer userId;
    private String userName;
    private String avatarAddress;
    private Integer videoNumber;
    private Integer fansNumber;
    private String introduce;
    private Integer grade;
    private Follow follow;

}
