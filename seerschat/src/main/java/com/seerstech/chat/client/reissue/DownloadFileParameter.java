package com.seerstech.chat.client.reissue;

import lombok.Builder;

@Builder
public class DownloadFileParameter {
    public String downloadUrl;
    public String fileName;
}
