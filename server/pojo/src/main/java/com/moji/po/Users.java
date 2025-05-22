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
public class Users implements Serializable {

    private Integer id;
    private String userName;
    private String password;
    private Integer gender;//0保密 1男 2女
    private String phone;
    private String avatarAddress;
    private Integer videoNumber;
    private Integer collectNumber;
    private Integer grade;
    private LocalDateTime createTime;
    private Integer coinNumber;
    private String question;
    private Integer dynamicNumber;
    private Integer allMessageNumber;
    private Integer messageNumber;//私信我的数量
    private Integer replyCommentNumber;//回复我的评论消息数量
    private Integer atNumber;///@我的消息数量
    private Integer likeAllNumber;//喜欢我的消息数量
    private String token;
    private Integer exp;
    private Integer ownDynamicNumber;
    private Integer fansNumber;
    private Integer followNumber;
    private String introduce;
    private Integer likeNumber;
    private LocalDateTime loginDateTime;
    private Integer messageWarn;//0关闭 1打开
    private Integer replyMessageWarn;//0关闭 1所有 2关注的人
    private Integer atMessageWarn;//0关闭 1所有 2关注的人
    private Integer likeMessageWarn;//0关闭 1所有 2关注的人
    private String backgroundAddress;
    private String birthday;
    private Integer publicCollect;
    private Integer publicAnime;
    private Integer publicCoin;
    private Integer publicLove;
    private Integer publicFollowList;
    private Integer publicFansList;
    private Integer publicBirthday;
    private Integer historyFlag;
    private Integer adminFlag;

}
