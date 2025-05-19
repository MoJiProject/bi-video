package com.moji.vo;

import com.moji.dto.CollectDto;
import com.moji.po.Collects;
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
public class CollectVo implements Serializable {

    private Integer id;
    private Integer collectClassifyId;
    private String collectName;
    private Integer collectNumber;
    private List<CollectDto> collectsList;

}
