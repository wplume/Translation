package com.example.asus.translation;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView tvOnline;
    TextView tvOffline;
    TextView tvAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.framelayout, new OnlineFragment()).commit();
        tvOnline = (TextView) findViewById(R.id.tvOnline);
        tvOffline = (TextView) findViewById(R.id.tvOffline);
        tvAbout = (TextView) findViewById(R.id.tvAbout);

        tvOnline.setOnClickListener(this);
        tvOffline.setOnClickListener(this);
        tvAbout.setOnClickListener(this);

        tvOnline.setSelected(true);
    }

    // TODO: 2017/11/29 将fragment缓存起来
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvOnline:
                if (!tvOnline.isSelected()) {
                    resetAll();
                    tvOnline.setSelected(true);
                    newFragment(new OnlineFragment());
                }
                break;
            case R.id.tvOffline:
                if (!tvOffline.isSelected()) {
                    resetAll();
                    tvOffline.setSelected(true);
                    newFragment(new OfflineFragment());
                }
                break;
            case R.id.tvAbout:
                if (!tvAbout.isSelected()) {
                    resetAll();
                    tvAbout.setSelected(true);
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
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
    }
}
