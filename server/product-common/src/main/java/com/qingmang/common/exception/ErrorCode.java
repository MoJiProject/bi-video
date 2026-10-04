package com.qingmang.common.exception;

/**
 * 业务错误码。前三位分段：1通用 2账号 3内容 4社交 5收藏 9服务端。新增只追加，不复用。
 */
public enum ErrorCode {

    /* 通用 */
    PARAM_INVALID(10001, "参数不合法"),
    OPERATION_FAILED(10002, "操作失败"),
    REQUEST_TOO_FREQUENT(10003, "操作太频繁，请稍后再试"),
    NOT_FOUND(10004, "资源不存在"),

    /* 账号与权限 */
    ACCOUNT_NOT_FOUND(20001, "账号不存在"),
    PASSWORD_ERROR(20002, "账号或密码不正确"),
    ACCOUNT_EXISTS(20003, "账号已被注册"),
    ACCOUNT_DISABLED(20004, "账号已被禁用"),
    NOT_LOGIN(20005, "请先登录"),
    NO_PERMISSION(20006, "没有操作权限"),
    USER_BANNED(20007, "账号已被封禁"),
    PHONE_INVALID(20008, "手机号格式不正确"),
    PHONE_ALREADY_BOUND(20009, "该手机号已绑定其他账号"),

    /* 内容 */
    VIDEO_NOT_FOUND(30001, "视频不存在或已被删除"),
    VIDEO_STATUS_NOT_ALLOW(30002, "当前视频状态不允许该操作"),
    VIDEO_DUPLICATED(30003, "不要重复投稿"),
    COMMENT_NOT_FOUND(31001, "评论不存在或已被删除"),
    COMMENT_CLOSED(31002, "该视频已关闭评论"),
    COMMENT_TOO_LONG(31003, "评论内容过长"),
    CONTENT_SENSITIVE(31004, "内容包含不允许发布的词"),
    POST_NOT_FOUND(32001, "动态不存在或已被删除"),

    /* 社交 */
    FOLLOW_SELF(40001, "不能关注自己"),
    ALREADY_FOLLOWED(40002, "已经关注过了"),
    NOT_FOLLOWED(40003, "尚未关注该用户"),
    ROOM_NOT_FOUND(40004, "房间不存在或已结束"),
    ROOM_FULL(40005, "房间人数已满"),
    ROOM_CODE_ERROR(40006, "房间号错误"),

    /* 收藏与观看记录 */
    FOLDER_NOT_FOUND(50001, "收藏夹不存在"),
    FOLDER_LIMIT_EXCEEDED(50002, "收藏夹数量已达上限"),
    CANNOT_DELETE_DEFAULT_FOLDER(50003, "默认收藏夹不能删除，请先转移内容"),
    FOLDER_NAME_DUPLICATED(50004, "收藏夹名称重复"),
    WATCH_LATER_LIMIT_EXCEEDED(50005, "待看清单已满"),
    HISTORY_VIDEO_NOT_FOUND(50006, "观看记录不存在"),

    /* 服务端 */
    DB_ERROR(90001, "数据库操作失败"),
    UPLOAD_FAILED(90002, "文件上传失败"),
    INTERNAL_ERROR(90000, "服务开小差了，请稍后再试");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}