package com.example.asus.translation;

public interface HttpCallback {
    void onFinish(String response);

    void onError(Exception e);
}
