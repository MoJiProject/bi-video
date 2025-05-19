package com.moji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.HistoryDto;
import com.moji.dto.SelectHistoryDto;
import com.moji.po.History;
import com.moji.po.Videos;

import java.util.List;

public interface HistoryService extends IService<History> {
    List<SelectHistoryDto> getHistory(Integer userId);

    void addHistory(String history);

    History getHistryById(Videos videos);

    int deleteAllHistory(Integer userId);

    List<HistoryDto> selectHistoryList(Integer userId, Integer pageNum, Integer sort, String keyWord, String startTime, String endTime);

    boolean deleteHistory(Integer userId, List<Integer> ids);
}
