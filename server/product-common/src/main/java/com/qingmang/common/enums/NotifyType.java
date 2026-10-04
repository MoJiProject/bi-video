package com.qingmang.common.enums;

import lombok.Getter;

/**
 * 通知类型与通知对象类型。
 *
 * <p>对应 {@code notification.type} / {@code notification.target_type}。
 * 旧的 {@code at} 表只能表示「@我」，这里扩展成完整的通知体系。</p>
 */
public final class NotifyType {

    private NotifyType() {
    }

    @Getter
    public enum Type implements CodeEnum {
        MENTION(1, "@我"),
        REPLY(2, "回复我"),
        VIDEO_COMMENT(3, "评论我的视频"),
        VIDEO_LIKE(4, "点赞我的视频"),
        COMMENT_LIKE(5, "点赞我的评论"),
        FOLLOW(6, "关注我"),
        SYSTEM(7, "系统通知");

        private final int code;
        private final String label;

        Type(int code, String label) {
            this.code = code;
            this.label = label;
        }
    }

    @Getter
    public enum Target implements CodeEnum {
        VIDEO(1, "视频"),
        COMMENT(2, "评论"),
        POST(3, "动态"),
        USER(4, "用户");

        private final int code;
        private final String label;

        Target(int code, String label) {
            this.code = code;
            this.label = label;
        }
    }
}