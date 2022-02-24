package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

public class AsyncFindUserEvent extends AsyncTask<AsyncFindUserEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public String userId;
        public String userNickname;
    }

    @Override
    protected Void doInBackground(AsyncFindUserEvent.Params... params) {
        AsyncFindUserEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onFindUserSuccess(param.userId, param.userNickname);
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onFindUserFail(param.code, param.message);
        }
        return null;
    }
}
