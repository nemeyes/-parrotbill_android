package com.seerstech.chat.client;

import com.seerstech.chat.client.api.MessageClient;
import com.seerstech.chat.client.api.RoomClient;
import com.seerstech.chat.client.api.UserClient;
import com.seerstech.chat.client.async.AsyncCreateRoomEvent;
import com.seerstech.chat.client.async.AsyncDownloadFileEvent;
import com.seerstech.chat.client.async.AsyncFindUserEvent;
import com.seerstech.chat.client.async.AsyncGetRoomListEvent;
import com.seerstech.chat.client.async.AsyncGetRoomMessageListEvent;
import com.seerstech.chat.client.async.AsyncGetRoomUserListEvent;
import com.seerstech.chat.client.async.AsyncInviteUserEvent;
import com.seerstech.chat.client.async.AsyncLeaveRoomEvent;
import com.seerstech.chat.client.async.AsyncLoginEvent;
import com.seerstech.chat.client.async.AsyncLogoutEvent;
import com.seerstech.chat.client.async.AsyncMessageReceiveEvent;
import com.seerstech.chat.client.async.AsyncReissueEvent;
import com.seerstech.chat.client.async.AsyncReissueNeededEvent;
import com.seerstech.chat.client.async.AsyncUploadFileEvent;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.message.OnFileDownloadListener;
import com.seerstech.chat.client.message.OnFileUploadListener;
import com.seerstech.chat.client.message.OnGetRoomMessageListListener;
import com.seerstech.chat.client.message.OnMessageReceiverListener;
import com.seerstech.chat.client.reissue.ChatAPIEnum;
import com.seerstech.chat.client.reissue.CreateRoomParameter;
import com.seerstech.chat.client.reissue.DownloadFileParameter;
import com.seerstech.chat.client.reissue.FindUserParameter;
import com.seerstech.chat.client.reissue.GetRoomMessageListParameter;
import com.seerstech.chat.client.reissue.GetRoomUserListParameter;
import com.seerstech.chat.client.reissue.InviteUserParameter;
import com.seerstech.chat.client.reissue.LeaveRoomParameter;
import com.seerstech.chat.client.reissue.UploadFileParameter;
import com.seerstech.chat.client.room.OnCreateRoomListener;
import com.seerstech.chat.client.room.OnGetRoomListListener;
import com.seerstech.chat.client.room.OnGetRoomUserListListener;
import com.seerstech.chat.client.room.OnInviteUserListener;
import com.seerstech.chat.client.room.OnLeaveRoomListener;
import com.seerstech.chat.client.user.OnFindUserListener;
import com.seerstech.chat.client.user.OnLoginListener;
import com.seerstech.chat.client.user.OnLogoutListener;
import com.seerstech.chat.client.user.OnReissueListener;
import com.seerstech.chat.client.vo.ChatMessage;
import com.seerstech.chat.client.vo.ChatMessageEnum;
import com.seerstech.chat.client.vo.ChatRoom;
import com.seerstech.chat.client.vo.ChatUser;

import java.io.File;
import java.util.List;

public class ChatClient {
    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int REISSUE_NEEDED = 2;

    private final String mRestEndpoint;
    private final String mWSEndpoint;

    private UserClient mUserClient;
    private RoomClient mRoomClient;
    private MessageClient mMessageClient;

    private Object mJWTTokenLock;
    private JWTToken mJWTToken;

    private OnChatListener mListener;

