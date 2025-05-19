package com.moji.dto;

import com.moji.po.History;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptHistory {

    private History history;
    private String token;
    private String userIp;
}
