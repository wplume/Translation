package com.example.asus.translation;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {
    public static void sendHttpRequest(final String address, HttpCallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream stream = null;
                HttpURLConnection connection = null;
                String result = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.connect();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
