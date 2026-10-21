package com.qingmang.application;

import com.qingmang.interfaces.vo.DanmakuVO;

public record DanmakuChanged(Long videoId, DanmakuVO danmaku, Long removedId, long count) {
}
