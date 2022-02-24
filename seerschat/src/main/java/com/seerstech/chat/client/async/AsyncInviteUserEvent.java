package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

public class AsyncInviteUserEvent extends AsyncTask<AsyncInviteUserEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
    }

    @Override
    protected Void doInBackground(AsyncInviteUserEvent.Params... params) {
        AsyncInviteUserEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onInviteUserSuccess();
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onInviteUserFail(param.code, param.message);
        }
        return null;
    }
}
