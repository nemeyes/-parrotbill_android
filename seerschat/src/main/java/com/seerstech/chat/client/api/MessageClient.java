package com.seerstech.chat.client.api;

import android.net.Uri;
import android.util.Log;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.JSONObjectBody;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.seerstech.chat.client.error.ErrorMessage;
import com.seerstech.chat.client.message.OnFileDownloadListener;
import com.seerstech.chat.client.message.OnFileUploadListener;
import com.seerstech.chat.client.message.OnGetRoomMessageListListener;
import com.seerstech.chat.client.message.OnMessageReceiverListener;
import com.seerstech.chat.client.vo.ChatMessage;
import com.seerstech.chat.client.vo.ChatMessageEnum;
import com.seerstech.chat.client.vo.ChatUser;

import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.dto.StompCommand;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageClient {
    private final String mUri;
    private final String mWSUri;
    private final String SUCCESS = "CODE_SUCCESS";
    private final String TAG = "MessageClient";

    private StompClient mStompClient;
    private CompositeDisposable mCompositeDisposable;
    private OnMessageReceiverListener mMessageObserver;

    public MessageClient(String uri, String wsUri) {
        this.mUri = uri;
        this.mWSUri = wsUri;
    }

    public void getRoomMessages(String grantType, String accessToken, String roomId, OnGetRoomMessageListListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/messages";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("room_id", roomId);
        } catch (Exception e) {}

        final OnGetRoomMessageListListener getRoomMessageListObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    if(source.code()==200) {
                        try {
                            String code = result.getString("code");
                            String message = result.getString("message");
                            if (code.equals(SUCCESS)) {
                                String roomId = result.getString("room_id");
                                JSONArray messages = result.getJSONArray("messages");
                                ArrayList<ChatMessage> messageList = null;
                                if (messages.length() > 0) {
                                    messageList = new ArrayList<ChatMessage>();
                                }
                                for (int i = 0; i < messages.length(); i++) {

                                    JSONObject obj = messages.getJSONObject(i);
                                    ChatMessage currentMessage = parseMessageJSONObject(obj);
                                    if (currentMessage != null) {
                                        messageList.add(currentMessage);
                                    }
                                }
                                getRoomMessageListObserver.onSuccess(roomId, messageList);

                            } else {
                                getRoomMessageListObserver.onFailure(code, message);
                            }
                        } catch (JSONException jsonException) {
                            getRoomMessageListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMMESSAGELIST);
                        }
                    } else {
                        getRoomMessageListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMMESSAGELIST);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        getRoomMessageListObserver.onReissueNeeded();
                    } else {
                        getRoomMessageListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMMESSAGELIST);
                    }
                }
            }
        });
    }

    public void beginChatMessage(String grantType, String accessToken, String roomId, OnMessageReceiverListener msgObserver) {

        mMessageObserver = msgObserver;
        mStompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, this.mWSUri + "/seers/websocket");
        mStompClient.withClientHeartbeat(10000).withServerHeartbeat(10000);
        resetSubscriptions();

        Disposable dispLifecycle = mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            //toast("Stomp connection opened");
                            Log.i(TAG, "Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            //toast("Stomp connection error");
                            break;
                        case CLOSED:
                            //toast("Stomp connection closed");
                            Log.e(TAG, "Stomp connection closed", lifecycleEvent.getException());
                            resetSubscriptions();
                            break;
                        case FAILED_SERVER_HEARTBEAT:
                            Log.e(TAG, "Stomp server heartbeat", lifecycleEvent.getException());
                            //toast("Stomp failed server heartbeat");
                            break;
                    }
                });
        mCompositeDisposable.add(dispLifecycle);

        Disposable dispTopic = mStompClient.topic("/sub/chat/room/" + roomId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Received " + topicMessage.getPayload());
                    JSONObject obj = new JSONObject(topicMessage.getPayload());
                    ChatMessage chatMessage = parseMessageJSONObject(obj);
                    mMessageObserver.onMessageReceive(chatMessage);
                }, throwable -> {
                    Log.e(TAG, "Error on subscribe topic", throwable);
                });

        mCompositeDisposable.add(dispTopic);

        List<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("Authorization", grantType + ' ' + accessToken));
        mStompClient.connect(headers);
    }

    public void endChatMessage() {
        if(mStompClient!=null) {
            mStompClient.disconnect();
        }
        mMessageObserver = null;
    }

    public void sendMessage(String grantType, String accessToken, ChatMessageEnum type, String roomId, String message) {
        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("type", type.toString());
            obj.put("room_id", roomId);
            obj.put("message", message);

            List<StompHeader> headers = new ArrayList<>();
            headers.add(new StompHeader(StompHeader.DESTINATION, "/pub/chat/message"));
            headers.add(new StompHeader("Authorization", grantType + ' ' + accessToken));
            StompMessage stompMessage = new StompMessage(StompCommand.SEND, headers, obj.toString());
            mCompositeDisposable.add(mStompClient.send(stompMessage)
                    .compose(applySchedulers())
                    .subscribe(() -> {
                        Log.d(TAG, "STOMP echo send successfully");
                    }, throwable -> {
                        Log.e(TAG, "Error send STOMP echo", throwable);
                        //toast(throwable.getMessage());
                    }));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendComment(ChatMessageEnum type, String roomId, String message, String parentMessageId) {
        //ws.send("/pub/chat/message", {"Authorization":this.grant_type + ' ' + this.access_token}, JSON.stringify({type:type, room_id:this.room_id, message:this.message, parent_message_id:this.message_id}));

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("type", type.toString());
            obj.put("room_id", roomId);
            obj.put("message", message);
            obj.put("parent_message_id", parentMessageId);

            mCompositeDisposable.add(mStompClient.send("/pub/chat/message", obj.toString())
                    .compose(applySchedulers())
                    .subscribe(() -> {
                        Log.d(TAG, "STOMP echo send successfully");
                    }, throwable -> {
                        Log.e(TAG, "Error send STOMP echo", throwable);
                        //toast(throwable.getMessage());
                    }));
        } catch (JSONException e) {}
    }

    public void uploadFile(String grantType, String accessToken, String filePath, String roomId, String userId, OnFileUploadListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/upload";

        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        MultipartFormDataBody body = new MultipartFormDataBody();
        body.addFilePart("file", new File(filePath));
        body.addStringPart("room_id", roomId);
        body.addStringPart("user_id", userId);
        post.addHeader("Authorization", grantType + ' ' + accessToken);
        post.setBody(body);

        final OnFileUploadListener fileUploadObserver = observer;
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    if(source.code()==200) {
                        try {
                            String code = result.getString("code");
                            if (code.equals(SUCCESS)) {
                                String fileName = result.getString("file_name");
                                String fileDownloadUrl = result.getString("file_download_url");
                                String fileType = result.getString("file_type");
                                Long fileSize = result.getLong("file_size");
                                fileUploadObserver.onSuccess(fileName, fileDownloadUrl, fileType, fileSize);
                            } else {
                                String errMessage = result.getString("message");
                                fileUploadObserver.onFailure(code, errMessage);
                            }
                        } catch (JSONException jsonException) {
                            fileUploadObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_UPLOADFILE);
                        }
                    } else {
                        fileUploadObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_UPLOADFILE);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        fileUploadObserver.onReissueNeeded();
                    } else if(source!=null && source.code()==413) {
                        fileUploadObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_LARGEFILESIZE);
                    } else {
                        fileUploadObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_UPLOADFILE);
                    }
                }
            }
        });
    }

    public void downloadFile(String downloadUrl, String filename, OnFileDownloadListener observer) {

        final OnFileDownloadListener fileDownloadObserver = observer;
        AsyncHttpClient.getDefaultInstance().executeFile(new AsyncHttpGet(downloadUrl), filename, new AsyncHttpClient.FileCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, File result) {
                if(e==null) {
                    fileDownloadObserver.onSuccess(result);
                } else {
                    fileDownloadObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_DOWNLOADFILE);
                }
            }
        });
    }

    private CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void resetSubscriptions() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.dispose();
        }
        mCompositeDisposable = new CompositeDisposable();
    }

    private ChatMessage parseMessageJSONObject(JSONObject obj) {
        try {
            String messageId = obj.getString("message_id");
            ChatMessageEnum type = ChatMessageEnum.valueOf(obj.getString("type"));
            String roomId = obj.getString("room_id");
            String userId = obj.getString("user_id");
            String message = obj.getString("message");
            String parentMessageId = obj.getString("parent_message_id");

            ChatMessage parentMessage = null;
            if(parentMessageId!=null) {
                try {
                    JSONObject parentObj = obj.getJSONObject("parentMessage");
                    parentMessage = new ChatMessage();
                    parentMessage.setMessageId(parentObj.getString("message_id"));
                    parentMessage.setType(ChatMessageEnum.valueOf(parentObj.getString("type")));
                    parentMessage.setRoomId(parentObj.getString("room_id"));
                    parentMessage.setUserId(parentObj.getString("user_id"));
                    parentMessage.setMessage(parentObj.getString("message"));
                    parentMessage.setMimeType(parentObj.getString("file_mime_type"));
                    parentMessage.setDownloadPath(parentObj.getString("file_download_path"));
                    parentMessage.setCreatedTime(parentObj.getLong("created_time"));
                } catch (JSONException jsonException) {}
            }

            String mimeType = obj.getString("file_mime_type");
            String downloadPath = obj.getString("file_download_path");
            Long createdTime = obj.getLong("created_time");

            ArrayList<ChatUser> userList = null;
            try {
                JSONArray participants = obj.getJSONArray("participants");
                if(participants.length()>0) {
                    userList = new ArrayList<ChatUser>();
                    for(int j=0; j<participants.length(); j++) {
                        JSONObject participant = participants.getJSONObject(j);
                        ChatUser user = new ChatUser();
                        user.setUserId(participant.getString("id"));
                        user.setUserNickname(participant.getString("name"));
                    }
                }
            } catch (JSONException jsonException) {}

            ChatMessage currentMessage = new ChatMessage();
            currentMessage.setMessageId(messageId);
            currentMessage.setType(type);
            currentMessage.setRoomId(roomId);
            currentMessage.setUserId(userId);
            currentMessage.setMessage(message);
            currentMessage.setParentMessageId(parentMessageId);
            currentMessage.setParentMessage(parentMessage);
            currentMessage.setMimeType(mimeType);
            currentMessage.setDownloadPath(downloadPath);
            currentMessage.setCreatedTime(createdTime);
            currentMessage.setParticipants(userList);

            return currentMessage;
        } catch (JSONException jsonException) {
            return null;
        }
    }


}
