package com.qingmang.interfaces.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class PrivacyUpdateRequest {
    private Boolean showFavoriteList;
    private Boolean showFollowList;
    private Boolean showFansList;
    private Boolean showCoinList;
    private Boolean showLikeList;
    private Boolean showBirthday;
    private Boolean recordHistory;
    @Size(min = 6, max = 6)
    private List<String> homeSectionOrder;
}
