package com.moji.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户封禁记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBan implements Serializable {

    private Integer id;
    private Integer userId;//被封禁用户id
    private String userName;//被封禁用户名快照
    private String reason;//封禁原因
    private Integer status;//1封禁中 0已解除
    private Integer operatorId;//操作管理员id
    private String operatorName;//操作管理员名快照
    private LocalDateTime banTime;//封禁时间
    private LocalDateTime unbanTime;//解除时间

}
