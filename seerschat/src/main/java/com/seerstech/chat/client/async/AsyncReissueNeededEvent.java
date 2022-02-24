package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.reissue.ChatAPIEnum;

public class AsyncReissueNeededEvent extends AsyncTask<AsyncReissueNeededEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient chatClient;
        public ChatAPIEnum apiType;
        public Object apiParameter;
    }

    @Override
    protected Void doInBackground(AsyncReissueNeededEvent.Params... params) {
        AsyncReissueNeededEvent.Params param = params[0];
        param.chatClient.reissue(param.apiType, param.apiParameter);
        return null;
    }
}
