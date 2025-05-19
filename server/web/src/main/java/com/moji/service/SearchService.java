package com.moji.service;


import com.moji.dto.SelectUserDto;
import com.moji.dto.SelectVideoDto;
import com.moji.dto.AcceptSearchDto;

import java.util.List;

public interface SearchService {
    List<SelectVideoDto> selectVideoByKeyWord(AcceptSearchDto acceptSearchData);

    List<SelectUserDto> selectUserByKeyWord(AcceptSearchDto keyWord);
}
