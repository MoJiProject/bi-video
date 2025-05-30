package com.moji.dto;

import com.moji.po.Collects;
import com.moji.po.Videos;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectDto implements Serializable {

    private Collects collects;
    private Videos videos;
    private SelectVideoDto selectVideoDto;

}
