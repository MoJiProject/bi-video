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
public class UsersVideosVo implements Serializable {

    private UserInfo2 users;
    private List<Videos> videos;
    private long videoSuccessNumber;
    private long videoErrorNumber;
    private long videoWaitNumber;
    private long videoAllNumber;
    private long total;

}
