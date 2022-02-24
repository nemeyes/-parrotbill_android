package com.seerstech.chat.client.reissue;

import lombok.Builder;

@Builder
public class InviteUserParameter {
    public String roomId;
    public String userId;
}
