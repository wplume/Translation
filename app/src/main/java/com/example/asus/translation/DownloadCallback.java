package com.example.asus.translation;

public interface DownloadCallback {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
