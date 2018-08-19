package com.example.asus.translation.util;

import com.example.asus.translation.HttpCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    @SuppressWarnings("unused")
    private static final String TAG = HttpUtil.class.getName();

    /**
     * 使用 HttpURLConnection 方式，对服务器发送 GET 请求，接收的是字节流，并回字符串类型的数据给接口的回调函数
     * @param address 请求地址
     * @param listener 自定义的回调接口
     */
    @SuppressWarnings("unused")
    public static void sendHttpGetRequest(final String address, final HttpCallback<String> listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream stream = null;
                HttpURLConnection connection = null;
                String result;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setReadTimeout(3000);
                    connection.setConnectTimeout(3000);
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);//默认已经是true，但设置以防万一
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP error code: " + responseCode);
                    }
                    stream = connection.getInputStream();
                    result = readStream(stream);
                    if (listener != null) {
                        listener.onFinish(result);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    /**
     * 使用 OkHttp 方式，对服务器发送 GET 请求
     * @param url 请求地址
     * @param listener 使用OkHttp提供的回调接口
     */
    public static void sendOkHttpGetRequest(final String url, final Callback listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(listener);
            }
        }).start();
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @SuppressWarnings("unused")
    public static void sendOkHttpPostRequest(final String url, final String postData, final Callback listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                RequestBody body = RequestBody.create(JSON, postData);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                client.newCall(request).enqueue(listener);
            }
        }).start();
    }

    private static String readStream(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String singleLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((singleLine = bufferedReader.readLine()) != null) {
            stringBuilder.append(singleLine);
        }
        return stringBuilder.toString();
    }
}
