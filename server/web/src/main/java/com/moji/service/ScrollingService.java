package com.moji.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moji.po.Scrolling;

import java.util.List;

public interface ScrollingService extends IService<Scrolling> {
    Boolean insterScrolling(Scrolling scrolling);

    List<Scrolling> selectScrollingList(Integer videoId);

    Boolean revocationScrolling(Integer scrollingId, String token);
}
