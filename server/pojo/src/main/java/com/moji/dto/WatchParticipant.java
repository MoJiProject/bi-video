package com.moji.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WatchParticipant {

    private Integer userId;
    private String userName;
    private String avatarAddress;
    private Integer gender;
    private Integer grade;
    private boolean owner;
    private boolean admin;
    private boolean online;
    private boolean blacklisted;
}
