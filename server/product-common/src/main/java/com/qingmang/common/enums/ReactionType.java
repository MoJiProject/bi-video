package com.qingmang.common.enums;

import lombok.Getter;

/**
 * 视频互动类型。
 *
 * <p>对应 {@code video_reaction.reaction_type}。点赞与投币合并成一张表后，
 * 用这个枚举替代散落的 {@code 1=点赞 2=投币 3=收藏} 注释。</p>
 */
@Getter
public enum ReactionType {

    LIKE(1, "点赞"),
    COIN(2, "投币"),
    FAVORITE(3, "收藏");

    private final int code;
    private final String label;

    ReactionType(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static ReactionType of(int code) {
        for (ReactionType t : values()) {
            if (t.code == code) return t;
        }
        return null;
    }
}