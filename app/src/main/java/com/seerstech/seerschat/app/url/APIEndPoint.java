package com.seerstech.seerschat.app.url;

public class APIEndPoint {
    private String mRestEndPoint = "https://chat-api.seers-visual-system.link";//"http://112.220.119.251:3030";//
    private String mWSEndPoint = "wss://chat-api.seers-visual-system.link";//"ws://112.220.119.251:3030";//
    private static APIEndPoint mInstance = new APIEndPoint();

    public static APIEndPoint getInstance() {
        return mInstance;
    }

    private APIEndPoint() {
    }

    public String getRestEndPoint() {
        return mRestEndPoint;
    }

    public String getWSEndPoint() {
        return mWSEndPoint;
    }
}
