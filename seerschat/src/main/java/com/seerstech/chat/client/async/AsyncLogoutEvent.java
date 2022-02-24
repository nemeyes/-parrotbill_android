package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

public class AsyncLogoutEvent extends AsyncTask<AsyncLogoutEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
    };

    @Override
    protected Void doInBackground(AsyncLogoutEvent.Params... params) {
        AsyncLogoutEvent.Params param = params[0];
        if(param.type==ChatClient.SUCCESS) {
            param.observer.onLogoutSuccess();
        } else if(param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onLogoutFail(param.code, param.message);
        }
        return null;
    }
}
