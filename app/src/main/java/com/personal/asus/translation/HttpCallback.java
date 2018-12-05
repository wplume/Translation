package com.personal.asus.translation;

public interface HttpCallback<E> {
    void onFinish(E response);

    void onError(Exception e);
}
