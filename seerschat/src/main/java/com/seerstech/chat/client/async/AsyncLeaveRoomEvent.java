package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

public class AsyncLeaveRoomEvent extends AsyncTask<AsyncLeaveRoomEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
    }

    @Override
    protected Void doInBackground(AsyncLeaveRoomEvent.Params... params) {
        AsyncLeaveRoomEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onLeaveRoomSuccess();
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onLeaveRoomFail(param.code, param.message);
        }
        return null;
    }
}
