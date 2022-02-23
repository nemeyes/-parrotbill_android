package com.seerstech.chat.client.message;

import com.seerstech.chat.client.vo.ChatMessage;

public interface OnMessageReceiverListener {
    public void onMessageReceive(ChatMessage chatMessage);
}
