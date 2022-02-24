package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;

public class AsyncUploadFileEvent extends AsyncTask<AsyncUploadFileEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public String fileName;
        public String fileDownloadUrl;
        public String fileType;
        public Long fileSize;
    }

    @Override
    protected Void doInBackground(AsyncUploadFileEvent.Params... params) {
        AsyncUploadFileEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onUploadFileSuccess(param.fileName, param.fileDownloadUrl, param.fileType, param.fileSize);
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onUploadFileFail(param.code, param.message);
        }
        return null;
    }
}
