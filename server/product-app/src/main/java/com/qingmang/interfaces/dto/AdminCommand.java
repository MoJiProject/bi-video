package com.qingmang.interfaces.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminCommand {
    // Search keywords use their natural string primary key; other modules require positive numeric IDs.
    private List<String> ids;
    private String reason;
    private Boolean adminFlag;
    private Long receiverId;
    private Integer messageType;
    private String content;
}
