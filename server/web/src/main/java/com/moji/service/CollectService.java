package com.moji.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.dto.CollectDto;
import com.moji.vo.CollectVo;
import com.moji.po.AcceptCollect;
import com.moji.po.Collects;

import java.util.List;

public interface CollectService extends IService<Collects> {
    List<CollectVo> getAllCollect(Integer userId);

    void insertCollect(AcceptCollect acceptCollect);


    Page<CollectDto> getCollectByName(String collectName, Integer homeUserId, Integer userId, Integer pageNum, Integer type, Integer sort, String keyWord);

    boolean deleteFailure(Integer userId);

    boolean deleteCollect(Integer userId, List<Integer> ids);

    boolean controlCollect(Integer userId, List<Integer> collectIds, List<Integer> collectClassifyIds, Integer controls);

    List<CollectDto> selectWaitWatch(Integer userId, Integer sort, Integer sort2, String keyWord, String startTime, String endTime, Integer pageNum);

    int cleanAllWaitWatch(Integer userId);
}
