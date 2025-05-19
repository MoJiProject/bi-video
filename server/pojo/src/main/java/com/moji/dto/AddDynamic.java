package com.moji.dto;

import com.moji.po.At;
import com.moji.po.Dynamic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDynamic {

    private Dynamic dynamic;
    private List<At> ats;
}
