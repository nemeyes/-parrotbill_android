package com.seerstech.seerschat.app.auth;

import com.seerstech.chat.client.jwt.JWTToken;

public class JWTTokenContainer {

    private static JWTTokenContainer mInstance = new JWTTokenContainer();

    private JWTToken mToken;
    public static JWTTokenContainer getInstance() {
        return mInstance;
    }

    public void setToken(JWTToken token) {
        mToken = token;
    }

    public JWTToken getToken() {
        return mToken;
    }
}
