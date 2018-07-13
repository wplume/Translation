package com.example.asus.translation;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        FragmentDownloadUpdateDialog.DownloadUpdateDialogListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    private static String TAG = MainActivity.class.getName();

    private static final String DATABASE_FILENAME = "BioDic.db";
    private static final String versionUrl = "https://raw.githubusercontent.com/wplume/Translation/master/app/release/output.json";
    private static final String downloadUrl = "https://github.com/wplume/Translation/raw/master/app/release/app-release.apk";

    BottomNavigationView bottomNavigationView;

    boolean isDatabaseFileExist;
    private FragmentOnline fragmentOnline;
    private FragmentOffline fragmentOffline;
    private FragmentGlossary fragmentGlossary;
    private FragmentManager fragmentManager;
    private Fragment lastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 申请 WRITE_EXTERNAL_STORAGE 权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // 设置内容显示在系统栏下面，这样当系统栏隐藏起来的时候，内容就不会被调整
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            // 这里是设置状态栏
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            //
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

//        DatabaseHelper.deleteDatabase(this);

        // 因为 new DatabaseHelper()无论存在不存在，都会创建一个数据库文件，所以需要提前标志，用来判断需不需要写入数据
        if ((new File(getDatabasePath(DATABASE_FILENAME).getPath())).exists()) {
            isDatabaseFileExist = true;
            System.out.println("数据库文件已存在");
        } else {
            isDatabaseFileExist = false;
            System.out.println("数据库文件不存在");
        }

        DatabaseHelper databaseHelper = DatabaseHelper.getDatabaseHelper(this);
        if (!isDatabaseFileExist) {
            databaseHelper.write();
        }

        // 设置侧边栏里面的NavigationView
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_about:
                        FragmentAboutDialog dialog = new FragmentAboutDialog();
                        dialog.show(getSupportFragmentManager(), "AboutDialog");
                        break;
                    case R.id.nav_update:
                        Toast.makeText(MainActivity.this, "正在检查版本信息，请稍后^_^", Toast.LENGTH_SHORT).show();
                        HttpUtil.sendOkHttpRequest(versionUrl, callback);
                        break;
                }
                return false;
            }
        });

        // 设置初始页面
        fragmentManager = getSupportFragmentManager();
        fragmentOnline = new FragmentOnline();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container, fragmentOnline).commit();
        lastFragment = fragmentOnline;

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: 2018/6/25 这里还有bug
        unbindService(serviceConnection);
    }

    Handler checkUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                int versionCode = (int) msg.obj;
                int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                Log.d(TAG, "当前版本为：" + currentVersionCode + " / " + "服务器版本为：" + versionCode);

                if (currentVersionCode == versionCode) {
                    Toast.makeText(MainActivity.this, "您当前已经是最新版本" + versionCode, Toast.LENGTH_SHORT).show();
                } else {
                    FragmentDownloadUpdateDialog dialog = new FragmentDownloadUpdateDialog();
                    dialog.show(getSupportFragmentManager(), "DownloadUpdateDialog");
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return false;
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限部分功能将无法使用", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    // 这里使用的是OkHttp自带的回调接口
    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            Toast.makeText(MainActivity.this, "检查失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            try {
                String str = response.body().string();
                JSONArray array = new JSONArray(str);
                JSONObject js = array.getJSONObject(0);
                JSONObject js1 = js.getJSONObject("apkInfo");
                int versionCode = js1.getInt("versionCode");

                Message msg = new Message();
                msg.obj = versionCode;
                checkUpdateHandler.sendMessage(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "活动与服务绑定成功");
            DownloadService.DownloadBinder downloadBinder = (DownloadService.DownloadBinder) service;
            downloadBinder.startDownload(downloadUrl);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (searchCallBack != null)
            searchCallBack.setSearchCallback();
    }

    private SearchCallBack searchCallBack;

    public void setSearchCallBack(SearchCallBack searchCallBack) {
        this.searchCallBack = searchCallBack;
    }

    @Override
    public void OnPositiveButtonClicked() {
        Log.d(TAG, "用户点击了确认下载");
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        Log.d(TAG, "开启并绑定服务");
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void OnNegativeButtonClicked() {
        Log.d(TAG, "用户点击了取消下载");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (item.getItemId()) {
            case R.id.bottom_home:
                Log.d(TAG, "点击了首页");
                if (fragmentOnline == null) {
                    fragmentOnline = new FragmentOnline();
                    fragmentTransaction.add(R.id.main_container, fragmentOnline);
                    fragmentTransaction.hide(lastFragment);
                } else {
                    if (!lastFragment.equals(fragmentOnline)) {
                        fragmentTransaction.show(fragmentOnline);
                        fragmentTransaction.hide(lastFragment);
                    }
                }
                lastFragment = fragmentOnline;
                break;

            case R.id.bottom_offline:
                Log.d(TAG, "点击了词汇");
                if (fragmentOffline == null) {
                    fragmentOffline = new FragmentOffline();
                    fragmentTransaction.add(R.id.main_container, fragmentOffline);
                    fragmentTransaction.hide(lastFragment);
                } else {
                    if (!lastFragment.equals(fragmentOffline)) {
                        fragmentTransaction.show(fragmentOffline);
                        fragmentTransaction.hide(lastFragment);
                    }
                }
                lastFragment = fragmentOffline;
                break;

            case R.id.bottom_glossary:
                Log.d(TAG, "点击了生词本");
                if (fragmentGlossary == null) {
                    fragmentGlossary = new FragmentGlossary();
                    fragmentTransaction.add(R.id.main_container, fragmentGlossary);
                    fragmentTransaction.hide(lastFragment);
                } else {
                    if (!lastFragment.equals(fragmentGlossary)) {
                        fragmentTransaction.show(fragmentGlossary);
                        fragmentTransaction.hide(lastFragment);
                    }
                }
                lastFragment = fragmentGlossary;
                break;
        }
        fragmentTransaction.commit();
        return true;
    }

    interface SearchCallBack {
        void setSearchCallback();
    }
}
