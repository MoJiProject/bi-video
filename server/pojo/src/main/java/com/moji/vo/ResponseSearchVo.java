package com.moji.vo;

import com.moji.dto.SelectUserDto;
import com.moji.dto.SelectVideoDto;
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
public class ResponseSearchVo implements Serializable {

    private Long videoTotal;
    private Long userTotal;
    private List<SelectVideoDto> selectVideoDtoList;
    private List<SelectUserDto> selectUserDtoList;


}
