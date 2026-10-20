package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PrivacyUpdateRequest {
    @NotNull
    private Boolean showFavoriteList;
    @NotNull
    private Boolean showFollowList;
    @NotNull
    private Boolean showFansList;
}
