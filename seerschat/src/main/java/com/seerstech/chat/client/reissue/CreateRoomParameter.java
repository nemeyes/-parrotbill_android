package com.seerstech.chat.client.reissue;

import java.util.List;

import lombok.Builder;

@Builder
public class CreateRoomParameter {
    public String roomName;
    public String roomDesc;
    public List<String> userIDs;
}
