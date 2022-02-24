package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.vo.ChatMessage;
import com.seerstech.chat.client.vo.ChatUser;

import java.util.List;

public class AsyncGetRoomMessageListEvent extends AsyncTask<AsyncGetRoomMessageListEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public String roomId;
        public List<ChatMessage> messageList;
    }

    @Override
    protected Void doInBackground(AsyncGetRoomMessageListEvent.Params... params) {
        AsyncGetRoomMessageListEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onGetRoomMessageListSuccess(param.roomId, param.messageList);
        } else if (param.type==ChatClient.REISSUE_NEEDED) {
            //param.observer.onReissueNeeded();
        } else {
            param.observer.onGetRoomMessageListFail(param.code, param.message);
        }
        return null;
    }
}
