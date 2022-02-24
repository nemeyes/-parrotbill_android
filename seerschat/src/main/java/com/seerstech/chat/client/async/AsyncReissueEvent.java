package com.seerstech.chat.client.async;

import android.os.AsyncTask;

import com.seerstech.chat.client.ChatClient;
import com.seerstech.chat.client.reissue.ChatAPIEnum;
import com.seerstech.chat.client.reissue.CreateRoomParameter;
import com.seerstech.chat.client.reissue.DownloadFileParameter;
import com.seerstech.chat.client.reissue.FindUserParameter;
import com.seerstech.chat.client.reissue.GetRoomMessageListParameter;
import com.seerstech.chat.client.reissue.GetRoomUserListParameter;
import com.seerstech.chat.client.reissue.InviteUserParameter;
import com.seerstech.chat.client.reissue.LeaveRoomParameter;
import com.seerstech.chat.client.reissue.UploadFileParameter;

public class AsyncReissueEvent extends AsyncTask<AsyncReissueEvent.Params, Void, Void> {
    public static class Params {
        public ChatClient chatClient;
        public ChatClient.OnChatListener observer;
        public int type;
        public String code;
        public String message;
        public String userId;
        public String userNickname;
        public String userRole;
        public ChatAPIEnum apiType;
        public Object apiObject;
    }

    @Override
    protected Void doInBackground(AsyncReissueEvent.Params... params) {
        AsyncReissueEvent.Params param = params[0];
        if (param.type==ChatClient.SUCCESS) {
            param.observer.onReissueSuccess(param.userId, param.userNickname, param.userRole);

            if(param.apiType==ChatAPIEnum.Logout) {
                param.chatClient.logout();

            } else if(param.apiType==ChatAPIEnum.FindUser) {
                FindUserParameter findUserParam = (FindUserParameter) param.apiObject;
                param.chatClient.findUser(findUserParam.userId);

            } else if(param.apiType==ChatAPIEnum.GetRoomList) {
                param.chatClient.getRoomList();

            } else if(param.apiType==ChatAPIEnum.CreateRoom) {
                CreateRoomParameter createRoomParam = (CreateRoomParameter) param.apiObject;
                param.chatClient.createRoom(createRoomParam.roomName, createRoomParam.roomDesc, createRoomParam.userIDs);

            } else if(param.apiType==ChatAPIEnum.GetRoomUserList) {
                GetRoomUserListParameter getRoomUserListParam = (GetRoomUserListParameter) param.apiObject;
                param.chatClient.getRoomUserList(getRoomUserListParam.roomId);

            } else if(param.apiType==ChatAPIEnum.LeaveRoom) {
                LeaveRoomParameter leaveRoomParam = (LeaveRoomParameter) param.apiObject;
                param.chatClient.leaveRoom(leaveRoomParam.roomId);

            } else if(param.apiType==ChatAPIEnum.InviteUser) {
                InviteUserParameter inviteUserParam = (InviteUserParameter) param.apiObject;
                param.chatClient.inviteUser(inviteUserParam.roomId, inviteUserParam.userId);

            } else if(param.apiType==ChatAPIEnum.GetRoomMessageList) {
                GetRoomMessageListParameter getRoomMessageListParam = (GetRoomMessageListParameter) param.apiObject;
                param.chatClient.getRoomMessageList(getRoomMessageListParam.roomId);

            } else if(param.apiType==ChatAPIEnum.UploadFile) {
                UploadFileParameter uploadFileParam = (UploadFileParameter) param.apiObject;
                param.chatClient.uploadFile(uploadFileParam.filePath, uploadFileParam.roomId, uploadFileParam.userId);

            } else if(param.apiType==ChatAPIEnum.DownloadFile) {
                DownloadFileParameter downloadFileParam = (DownloadFileParameter) param.apiObject;
                param.chatClient.downloadFile(downloadFileParam.downloadUrl, downloadFileParam.fileName);

            }

        } else {
            param.observer.onReissueFail(param.code, param.message);
        }
        return null;
    }
}
