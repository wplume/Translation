package com.example.asus.translation;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask extends AsyncTask<String, Integer, Integer> {

    private static final int TYPE_SUCCESS = 0;
    private static final int TYPE_FAILED = 1;
    private static final int TYPE_PAUSED = 2;
    private static final int TYPE_CANCELED = 3;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    private int lastProgress;

    private DownloadListener listener;

    DownloadTask(DownloadListener downloadListener) {
        this.listener = downloadListener;
    }

    /**
     * 后台执行
     *
     * @param strings 关联的是AsyncTask第一个参数类型，用于传递后台任务需要字符串（地址）
     * @return 关联的是AsyncTask第三个参数类型，用于反馈执行结果
     */
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream stream = null;
        RandomAccessFile saveFile = null;
        File file = null;

        long downloadedLength = 0;//记录已下载的文件长度

        String downloadUrl = strings[0];
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        String directory = Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS).getPath();
        file = new File(directory + fileName);

        if (file.exists()) {
            downloadedLength = file.length();
        }
        long contentLength = getContentLength(strings[0]);
        if (contentLength == 0) {
            return TYPE_FAILED;
        } else if (contentLength == downloadedLength) {
            return TYPE_SUCCESS;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .addHeader("RANGE", "byte=" + contentLength + "-")
                .url(strings[0])
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null) {
                stream = response.body().byteStream();
                saveFile = new RandomAccessFile(file, "rw");
                saveFile.seek(downloadedLength);
                byte[] b = new byte[1024 * 1000000];
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
    protected void onPostExecute(Integer integer) {
        int i = integer;
        switch (i) {
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

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if (progress > lastProgress) {
            if (listener != null) listener.onProgress(values[0]);
            lastProgress = progress;
        }
    }

    private long getContentLength(String downloadUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }

    public void setPaused() {
        isPaused = true;
    }

    public void setCanceled() {
        isCanceled = true;
    }
}
