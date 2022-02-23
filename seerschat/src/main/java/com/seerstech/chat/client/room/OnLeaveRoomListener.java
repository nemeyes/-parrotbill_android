package com.seerstech.chat.client.room;

public interface OnLeaveRoomListener {
    public void onSuccess();
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
