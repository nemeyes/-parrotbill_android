package com.seerstech.chat.client.room;

import com.seerstech.chat.client.vo.ChatRoom;

import java.util.List;

public interface OnGetRoomListListener {
    public void onSuccess(List<ChatRoom> roomList);
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
