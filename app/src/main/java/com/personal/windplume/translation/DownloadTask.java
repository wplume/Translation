package com.personal.windplume.translation;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, Integer> {
    private static final String TAG = "DownloadTask";

    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    private int lastProgress;

    private DownloadCallback listener;

    DownloadTask(DownloadCallback downloadCallback) {
        this.listener = downloadCallback;
    }

    /**
     * 后台执行，位于新的线程
     *
     * @param strings 关联的是AsyncTask第一个参数类型，用于传递后台任务需要字符串（地址）
     * @return 关联的是AsyncTask第三个参数类型，用于反馈执行结果
     */
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream stream = null;
        RandomAccessFile saveFile = null;
        File file;

        long downloadedLength = 0;//记录已下载的文件长度

        String downloadUrl = strings[0];

        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getPath();
        file = new File(directory + fileName);

        if (file.exists()) {
            Log.d(TAG, "下载文件已存在，检查是否完整...");
            downloadedLength = file.length();
        }

        long contentLength = getContentLength(strings[0]);
        Log.d(TAG, "获取服务器版本apk文件长度:" + contentLength + "字节");

        if (contentLength == 0) {
            Log.d(TAG, "服务器版本文件出现问题");
            return TYPE_FAILED;

        } else if (contentLength == downloadedLength) {
            Log.d(TAG, "检查完毕，文件完整，可以使用");
            return TYPE_SUCCESS;

        } else {
            Log.d(TAG, "文件不完整，需要继续下载");
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                // 断点下载，指定从哪里开始下载
                .addHeader("RANGE", "byte=" + contentLength + "-")
                .url(strings[0])
                .build();
        try {
            Log.d(TAG, "向服务器发起新版本安装文件下载请求");
            Response response = client.newCall(request).execute();
            if (response != null) {
                Log.d(TAG, "正在接收文件......");
                stream = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                saveFile.seek(downloadedLength);// 跳过已下载的字节
                byte[] b = new byte[1024 * 3000];
                int total = 0;
                int len;
                while ((len = stream.read(b)) != -1) {
                    if (isPaused) {

                        return TYPE_PAUSED;

                    } else if (isCanceled) {

                        return TYPE_CANCELED;

                    } else {
                        total += len;
                        saveFile.write(b, 0, len);
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
            }
            Log.d(TAG, "新版安装文件下载完成");
            response.body().close();
            return TYPE_SUCCESS;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) stream.close();
                if (saveFile != null) saveFile.close();
                if (isCanceled && file.exists()) file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            if (listener != null) listener.onProgress(values[0]);
            lastProgress = progress;
        }
    }

    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer) {
            case TYPE_SUCCESS:
                if (listener != null) listener.onSuccess();
                break;
            case TYPE_FAILED:
                if (listener != null) listener.onFailed();
                break;
            case TYPE_PAUSED:
                if (listener != null) listener.onPaused();
                break;
            case TYPE_CANCELED:
                if (listener != null) listener.onCanceled();
                break;
        }
    }

    // 获取服务器存储文件的长度
    private long getContentLength(String downloadUrl) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 如果为0则说明文件有问题
        return 0;
    }

    public void setPaused() {
        isPaused = true;
    }

    public void setCanceled() {
        isCanceled = true;
    }
}
