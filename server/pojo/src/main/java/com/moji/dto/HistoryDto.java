package com.moji.dto;

import com.moji.po.History;
import com.moji.po.UserInfo2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryDto implements Serializable {

    private History history;
    private SelectVideoDto selectVideoDto;
    private UserInfo2 userInfo2;

}
