package com.example.asus.translation;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DATABASE_FILENAME = "BioDic.db";
    TextView tvOnline;
    TextView tvOffline;
    TextView tvAbout;
    boolean isDatabaseFileExist;
    private FragmentOnline fragmentOnline;
    private FragmentOffline fragmentOffline;
    private FragmentGlossary fragmentGlossary;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        DatabaseHelper.deleteDatabase(this);

        //因为 new DatabaseHelper()无论存在不存在，都会创建一个数据库文件，所以需要提前标志，用来判断需不需要写入数据
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

        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_about:
                        break;
                    case R.id.nav_update:

                        break;
                }
                return false;
            }
        });

        fragmentManager = getSupportFragmentManager();
        //设置初始页面
        fragmentOnline = new FragmentOnline();
        fragmentManager.beginTransaction().add(R.id.main_framelayout, fragmentOnline).commit();

        tvOnline = findViewById(R.id.tvOnline);
        tvOffline = findViewById(R.id.tvOffline);
        tvAbout = findViewById(R.id.tvAbout);

        tvOnline.setOnClickListener(this);
        tvOffline.setOnClickListener(this);
        tvAbout.setOnClickListener(this);

        tvOnline.setSelected(true);
    }

    private DownloadService.DownloadBinder downloadBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
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

    interface SearchCallBack {
        void setSearchCallback();
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.tvOnline:
                if (!tvOnline.isSelected()) {
                    resetAllSelected();
                    tvOnline.setSelected(true);
                    if (fragmentOnline == null) {
                        fragmentOnline = new FragmentOnline();
                        fragmentTransaction.add(R.id.main_framelayout, fragmentOnline);
                    } else {
                        fragmentTransaction.show(fragmentOnline);
                    }
                }
                break;
            case R.id.tvOffline:
                if (!tvOffline.isSelected()) {
                    resetAllSelected();
                    tvOffline.setSelected(true);
                    if (fragmentOffline == null) {
                        fragmentOffline = new FragmentOffline();
                        fragmentTransaction.add(R.id.main_framelayout, fragmentOffline);
                    } else {
                        fragmentTransaction.show(fragmentOffline);
                    }
                }
                break;
            case R.id.tvAbout:
                if (!tvAbout.isSelected()) {
                    resetAllSelected();
                    tvAbout.setSelected(true);
                    if (fragmentGlossary == null) {
                        fragmentGlossary = new FragmentGlossary();
                        fragmentTransaction.add(R.id.main_framelayout, fragmentGlossary);
                    } else {
                        fragmentTransaction.show(fragmentGlossary);
                    }
                }
                break;
        }
        if (!tvOnline.isSelected())
            if (fragmentOnline != null)
                fragmentTransaction.hide(fragmentOnline);
        if (!tvOffline.isSelected())
            if (fragmentOffline != null)
                fragmentTransaction.hide(fragmentOffline);
        if (!tvAbout.isSelected())
            if (fragmentGlossary != null)
                fragmentTransaction.hide(fragmentGlossary);
        fragmentTransaction.commit();
    }

    void resetAllSelected() {
        tvOnline.setSelected(false);
        tvOffline.setSelected(false);
        tvAbout.setSelected(false);
    }
}
