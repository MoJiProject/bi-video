package com.qingmang.common.enums;

import lombok.Getter;

/**
 * 视频状态。
 *
 * <p>对应 {@code video.status}。老代码里 0/1 各表示什么在不同方法里不一样，
 * 这里定死一份含义，DB 注释与本枚举保持一致。</p>
 */
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

    /** 是否对外可见。 */
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