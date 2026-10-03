package com.moji.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理 - 私信列表项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageVo implements Serializable {

    private Integer id;
    private Integer senderId;
    private String senderName;
    private String senderAvatar;
    private Integer receiverId;
    private String receiverName;
    private String receiverAvatar;//接收人头像
    private String content;
    private LocalDateTime sendTime;
    private Integer status;//0未读 1已读 2撤回
    private Integer messageType;//1文字 2图片 3视频

}
