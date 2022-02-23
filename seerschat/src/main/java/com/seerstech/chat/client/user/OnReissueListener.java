package com.seerstech.chat.client.user;

import com.seerstech.chat.client.jwt.JWTToken;

public interface OnReissueListener {
    public void onSuccess(JWTToken token);
    public void onFailure(String code, String message);
}
