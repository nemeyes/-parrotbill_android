package com.seerstech.chat.client.api;

import android.net.Uri;
import android.util.Log;

import com.koushikdutta.async.http.*;
import com.koushikdutta.async.http.body.*;
import com.seerstech.chat.client.error.ErrorMessage;
import com.seerstech.chat.client.room.OnCreateRoomListener;
import com.seerstech.chat.client.room.OnGetRoomListListener;
import com.seerstech.chat.client.room.OnGetRoomUserListListener;
import com.seerstech.chat.client.room.OnInviteUserListener;
import com.seerstech.chat.client.room.OnLeaveRoomListener;
import com.seerstech.chat.client.vo.ChatRoom;
import com.seerstech.chat.client.vo.ChatUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RoomClient {
    private final String mUri;
    private final String SUCCESS = "CODE_SUCCESS";

    public RoomClient(String uri) {
        this.mUri = uri;
    }

    public void getRoomList(String grantType, String accessToken, OnGetRoomListListener observer) {
        if(mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/room";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "get");
        AsyncHttpGet get = new AsyncHttpGet(reqUri);
        get.addHeader("Content-Type", "application/json;charset=utf-8");
        get.addHeader("Authorization", grantType + ' ' + accessToken);

        final OnGetRoomListListener getRoomListObserver = observer;
        AsyncHttpClient.getDefaultInstance().executeJSONArray(get, new AsyncHttpClient.JSONArrayCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONArray result) {
                if(e==null) {
                    if(source.code()==200) {
                        ArrayList<ChatRoom> roomList = null;
                        if (result.length() > 0) {
                            roomList = new ArrayList<ChatRoom>();
                        }
                        for (int i = 0; i < result.length(); i++) {
                            try {
                                JSONObject obj = result.getJSONObject(i);
                                String roomId = obj.getString("id");
                                String roomName = obj.getString("name");
                                String roomDescription = obj.getString("description");

                                ChatRoom room = new ChatRoom();
                                room.setRoomId(roomId);
                                room.setRoomName(roomName);
                                room.setRoomDescription(roomDescription);

                                roomList.add(room);

                            } catch (JSONException jsonException) {}
                        }
                        getRoomListObserver.onSuccess(roomList);
                    } else {
                        getRoomListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMLIST);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        getRoomListObserver.onReissueNeeded();
                    } else {
                        getRoomListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMLIST);
                    }
                }
            }
        });
    }

    public void createRoom(String grantType, String accessToken, String roomName, String roomDescription, List<String> userIds, OnCreateRoomListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/room/create";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("name", roomName);
            obj.put("description", roomDescription);
            JSONArray participants = new JSONArray(userIds);
            obj.put("participants", participants);
        } catch (Exception e) {}

        final OnCreateRoomListener createRoomObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {

            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    if(source.code()==200) {
                        try {
                            String code = result.getString("code");
                            if (code.equals(SUCCESS)) {
                                createRoomObserver.onSuccess();
                            } else {
                                createRoomObserver.onFailure(result.getString("code"), result.getString("message"));
                            }
                        } catch (JSONException jsonException) {
                            createRoomObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_CREATEROOM);
                        }
                    } else {
                        createRoomObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_CREATEROOM);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        createRoomObserver.onReissueNeeded();
                    } else {
                        createRoomObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_CREATEROOM);
                    }
                }
            }
        });
    }

    public void getRoomUserList(String grantType, String accessToken, String roomId, OnGetRoomUserListListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/room/users";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("room_id", roomId);
        } catch (Exception e) {}

        final OnGetRoomUserListListener getRoomUserListObserver = observer;
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
                                JSONArray participant = result.getJSONArray("participants");

                                ArrayList<ChatUser> userList = null;
                                if (participant.length() > 0) {
                                    userList = new ArrayList<ChatUser>();
                                }
                                for (int i = 0; i < participant.length(); i++) {
                                    JSONObject obj = participant.getJSONObject(i);
                                    String userId = obj.getString("id");
                                    String userNickname = obj.getString("name");

                                    ChatUser user = new ChatUser();
                                    user.setUserId(userId);
                                    user.setUserNickname(userNickname);

                                    userList.add(user);
                                }
                                getRoomUserListObserver.onSuccess(roomId, userList);
                            } else {
                                getRoomUserListObserver.onFailure(code, message);
                            }

                        } catch (JSONException jsonException) {
                            getRoomUserListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMUSERLIST);
                        }
                    } else {
                        getRoomUserListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMUSERLIST);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        getRoomUserListObserver.onReissueNeeded();
                    } else {
                        getRoomUserListObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_GETROOMUSERLIST);
                    }
                }
            }
        });
    }

    public void leaveRoom(String grantType, String accessToken, String roomId, OnLeaveRoomListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/room/leave";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("room_id", roomId);
        } catch (Exception e) {}

        final OnLeaveRoomListener leaveRoomObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    if(source.code()==200) {
                        try {
                            String code = result.getString("code");
                            if (code.equals(SUCCESS)) {
                                leaveRoomObserver.onSuccess();
                            } else {
                                leaveRoomObserver.onFailure(result.getString("code"), result.getString("message"));
                            }
                        } catch (JSONException jsonException) {
                            leaveRoomObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_LEAVEROOM);
                        }
                    } else {
                        leaveRoomObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_LEAVEROOM);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        leaveRoomObserver.onReissueNeeded();
                    } else {
                        leaveRoomObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_LEAVEROOM);
                    }
                }
            }
        });
    }

    public void inviteUser(String grantType, String accessToken, String roomId, String userId, OnInviteUserListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/chat/room/invite";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("room_id", roomId);
            obj.put("user_id", userId);
        } catch (Exception e) {}

        final OnInviteUserListener inviteUserObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    if(source.code()==200) {
                        try {
                            String code = result.getString("code");
                            if (code.equals(SUCCESS)) {
                                inviteUserObserver.onSuccess();
                            } else {
                                inviteUserObserver.onFailure(result.getString("code"), result.getString("message"));
                            }
                        } catch (JSONException jsonException) {
                            inviteUserObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_INVITEUSER);
                        }
                    } else {
                        inviteUserObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_INVITEUSER);
                    }
                } else {
                    if(source!=null && source.code()==401) {
                        inviteUserObserver.onReissueNeeded();
                    } else {
                        inviteUserObserver.onFailure("CODE_GENERIC_FAIL", ErrorMessage.FAILED_IN_INVITEUSER);
                    }
                }
            }
        });
    }

}
