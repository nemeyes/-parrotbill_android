package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.vo.ChatRoom;

import java.util.List;

public class AsyncGetRoomListEvent extends AsyncTask<AsyncGetRoomListEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public List<ChatRoom> roomList;
    }

    @Override
    protected Void doInBackground(AsyncGetRoomListEvent.Params... params) {
        AsyncGetRoomListEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onGetRoomListSuccess(param.roomList);
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onGetRoomListFail(param.code, param.message);
        }
        return null;
    }
}
