package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.vo.ChatMessage;

import java.io.File;

public class AsyncMessageReceiveEvent extends AsyncTask<AsyncMessageReceiveEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public ChatMessage message;
    }

    @Override
    protected Void doInBackground(AsyncMessageReceiveEvent.Params... params) {
        AsyncMessageReceiveEvent.Params param = params[0];
        param.observer.onMessageReceive(param.message);
        return null;
    }
}
