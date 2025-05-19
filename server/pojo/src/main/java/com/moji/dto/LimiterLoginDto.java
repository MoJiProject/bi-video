package com.moji.dto;

import com.moji.po.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimiterLoginDto {

    private Users user;
    private String userIp;
}
