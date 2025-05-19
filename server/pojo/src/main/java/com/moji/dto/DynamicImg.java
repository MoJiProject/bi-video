package com.moji.dto;

import com.moji.po.Comments;
import com.moji.po.Dynamic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicImg implements Serializable {

    private Dynamic dynamic;
    private Integer likeNumber;

}
