package com.qingmang.interfaces.vo;

import lombok.Data;

/**
 * 搜索结果里的用户条目。
 *
 * <p>搜索页有「用户」结果页签，前端要展示头像、昵称、等级、粉丝数、
 * 视频数、签名和关注状态。</p>
 */
@Data
public class UserSearchItemVO {

    private Long userId;
    private String userName;
    /** 头像地址 */
    private String avatarAddress;
    /** 0~6，对应前端 0级.png ~ 6级.png */
    private Integer grade;
    private Long fansNumber;
    private Long videoNumber;
    /** 个人签名，可空 */
    private String introduce;

    /** 当前登录用户是否已关注；null 表示未登录 */
    private Boolean followed;
}
