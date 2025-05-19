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
public class Dialogue implements Serializable {

    private Integer id;
    private Integer userId;
    private Integer dialogueId;
    private Integer dnd;//是否免打扰 0否1是
    private String newContent;//最新消息
    private LocalDateTime upDateTime;
    private String sign;//拼接标识 从小到大根据id拼接 message的发送人id和接收人id
}
