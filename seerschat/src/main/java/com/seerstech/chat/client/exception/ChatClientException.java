package com.seerstech.chat.client.exception;


import lombok.Getter;

@Getter
public class ChatClientException extends Exception {
    private String code;

    public ChatClientException(String code, String message) {
        super(message);
        this.code = code;
    }
}
