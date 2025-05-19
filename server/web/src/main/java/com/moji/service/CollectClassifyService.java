package com.moji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.po.CollectsClassify;

import java.util.List;

public interface CollectClassifyService extends IService<CollectsClassify> {

    List<CollectsClassify> getCollectsClassify(Integer homeUserId, Integer userId);


    boolean addCollectClassify(CollectsClassify collectsClassify);

    boolean deleteCollectClassify(Integer id);

    boolean putCollectClassify(CollectsClassify collectsClassify);
}
