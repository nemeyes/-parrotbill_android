package com.seerstech.chat.client;

import com.seerstech.chat.client.api.MessageClient;
import com.seerstech.chat.client.api.RoomClient;
import com.seerstech.chat.client.api.UserClient;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.message.OnFileDownloadListener;
import com.seerstech.chat.client.message.OnFileUploadListener;
import com.seerstech.chat.client.message.OnGetRoomMessageListListener;
import com.seerstech.chat.client.message.OnMessageReceiverListener;
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
    private final String mRestEndpoint;
    private final String mWSEndpoint;

    private UserClient mUserClient;
    private RoomClient mRoomClient;
    private MessageClient mMessageClient;

    private Object mJWTTokenLock;
    private JWTToken mJWTToken;

    private OnListener mListener;

    public static class OnListener {
        public void onLoginSuccess(String userId, String userNickname, String userRole) {}
        public void onLoginFail(String code, String message) {}
        public void onLogoutSuccess() {}
        public void onLogoutFail(String code, String message) {}
        public void onReissueNeeded() {}
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
        /*
        public void onLoginSuccess(String userId, String userNickname, String userRole);
        public void onLoginFail(String code, String message);
        public void onLogoutSuccess();
        public void onLogoutFail(String code, String message);
        public void onReissueNeeded();
        public void onReissueSuccess(String userId, String userNickname, String userRole);
        public void onReissueFail(String code, String message);
        public void onFindUserSuccess(String userId, String userNickname);
        public void onFindUserFail(String code, String message);
        public void onGetRoomListSuccess(List<ChatRoom> roomList);
        public void onGetRoomListFail(String code, String message);
        public void onCreateRoomSuccess();
        public void onCreateRoomFail(String code, String message);
        public void onGetRoomUserListSuccess(List<ChatUser> userList);
        public void onGetRoomUserListFail(String code, String message);
        public void onLeaveRoomSuccess();
        public void onLeaveRoomFail(String code, String message);
        public void onInviteUserSuccess();
        public void onInviteUserFail(String code, String message);
        public void onGetRoomMessageListSuccess(List<ChatMessage> messageList);
        public void onGetRoomMessageListFail(String code, String message);
        public void onMessageReceive(ChatMessage chatMessage);
        public void onUploadFileSuccess(String fileName, String fileDownloadUrl, String fileType, Long fileSize);
        public void onUploadFileFail(String code, String message);
        public void onDownloadFileSuccess(File file);
        public void onDownloadFileFail(String code, String message);
        */
    }

    public ChatClient(String restEndpoint, String wsEndpoint, OnListener listener) {
        this.mRestEndpoint = restEndpoint;
        this.mWSEndpoint = wsEndpoint;
        this.mListener = listener;
        this.mUserClient = new UserClient(mRestEndpoint);
        this.mRoomClient = new RoomClient(mRestEndpoint);
        this.mMessageClient = new MessageClient(mRestEndpoint, mWSEndpoint);

        mJWTTokenLock = new Object();
    }

    public void setJWTToken(JWTToken token) {
        mJWTToken = token;
    }

    public JWTToken getJWTToken() {
        return mJWTToken;
    }

    public void login(String userId, String userPassword) {
        /*
        final Observable<JWTToken> loginObservable = Observable.create(subscriber -> {
            mUserClient.login(userId, userPassword, new OnLoginListener() {
                @Override
                public void onSuccess(JWTToken token) {
                    if(!subscriber.isDisposed()) {
                        subscriber.onNext(token);
                        subscriber.onComplete();
                    }
                }

                @Override
                public void onFailure(String code, String message) {
                    if(!subscriber.isDisposed()) {
                        subscriber.onError(new ChatClientException(code, message));
                    }
                }
            });
        });

        loginObservable
                .doOnError(e -> {
                    ((ChatClientException)e).getCode();
                    ((ChatClientException)e).getMessage();
                })
                .subscribe(token -> {
                    token.getUserId();
                    token.getUserNickname();
                    token.getUserRole();
                });
        */

        mUserClient.login(userId, userPassword, new OnLoginListener() {
            @Override
            public void onSuccess(JWTToken token) {
                synchronized (mJWTTokenLock) {
                    mJWTToken = token;
                }
                mListener.onLoginSuccess(token.getUserId(), token.getUserNickname(), token.getUserRole());
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onLoginFail(code, message);
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
                    //reissue();
                    mListener.onReissueNeeded();
                } else {
                    mListener.onLogoutSuccess();
                }
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onLogoutFail(code, message);
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
                    mListener.onReissueSuccess(token.getUserId(), token.getUserNickname(), token.getUserRole());
                }
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onReissueFail(code, message);
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
                mListener.onFindUserSuccess(userId, userNickname);
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onFindUserFail(code, message);
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
                mListener.onGetRoomListSuccess(roomList);
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onGetRoomListFail(code, message);
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
                mListener.onCreateRoomSuccess();
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onCreateRoomFail(code, message);
            }
        });
    }

    public void getRoomUserList(String roomId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mRoomClient.getRoomUsers(jwtToken.getGrantType(), jwtToken.getAccessToken(), roomId, new OnGetRoomUserListListener() {
            @Override
            public void onSuccess(String roomId, List<ChatUser> userList) {
                mListener.onGetRoomUserListSuccess(roomId, userList);
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onGetRoomUserListFail(code, message);
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
                mListener.onLeaveRoomSuccess();
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onLeaveRoomFail(code, message);
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
                mListener.onInviteUserSuccess();
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onInviteUserFail(code, message);
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
                mListener.onGetRoomMessageListSuccess(roomId, messageList);
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onGetRoomMessageListFail(code, message);
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
                mListener.onMessageReceive(chatMessage);
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

    public void uploadFile(String filePath, String roomId, String userId) {
        JWTToken jwtToken = null;
        synchronized (mJWTTokenLock) {
            jwtToken = mJWTToken.toBuilder().build();
        }
        mMessageClient.uploadFile(jwtToken.getGrantType(), jwtToken.getAccessToken(), filePath, roomId, userId, new OnFileUploadListener() {
            @Override
            public void onSuccess(String fileName, String fileDownloadUrl, String fileType, Long fileSize) {
                mListener.onUploadFileSuccess(fileName, fileDownloadUrl, fileType, fileSize);
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onUploadFileFail(code, message);
            }
        });
    }

    public void downloadFile(String downloadUrl, String filename) {
        mMessageClient.downloadFile(downloadUrl, filename, new OnFileDownloadListener() {
            @Override
            public void onSuccess(File file) {
                mListener.onDownloadFileSuccess(file);
            }

            @Override
            public void onReissueNeeded() {
                //reissue();
                mListener.onReissueNeeded();
            }

            @Override
            public void onFailure(String code, String message) {
                mListener.onDownloadFileFail(code, message);
            }
        });
    }
}
