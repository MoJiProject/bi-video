package com.qingmang.common.enums;

import lombok.Getter;

/** 视频状态，对应 video.status。与库里的注释保持一致。 */
@Getter
public enum VideoStatus {

    /** 草稿，只有作者自己可见。 */
    DRAFT(0, "草稿"),
    /** 已发布，对外可见。 */
    PUBLISHED(1, "已发布"),
    /** 审核中。 */
    REVIEWING(2, "审核中"),
    /** 已下架，只有作者自己可见。 */
    OFF_SHELF(3, "已下架");

    private final int code;
    private final String label;

    VideoStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static VideoStatus of(int code) {
        for (VideoStatus s : values()) {
            if (s.code == code) return s;
        }
        return DRAFT;
    }

    public boolean isPublic() {
        return this == PUBLISHED;
    }

    public static boolean isValid(int code) {
        for (VideoStatus s : values()) {
            if (s.code == code) return true;
        }
        return false;
    }
}