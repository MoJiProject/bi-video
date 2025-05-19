package com.moji.dto;

import com.moji.po.UserInfo2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectPrivateMessage implements Serializable {

    private List<PrivateMessageDto> privateMessage;
    private UserInfo2 userInfo;
    private UserInfo2 dialogueUserInfo;
    private Integer messageNumber;
}
