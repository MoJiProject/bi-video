package com.qingmang.interfaces.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserLibraryVideoVO extends VideoListItemVO {
    private LocalDateTime addedAt;
    private Boolean available = true;
}
