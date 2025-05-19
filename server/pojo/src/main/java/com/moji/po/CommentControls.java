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
public class CommentControls implements Serializable {

    private Integer id;
    private Integer commentId;
    private Integer userId;
    private Integer videoId;
    private Integer dynamicId;
    private Integer controls;//0是踩 1是喜欢
    private LocalDateTime likeCommentTime;
}
