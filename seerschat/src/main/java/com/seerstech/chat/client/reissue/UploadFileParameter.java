package com.seerstech.chat.client.reissue;

import lombok.Builder;

@Builder
public class UploadFileParameter {
    public String filePath;
    public String roomId;
    public String userId;
}
