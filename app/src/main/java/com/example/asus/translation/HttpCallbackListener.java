package com.example.asus.translation;

public interface HttpCallbackListener {
    void onFinish(String response);

    void onError(Exception e);
}
