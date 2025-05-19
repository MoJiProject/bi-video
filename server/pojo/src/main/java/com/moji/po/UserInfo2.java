package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo2 implements Serializable {

    private Integer id;
    private String userName;
    private Integer gender;
    private String avatarAddress;
    private Integer grade;
    private Integer fansNumber;
    private Integer followNumber;
    private Integer likeNumber;
    private String introduce;
    private Integer isFansFlag;
    private Integer isFollowFlag;
    private Integer allMessageNumber;
    private Integer messageNumber;//私信我的数量
    private Integer replyCommentNumber;//回复我的评论消息数量
    private Integer atNumber;///@我的消息数量
    private Integer likeAllNumber;//喜欢我的消息数量
    private Integer messageWarn;//0关闭 1打开
    private Integer replyMessageWarn;//0关闭 1所有 2关注的人
    private Integer atMessageWarn;//0关闭 1所有 2关注的人
    private Integer likeMessageWarn;//0关闭 1所有 2关注的人
    private String backgroundAddress;
    private String birthday;
    private Integer playNumber;
    private Integer publicCollect;
    private String phone;
    private Integer publicAnime;
    private Integer publicCoin;
    private Integer publicLove;
    private Integer publicFollowList;
    private Integer publicFansList;
    private Integer publicBirthday;
    private Integer newFans;
    private Integer historyFlag;
    private Integer newDynamicFlag;//是否有新的动态


}
