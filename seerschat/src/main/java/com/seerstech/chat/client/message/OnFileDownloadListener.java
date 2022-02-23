package com.seerstech.chat.client.message;

import java.io.File;

public interface OnFileDownloadListener {
    public void onSuccess(File file);
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
