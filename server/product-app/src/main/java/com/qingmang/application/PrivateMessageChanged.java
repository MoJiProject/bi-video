package com.qingmang.application;

public record PrivateMessageChanged(Long senderId, Long receiverId, Long conversationId,
                                    Long messageId, String kind, Long onlyUserId) {
}
