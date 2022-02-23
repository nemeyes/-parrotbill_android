package com.seerstech.chat.client.room;

import com.seerstech.chat.client.vo.ChatUser;

import java.util.List;

public interface OnGetRoomUserListListener {
    public void onSuccess(String roomId, List<ChatUser> userList);
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
