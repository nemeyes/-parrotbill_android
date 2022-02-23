package com.seerstech.chat.client.message;

import com.seerstech.chat.client.vo.ChatMessage;

import java.util.List;

public interface OnGetRoomMessageListListener {
    public void onSuccess(String roomId, List<ChatMessage> messageList);
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
