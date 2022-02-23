package com.seerstech.chat.client.message;

public interface OnFileUploadListener {
    public void onSuccess(String fileName, String fileDownloadUrl, String fileType, Long fileSize);
    public void onReissueNeeded();
    public void onFailure(String code, String message);
}
