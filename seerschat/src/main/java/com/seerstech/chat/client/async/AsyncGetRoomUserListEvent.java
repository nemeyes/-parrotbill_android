package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.vo.ChatUser;

import java.util.List;

public class AsyncGetRoomUserListEvent extends AsyncTask<AsyncGetRoomUserListEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public String roomId;
        public List<ChatUser> userList;
    }

    @Override
    protected Void doInBackground(AsyncGetRoomUserListEvent.Params... params) {
        AsyncGetRoomUserListEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onGetRoomUserListSuccess(param.roomId, param.userList);
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onGetRoomUserListFail(param.code, param.message);
        }
        return null;
    }
}
