package com.moji.service;
import com.moji.dto.AcceptSearchDto;
import com.moji.vo.ResponseSearchVo;

public interface SearchService {
    ResponseSearchVo selectVideoByKeyWord(AcceptSearchDto acceptSearchData);

    ResponseSearchVo selectUserByKeyWord(AcceptSearchDto keyWord);
}
