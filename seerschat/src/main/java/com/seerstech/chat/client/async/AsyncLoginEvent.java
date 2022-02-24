package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

public class AsyncLoginEvent extends AsyncTask<AsyncLoginEvent.Params, Void, Void>  {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public String userId;
        public String userNickname;
        public String userRole;
    };

    @Override
    protected Void doInBackground(AsyncLoginEvent.Params... params) {
        AsyncLoginEvent.Params param = params[0];
        if(param.type==ChatClient.SUCCESS) {
            param.observer.onLoginSuccess(param.userId, param.userNickname, param.userRole);
        } else {
            param.observer.onLoginFail(param.code, param.message);
        }
        return null;
    }
}
