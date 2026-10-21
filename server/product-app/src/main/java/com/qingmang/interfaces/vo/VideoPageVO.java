package com.qingmang.interfaces.vo;

import com.qingmang.common.api.PageResult;
import java.util.List;
import lombok.Getter;

@Getter
public class VideoPageVO extends PageResult<VideoListItemVO> {
    private final boolean hasMore;
    private final Long cursorId;

    public VideoPageVO(List<VideoListItemVO> records, long total, long pageNum, int pageSize, boolean hasMore) {
        super(records, total, pageNum, pageSize);
        this.hasMore = hasMore;
        this.cursorId = records.isEmpty() ? null : records.get(records.size() - 1).getId();
    }
}
