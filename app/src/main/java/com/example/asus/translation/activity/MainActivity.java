package com.example.asus.translation.activity;

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

import com.example.asus.translation.DatabaseHelper;
import com.example.asus.translation.DownloadService;
import com.example.asus.translation.fragment.FragmentAboutDialog;
import com.example.asus.translation.fragment.FragmentDownloadUpdateDialog;
import com.example.asus.translation.fragment.FragmentGlossary;
import com.example.asus.translation.fragment.FragmentHome;
import com.example.asus.translation.fragment.FragmentVocabulary;
import com.example.asus.translation.R;
import com.example.asus.translation.TranslationLab;
import com.example.asus.translation.bean.OfflineWord;
import com.example.asus.translation.util.HttpUtil;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        FragmentDownloadUpdateDialog.DownloadUpdateDialogListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String DATABASE_FILENAME = "BioDic.db";
    /**
     * 获取最新软件版本号的地址
     */
    private static final String versionUrl = "https://raw.githubusercontent.com/wplume/Translation/master/app/release/output.json";
    /**
     * 获取最新软件apk文件的地址
     */
    private static final String downloadUrl = "https://github.com/wplume/Translation/raw/master/app/release/app-release.apk";
    private static String TAG = MainActivity.class.getName();
    BottomNavigationView bottomNavigationView;
    Handler checkUpdateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                int newestVersionCode = (int) msg.obj;
                int currentVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

                Log.d(TAG, "当前版本为：" + currentVersionCode + " / " + "服务器版本为：" + newestVersionCode);

                if (currentVersionCode == newestVersionCode) {
                    Toast.makeText(MainActivity.this, "您当前已经是最新版本" + newestVersionCode, Toast.LENGTH_SHORT).show();
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
    // 这里使用的是OkHttp自带的回调接口
    Callback checkUpdateCallback = new Callback() {
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
    private FragmentHome fragmentHome;
    private FragmentVocabulary fragmentVocabulary;
    private FragmentGlossary fragmentGlossary;
    private FragmentManager fragmentManager;
    private Fragment lastFragment;
    private ServiceConnection serviceConnection = null;
    private SearchCallBack searchCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 这里申请WRITE_EXTERNAL_STORAGE权限是为了保存app的更新的apk文件
        // 申请 WRITE_EXTERNAL_STORAGE(外部存储写入) 权限，也需要在Manifests里面用<user-permission>标记
        int checkSelfPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            // 点三个参数1，对应的是onRequestPermissionsResult的requestCode
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // 设置状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // setStatusBarColor需要设置这个属性FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);

            // 设置内容显示在系统栏下面，这样当系统栏隐藏起来的时候，内容就不会被调整
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            // 这里是设置状态栏
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (!(new File(getDatabasePath(DATABASE_FILENAME).getPath())).exists()) write();

        // 设置侧边栏里面的NavigationView
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_about:
                        // 显示关于的dialog
                        FragmentAboutDialog dialog = new FragmentAboutDialog();
                        dialog.show(getSupportFragmentManager(), "AboutDialog");
                        break;
                    case R.id.nav_update:
                        // 检测软件版本并判断是否更新
                        Toast.makeText(MainActivity.this, "正在检查版本信息，请稍后^_^", Toast.LENGTH_SHORT).show();
                        HttpUtil.sendOkHttpGetRequest(versionUrl, checkUpdateCallback);
                        break;
                }
                return false;
            }
        });

        // 设置初始页面
        fragmentManager = getSupportFragmentManager();
        fragmentHome = new FragmentHome();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_container, fragmentHome).commit();
        lastFragment = fragmentHome;

        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serviceConnection != null)
            unbindService(serviceConnection);
    }

    // 权限请求回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // requestCode对应的是requestPermissions的第三个参数
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限部分功能将无法使用", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (searchCallBack != null)
            searchCallBack.setSearchCallback();
    }

    public void setSearchCallBack(SearchCallBack searchCallBack) {
        this.searchCallBack = searchCallBack;
    }

    // 这是实现 FragmentDownloadUpdateDialog 的两个回调
    @Override
    public void OnPositiveButtonClicked() {
        Log.d(TAG, "用户点击了确认下载");
        Intent intent = new Intent(MainActivity.this, DownloadService.class);
        Log.d(TAG, "开启并绑定服务");

        // 生成bindService需要的serviceConnection
        serviceConnection = new ServiceConnection() {
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

        // 启动服务可以保证服务一直在后台运行
        startService(intent);
        // 绑定服务则可以让服务和活动进行通信
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void OnNegativeButtonClicked() {
        Log.d(TAG, "用户点击了取消下载");
    }

    // 底部导航栏事件
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (item.getItemId()) {
            case R.id.bottom_home:
                Log.d(TAG, "点击了首页");
                if (fragmentHome == null) {
                    fragmentHome = new FragmentHome();
                    fragmentTransaction.add(R.id.main_container, fragmentHome);
                    fragmentTransaction.hide(lastFragment);
                } else {
                    if (!lastFragment.equals(fragmentHome)) {
                        fragmentTransaction.show(fragmentHome);
                        fragmentTransaction.hide(lastFragment);
                    }
                }
                lastFragment = fragmentHome;
                break;

            case R.id.bottom_offline:
                Log.d(TAG, "点击了词汇");
                if (fragmentVocabulary == null) {
                    fragmentVocabulary = new FragmentVocabulary();
                    fragmentTransaction.add(R.id.main_container, fragmentVocabulary);
                    fragmentTransaction.hide(lastFragment);
                } else {
                    if (!lastFragment.equals(fragmentVocabulary)) {
                        fragmentTransaction.show(fragmentVocabulary);
                        fragmentTransaction.hide(lastFragment);
                    }
                }
                lastFragment = fragmentVocabulary;
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

    private void write() {
        Log.d(TAG, "将xls文件数据读入数据库的词汇表");
        try {
            //.xls文件放在assets文件夹
            InputStream inputStream = getAssets().open(DatabaseHelper.XLS_FILENAME);
            //数据流方式读取，不过文件方式读取的话会更快
            NPOIFSFileSystem fileSystem = new NPOIFSFileSystem(inputStream);
            HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                if (row.getRowNum() != 0) {
                    OfflineWord offlineWord = new OfflineWord();
                    offlineWord.setEn_word(row.getCell(0).toString());
                    offlineWord.setZh_word(row.getCell(1).toString());
                    offlineWord.setExplanation(row.getCell(2).toString());
                    TranslationLab.get(this).addOfflineWord(offlineWord);
                }
            }
            Log.d(TAG, "读取完成");
            inputStream.close();
            fileSystem.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface SearchCallBack {
        void setSearchCallback();
    }
}
