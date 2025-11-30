package com.moji.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moji.R;
import com.moji.dto.SelectUserDto;
import com.moji.dto.SelectVideoDto;
import com.moji.dto.AcceptSearchDto;
import com.moji.service.SearchService;
import com.moji.vo.ResponseSearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {


    @Autowired
    private SearchService searchService;

    /**
     * 根据搜索词查询视频
     * @param acceptSearchData
     * @return
     */
    @PostMapping("/searchVideoByKeyWord")
    public R<ResponseSearchVo> selectVideoByKeyWord(@RequestBody AcceptSearchDto acceptSearchData){
        return R.success(searchService.selectVideoByKeyWord(acceptSearchData));
    }

    /**
     * 根据搜索词查询用户
     * @param acceptSearchData
     * @return
     */
    @PostMapping("/selectUsers")
    public R<ResponseSearchVo> selectUserDto(@RequestBody AcceptSearchDto acceptSearchData){
        return R.success(searchService.selectUserByKeyWord(acceptSearchData));
    }




}
