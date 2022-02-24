package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.vo.ChatRoom;

import java.util.List;

public class AsyncCreateRoomEvent extends AsyncTask<AsyncCreateRoomEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
    }

    @Override
    protected Void doInBackground(AsyncCreateRoomEvent.Params... params) {
        AsyncCreateRoomEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onCreateRoomSuccess();
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onCreateRoomFail(param.code, param.message);
        }
        return null;
    }
}
