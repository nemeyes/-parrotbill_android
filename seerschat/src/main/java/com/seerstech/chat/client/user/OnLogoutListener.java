package com.seerstech.chat.client.user;

public interface OnLogoutListener {
    public void onSuccess(boolean reissue);
    public void onFailure(String code, String message);
}