    public static class OnChatListener {
        public void onLoginSuccess(String userId, String userNickname, String userRole) {}
        public void onLoginFail(String code, String message) {}
        public void onLogoutSuccess() {}
        public void onLogoutFail(String code, String message) {}
        public void onReissueSuccess(String userId, String userNickname, String userRole) {}
        public void onReissueFail(String code, String message) {}
        public void onFindUserSuccess(String userId, String userNickname) {}
        public void onFindUserFail(String code, String message) {}
        public void onGetRoomListSuccess(List<ChatRoom> roomList) {}
        public void onGetRoomListFail(String code, String message) {}
        public void onCreateRoomSuccess() {}
        public void onCreateRoomFail(String code, String message) {}
        public void onGetRoomUserListSuccess(String roomId, List<ChatUser> userList) {}
        public void onGetRoomUserListFail(String code, String message) {}
        public void onLeaveRoomSuccess() {}
        public void onLeaveRoomFail(String code, String message) {}
        public void onInviteUserSuccess() {}
        public void onInviteUserFail(String code, String message) {}
        public void onGetRoomMessageListSuccess(String roomId, List<ChatMessage> messageList) {}
        public void onGetRoomMessageListFail(String code, String message) {}
        public void onMessageReceive(ChatMessage chatMessage) {}
        public void onUploadFileSuccess(String fileName, String fileDownloadUrl, String fileType, Long fileSize) {}
        public void onUploadFileFail(String code, String message) {}
        public void onDownloadFileSuccess(File file) {}
        public void onDownloadFileFail(String code, String message) {}
    }

    public ChatClient(String restEndpoint, String wsEndpoint, OnChatListener listener) {
        this.mRestEndpoint = restEndpoint;
        this.mWSEndpoint = wsEndpoint;
        this.mListener = listener;
        this.mUserClient = new UserClient(mRestEndpoint);
        this.mRoomClient = new RoomClient(mRestEndpoint);
        this.mMessageClient = new MessageClient(mRestEndpoint, mWSEndpoint);

        mJWTTokenLock = new Object();
    }

    public void setListener(OnChatListener listener) {
        mListener = listener;
    }

    public void setJWTToken(JWTToken token) {
        mJWTToken = token;
    }

    public JWTToken getJWTToken() {
        return mJWTToken;
    }

    public void login(String userId, String userPassword) {
        mUserClient.login(userId, userPassword, new OnLoginListener() {
            @Override
            public void onSuccess(JWTToken token) {
                synchronized (mJWTTokenLock) {
                    mJWTToken = token;
                }
                AsyncLoginEvent.Params param = new AsyncLoginEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.userId = token.getUserId();
                param.userNickname = token.getUserNickname();
                param.userRole = token.getUserRole();
                new AsyncLoginEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncLoginEvent.Params param = new AsyncLoginEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncLoginEvent().execute(param);
            }
        });
    }

    public void logout() {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mUserClient.logout(jwtToken.getGrantType(), jwtToken.getAccessToken(), jwtToken.getRefreshToken(), new OnLogoutListener() {
            @Override
            public void onSuccess(boolean reissue) {
                if(reissue) {
                    AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                    param.chatClient = ChatClient.this;
                    param.apiType = ChatAPIEnum.Logout;
                    param.apiParameter = null;
                    new AsyncReissueNeededEvent().execute(param);
                } else {
                    AsyncLogoutEvent.Params param = new AsyncLogoutEvent.Params();
                    param.observer = mListener;
                    param.type = SUCCESS;
                    new AsyncLogoutEvent().execute(param);
                }
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncLogoutEvent.Params param = new AsyncLogoutEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncLogoutEvent().execute(param);
            }
        });
    }

    public void reissue() {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mUserClient.reissue(jwtToken.getGrantType(), jwtToken.getAccessToken(), jwtToken.getRefreshToken(), new OnReissueListener() {
            @Override
            public void onSuccess(JWTToken token) {
                synchronized (mJWTTokenLock) {
                    mJWTToken = token;
                }
                AsyncReissueEvent.Params param = new AsyncReissueEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.userId = token.getUserId();
                param.userNickname = token.getUserNickname();
                param.userRole = token.getUserRole();
                param.apiType = ChatAPIEnum.Unknown;
                param.apiObject = null;
                new AsyncReissueEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncReissueEvent.Params param = new AsyncReissueEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncReissueEvent().execute(param);
            }
        });
    }

