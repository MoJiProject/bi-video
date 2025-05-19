package com.moji.vo;

import com.moji.po.UserInfo2;
import com.moji.po.Videos;
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
public class SelectVideoByIdVo implements Serializable {

    private UserInfo2 upUser;
    private Videos upVideo;
    private Integer userId;
    private Integer isFansFlag;
    private Boolean likeVideoClickFlag;
    private Boolean videoThrowCoinClickFlag;
    private Boolean videoShareClickFlag;
    private Boolean videoCollectClickFlag;
    private List<String> collectName;
    private Integer throwCoinResult; //0是硬币不足 1是已投币
    private Integer throwCoinNumber;
    private Integer controlsType;//操作类型 1点赞2投币3三连

}
