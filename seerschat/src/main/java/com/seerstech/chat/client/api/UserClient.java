package com.seerstech.chat.client.api;

import android.net.Uri;

import com.koushikdutta.async.http.*;
import com.koushikdutta.async.http.body.*;
import com.seerstech.chat.client.jwt.JWTToken;
import com.seerstech.chat.client.user.OnFindUserListener;
import com.seerstech.chat.client.user.OnLoginListener;
import com.seerstech.chat.client.user.OnLogoutListener;
import com.seerstech.chat.client.user.OnReissueListener;

import org.json.JSONException;
import org.json.JSONObject;

public class UserClient {
    private final String mUri;
    private final String SUCCESS = "CODE_SUCCESS";

    public UserClient(String uri) {
        this.mUri = uri;
    }

    public void login(String userId, String userPassword, OnLoginListener observer) {
        if(mUri.isEmpty())
            return;

        String reqUri = mUri +"/user/login";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("user_id", userId);
            obj.put("user_password", userPassword);
        } catch (Exception e) {}

        final OnLoginListener loginObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
           @Override
           public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    try {
                       if(source.code()==200) {
                           String code = result.getString("code");
                           if (code.equals(SUCCESS)) {
                               String grantType = result.getString("grant_type");
                               String accessToken = result.getString("access_token");
                               String refreshToken = result.getString("refresh_token");
                               Long refreshTokenExpTime = result.getLong("refresh_token_expiration_time");
                               String userId = result.getString("user_id");
                               String userNickname = result.getString("user_nickname");
                               String userRole = result.getString("user_role");

                               JWTToken token = JWTToken.builder()
                                       .grantType(grantType)
                                       .accessToken(accessToken)
                                       .refreshToken(refreshToken)
                                       .refreshTokenExpTime(refreshTokenExpTime)
                                       .userId(userId)
                                       .userNickname(userNickname)
                                       .userRole(userRole)
                                       .build();

                               loginObserver.onSuccess(token);
                           } else {
                               loginObserver.onFailure(result.getString("code"), result.getString("message"));
                           }
                       } else {
                           loginObserver.onFailure("CODE_GENERIC_FAIL", "Failed In Login");
                       }
                    } catch (JSONException jsonException) {
                        //jsonException.printStackTrace();
                        loginObserver.onFailure("CODE_GENERIC_FAIL", "Failed In Login");
                    }
                } else {
                    loginObserver.onFailure("CODE_GENERIC_FAIL", "Failed In Login");
                }
           }
        });
    }

    public void logout(String grantType, String accessToken, String refreshToken, OnLogoutListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/user/logout";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("access_token", accessToken);
            obj.put("refresh_token", refreshToken);
        } catch (Exception e) {}

        final OnLogoutListener logoutObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    try {
                        if(source.code()==200) {
                            String code = result.getString("code");
                            if (code.equals(SUCCESS)) {
                                logoutObserver.onSuccess(false);
                            } else {
                                logoutObserver.onFailure(result.getString("code"), result.getString("message"));
                            }
                        } else {

                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                } else {
                    if(source.code()==401) {
                        logoutObserver.onSuccess(true);
                    } else {
                        logoutObserver.onFailure("CODE_GENERIC_FAIL", "Failed In Logout");
                    }
                }
            }
        });
    }

    public void reissue(String grantType, String accessToken, String refreshToken, OnReissueListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/user/reissue";

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "post");
        AsyncHttpPost post = new AsyncHttpPost(reqUri);
        post.addHeader("Content-Type", "application/json;charset=utf-8");
        post.addHeader("Authorization", grantType + ' ' + accessToken);

        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put("access_token", accessToken);
            obj.put("refresh_token", refreshToken);
        } catch (Exception e) {}

        final OnReissueListener reissueObserver = observer;
        post.setBody(new JSONObjectBody(obj));
        AsyncHttpClient.getDefaultInstance().executeJSONObject(post, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    try {
                        String code = result.getString("code");
                        if(code.equals(SUCCESS)) {
                            String grantType = result.getString("grant_type");
                            String accessToken = result.getString("access_token");
                            String refreshToken = result.getString("refresh_token");
                            Long refreshTokenExpTime = result.getLong("refresh_token_expiration_time");
                            String userId = result.getString("user_id");
                            String userNickname = result.getString("user_nickname");
                            String userRole = result.getString("user_role");

                            JWTToken token = JWTToken.builder()
                                    .grantType(grantType)
                                    .accessToken(accessToken)
                                    .refreshToken(refreshToken)
                                    .refreshTokenExpTime(refreshTokenExpTime)
                                    .userId(userId)
                                    .userNickname(userNickname)
                                    .userRole(userRole)
                                    .build();

                            reissueObserver.onSuccess(token);
                        } else {
                            reissueObserver.onFailure(result.getString("code"), result.getString("message"));
                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                } else {
                    reissueObserver.onFailure("CODE_GENERIC_FAIL", "Failed In Reissue");
                }
            }
        });
    }

    public void findUser(String grantType, String accessToken, String userId, OnFindUserListener observer) {
        if (mUri.isEmpty())
            return;

        String reqUri = mUri +"/user/" + userId;

        AsyncHttpRequest request = new AsyncHttpRequest(Uri.parse(reqUri), "get");
        AsyncHttpGet get = new AsyncHttpGet(reqUri);
        get.addHeader("Content-Type", "application/json;charset=utf-8");
        get.addHeader("Authorization", grantType + ' ' + accessToken);

        final OnFindUserListener findUserObserver = observer;
        AsyncHttpClient.getDefaultInstance().executeJSONObject(get, new AsyncHttpClient.JSONObjectCallback() {
            @Override
            public void onCompleted(Exception e, AsyncHttpResponse source, JSONObject result) {
                if(e==null) {
                    try {
                        String code = result.getString("code");
                        if(code.equals(SUCCESS)) {
                            findUserObserver.onSuccess(result.getString("id"), result.getString("nickname"));
                        } else {
                            findUserObserver.onFailure(result.getString("code"), result.getString("message"));
                        }
                    } catch (JSONException jsonException) {
                        jsonException.printStackTrace();
                    }
                } else {
                    if(source.code()==401) {
                        findUserObserver.onReissueNeeded();
                    } else {
                        findUserObserver.onFailure("CODE_GENERIC_FAIL", "Failed In FindUser");
                    }
                }
            }
        });
    }
}
