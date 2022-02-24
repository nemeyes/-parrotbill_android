package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

import java.io.File;

public class AsyncDownloadFileEvent extends AsyncTask<AsyncDownloadFileEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public File file;
    }

    @Override
    protected Void doInBackground(AsyncDownloadFileEvent.Params... params) {
        AsyncDownloadFileEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onDownloadFileSuccess(param.file);
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onDownloadFileFail(param.code, param.message);
        }
        return null;
    }
}
