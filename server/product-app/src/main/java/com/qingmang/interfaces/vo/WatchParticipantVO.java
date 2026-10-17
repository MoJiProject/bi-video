package com.qingmang.interfaces.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchParticipantVO {

    private Long userId;
    private String userName;
    private String avatarAddress;
    private Integer gender;
    private Integer grade;
    private boolean owner;
    private boolean admin;
    private boolean online;
    private boolean blacklisted;
}