    public void reissue(ChatAPIEnum apiType, Object apiParameter) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mUserClient.reissue(jwtToken.getGrantType(), jwtToken.getAccessToken(), jwtToken.getRefreshToken(), new OnReissueListener() {
            @Override
            public void onSuccess(JWTToken token) {
                synchronized (mJWTTokenLock) {
                    mJWTToken = token;
                }
                AsyncReissueEvent.Params param = new AsyncReissueEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.userId = token.getUserId();
                param.userNickname = token.getUserNickname();
                param.userRole = token.getUserRole();
                param.apiType = apiType;
                param.apiObject = apiParameter;
                new AsyncReissueEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncReissueEvent.Params param = new AsyncReissueEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncReissueEvent().execute(param);
            }
        });
    }

    public void findUser(String userId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mUserClient.findUser(jwtToken.getGrantType(), jwtToken.getAccessToken(), userId, new OnFindUserListener() {
            @Override
            public void onSuccess(String userId, String userNickname) {
                AsyncFindUserEvent.Params param = new AsyncFindUserEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.userId = userId;
                param.userNickname = userNickname;
                new AsyncFindUserEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.FindUser;
                param.apiParameter = FindUserParameter.builder().userId(userId);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncFindUserEvent.Params param = new AsyncFindUserEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncFindUserEvent().execute(param);
            }
        });
    }

    public void getRoomList() {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mRoomClient.getRoomList(jwtToken.getGrantType(), jwtToken.getAccessToken(), new OnGetRoomListListener() {
            @Override
            public void onSuccess(List<ChatRoom> roomList) {
                AsyncGetRoomListEvent.Params param = new AsyncGetRoomListEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.roomList = roomList;
                new AsyncGetRoomListEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.GetRoomList;
                param.apiParameter = null;
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncGetRoomListEvent.Params param = new AsyncGetRoomListEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncGetRoomListEvent().execute(param);
            }
        });
    }

    public void createRoom(String roomName, String roomDescription, List<String> userIds) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mRoomClient.createRoom(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomName, roomDescription, userIds, new OnCreateRoomListener() {
            @Override
            public void onSuccess() {
                AsyncCreateRoomEvent.Params param = new AsyncCreateRoomEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                new AsyncCreateRoomEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.CreateRoom;
                param.apiParameter = CreateRoomParameter.builder().roomName(roomName).roomDesc(roomDescription).userIDs(userIds);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncCreateRoomEvent.Params param = new AsyncCreateRoomEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncCreateRoomEvent().execute(param);
            }
        });
    }

    public void getRoomUserList(String roomId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mRoomClient.getRoomUserList(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomId, new OnGetRoomUserListListener() {
            @Override
            public void onSuccess(String roomId, List<ChatUser> userList) {
                AsyncGetRoomUserListEvent.Params param = new AsyncGetRoomUserListEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.roomId = roomId;
                param.userList = userList;
                new AsyncGetRoomUserListEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.GetRoomUserList;
                param.apiParameter = GetRoomUserListParameter.builder().roomId(roomId);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncGetRoomUserListEvent.Params param = new AsyncGetRoomUserListEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncGetRoomUserListEvent().execute(param);
            }
        });
    }

    public void leaveRoom(String roomId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mRoomClient.leaveRoom(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomId, new OnLeaveRoomListener() {
            @Override
            public void onSuccess() {
                AsyncLeaveRoomEvent.Params param = new AsyncLeaveRoomEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                new AsyncLeaveRoomEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.LeaveRoom;
                param.apiParameter = LeaveRoomParameter.builder().roomId(roomId);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncLeaveRoomEvent.Params param = new AsyncLeaveRoomEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncLeaveRoomEvent().execute(param);
            }
        });
    }

    public void inviteUser(String roomId, String userId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mRoomClient.inviteUser(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomId, userId, new OnInviteUserListener() {
            @Override
            public void onSuccess() {
                AsyncInviteUserEvent.Params param = new AsyncInviteUserEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                new AsyncInviteUserEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.InviteUser;
                param.apiParameter = InviteUserParameter.builder().roomId(roomId).userId(userId);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncInviteUserEvent.Params param = new AsyncInviteUserEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncInviteUserEvent().execute(param);
            }
        });
    }

    public void getRoomMessageList(String roomId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mMessageClient.getRoomMessages(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomId, new OnGetRoomMessageListListener() {
            @Override
            public void onSuccess(String roomId, List<ChatMessage> messageList) {
                AsyncGetRoomMessageListEvent.Params param = new AsyncGetRoomMessageListEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.roomId = roomId;
                param.messageList = messageList;
                new AsyncGetRoomMessageListEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.GetRoomMessageList;
                param.apiParameter = GetRoomMessageListParameter.builder().roomId(roomId);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncGetRoomMessageListEvent.Params param = new AsyncGetRoomMessageListEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncGetRoomMessageListEvent().execute(param);
            }
        });
    }

    public void uploadFile(String filePath, String roomId, String userId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mMessageClient.uploadFile(jwtToken.getGrantType(), jwtToken.getAccessToken(), filePath, roomId, userId, new OnFileUploadListener() {
            @Override
            public void onSuccess(String fileName, String fileDownloadUrl, String fileType, Long fileSize) {
                AsyncUploadFileEvent.Params param = new AsyncUploadFileEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.fileName = fileName;
                param.fileDownloadUrl = fileDownloadUrl;
                param.fileType = fileType;
                param.fileSize = fileSize;
                new AsyncUploadFileEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.UploadFile;
                param.apiParameter = UploadFileParameter.builder().filePath(filePath).roomId(roomId).userId(userId);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncUploadFileEvent.Params param = new AsyncUploadFileEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncUploadFileEvent().execute(param);
            }
        });
    }

    public void downloadFile(String downloadUrl, String filename) {
        mMessageClient.downloadFile(downloadUrl, filename, new OnFileDownloadListener() {
            @Override
            public void onSuccess(File file) {
                AsyncDownloadFileEvent.Params param = new AsyncDownloadFileEvent.Params();
                param.observer = mListener;
                param.type = SUCCESS;
                param.file = file;
                new AsyncDownloadFileEvent().execute(param);
            }

            @Override
            public void onReissueNeeded() {
                AsyncReissueNeededEvent.Params param = new AsyncReissueNeededEvent.Params();
                param.chatClient = ChatClient.this;
                param.apiType = ChatAPIEnum.DownloadFile;
                param.apiParameter = DownloadFileParameter.builder().downloadUrl(downloadUrl).fileName(filename);
                new AsyncReissueNeededEvent().execute(param);
            }

            @Override
            public void onFailure(String code, String message) {
                AsyncDownloadFileEvent.Params param = new AsyncDownloadFileEvent.Params();
                param.observer = mListener;
                param.type = FAIL;
                param.code = code;
                param.message = message;
                new AsyncDownloadFileEvent().execute(param);
            }
        });
    }

    public void beginChatMessage(String roomId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mMessageClient.beginChatMessage(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomId, new OnMessageReceiverListener() {
            @Override
            public void onMessageReceive(ChatMessage chatMessage) {
                AsyncMessageReceiveEvent.Params param = new AsyncMessageReceiveEvent.Params();
                param.observer = mListener;
                param.message = chatMessage;
                new AsyncMessageReceiveEvent().execute(param);
            }
        });
    }

    public void endChatMessage() {
        mMessageClient.endChatMessage();
    }

    public void sendMessage(String roomId, String message) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mMessageClient.sendMessage(jwtToken.getGrantType(), jwtToken.getAccessToken(), ChatMessageEnum.MSG_TALK, roomId, message);
    }

    public void sendComment(String roomId, String message, String parentMessageId) {
        mMessageClient.sendComment(ChatMessageEnum.MSG_TALK, roomId, message, parentMessageId);
    }
}
