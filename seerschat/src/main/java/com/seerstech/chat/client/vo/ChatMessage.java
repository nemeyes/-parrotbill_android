package com.seerstech.chat.client.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String messageId;
    private ChatMessageEnum type;
    private String roomId;
    private String userId;
    private String message;
    private String parentMessageId;
    private ChatMessage parentMessage;
    private String mimeType;
    private String downloadPath;
    private Long createdTime;
    private List<ChatUser> participants;
}
