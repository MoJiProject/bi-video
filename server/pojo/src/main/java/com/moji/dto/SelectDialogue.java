package com.moji.dto;

import com.moji.po.Dialogue;
import com.moji.po.UserInfo2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectDialogue implements Serializable {

    private Dialogue dialogue;
    private UserInfo2 userInfo;
    private Integer notReadNumber;//没有读的数量
    private Integer allMessageNumber;
}
