package com.personal.windplume.translation;

public interface DownloadCallback {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
