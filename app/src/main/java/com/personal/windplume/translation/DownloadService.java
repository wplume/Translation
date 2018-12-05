package com.personal.windplume.translation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.personal.windplume.translation.activity.MainActivity;

import java.io.File;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    private DownloadTask downloadTask;
    private String downloadUrl;

    /**
     * 回调接口实现，监听网络状态和用户操作，做出相对应的操作
     */
    private DownloadCallback listener = new DownloadCallback() {

        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download success", -1));
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download failed", -1));
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            getNotificationManager().notify(1, getNotification("Pause", -1));
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
        }
    };

    public DownloadService() {
    }

    private DownloadBinder downloadBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return downloadBinder;
    }

    // 设计Binder类
    public class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(url);

                Log.d(TAG, "开启前台服务显示下载进度");
                startForeground(1, getNotification("Downloading...", 0));
            }
        }

        void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.setPaused();
            }
        }

        void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.setCanceled();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory
                            (Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    boolean isDeleted = false;
                    if (file.exists()) {
                        isDeleted = file.delete();
                    }
                    if (isDeleted) {
                        System.out.println("File has been deleted");
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                }
            }
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Download");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle(title);
        if (progress > 0) {
            builder.setContentText(progress + "%");
            //第一个参数是最大进度，第二个是当前进度，第三个表示是否模糊进度条
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }
}
