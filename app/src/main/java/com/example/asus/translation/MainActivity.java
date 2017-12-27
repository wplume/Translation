package com.example.asus.translation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String DATABASE_FILENAME = "BioDic.db";
    TextView tvOnline;
    TextView tvOffline;
    TextView tvAbout;
    boolean isDatabaseFileExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        //设置初始页面
        getSupportFragmentManager().beginTransaction().add(R.id.main_framelayout, new FragmentOnline()).commit();
        tvOnline = (TextView) findViewById(R.id.tvOnline);
        tvOffline = (TextView) findViewById(R.id.tvOffline);
        tvAbout = (TextView) findViewById(R.id.tvAbout);

        tvOnline.setOnClickListener(this);
        tvOffline.setOnClickListener(this);
        tvAbout.setOnClickListener(this);

        tvOnline.setSelected(true);
    }

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

    // TODO: 2017/11/29 将fragment缓存起来
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvOnline:
                if (!tvOnline.isSelected()) {
                    resetAll();
                    tvOnline.setSelected(true);
                    newFragment(new FragmentOnline());
                }
                break;
            case R.id.tvOffline:
                if (!tvOffline.isSelected()) {
                    resetAll();
                    tvOffline.setSelected(true);
                    newFragment(new FragmentOffline());
                }
                break;
            case R.id.tvAbout:
                if (!tvAbout.isSelected()) {
                    resetAll();
                    tvAbout.setSelected(true);
                    newFragment(new FragmentGlossary());
                }
                break;
        }
    }

    void resetAll() {
        tvOnline.setSelected(false);
        tvOffline.setSelected(false);
        tvAbout.setSelected(false);
    }

    void newFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.main_framelayout, fragment).commit();
    }
}
