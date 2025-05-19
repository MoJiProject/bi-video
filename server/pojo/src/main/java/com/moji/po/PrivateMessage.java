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
public class PrivateMessage implements Serializable {

    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private LocalDateTime sendTime;
    private Integer status;//0是未读 1是已读 2是撤回
    private String content;
    private String selectSign;//查找标识 从小到大根据id拼接 message的发送人id和接收人id 用于查找聊天数据
    private Integer deleteSign;//存储要删除人的id
    private Integer messageType;//1是文字消息 2是图片 3是视频

}
