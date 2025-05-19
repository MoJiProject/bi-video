package com.moji.dto;

import com.moji.po.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectLike {

    private Integer id;
    private Likes likes;
    private List<UserInfo2> userInfoList;
    private UserInfo2 userInfo2;
    private Comments comments;
    private Videos videos;
    private LocalDateTime likeTime;
    private Integer isFansFlag;
    private Dynamic dynamic;
}
