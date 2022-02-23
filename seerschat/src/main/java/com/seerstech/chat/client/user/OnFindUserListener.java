package com.seerstech.chat.client.user;

public interface OnFindUserListener {
    public void onSuccess(String userId, String userNickname);
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
