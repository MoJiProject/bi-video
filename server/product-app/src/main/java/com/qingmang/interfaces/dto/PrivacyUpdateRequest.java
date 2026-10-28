package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class PrivacyUpdateRequest {
    private Boolean showFavoriteList;
    private Boolean showFollowList;
    private Boolean showFansList;
    private Boolean showLikeList;
    private Boolean showBirthday;
    private Boolean recordHistory;
    @Size(min = 5, max = 5)
    private List<String> homeSectionOrder;
}
