package com.example.asus.translation;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FragmentOnline extends android.support.v4.app.Fragment {

    private static final String TAG = FragmentOnline.class.toString();
    private static final int CONFIG_DAILY_SENTENCE = 0;
    private static final int CONFIG_PICTURE = 1;
    private static final int CONFIG_SEARCH_WORD_RESULT = 2;

    private ImageView imageView;
    private TextView content;
    private TextView note;
    private Button btnEnPron;
    private Button btnAmPron;
    private Button btnAddToGlossary;
    private TextView ph_en;
    private TextView ph_am;
    private TextView tvOut;
    private TextView tvWord;
    private Toolbar toolbar;
    private CardView cardView;

    private String url = "http://open.iciba.com/dsapi/?date=";
    //金山api申请到的key，查词的时候需要和词一起传给服务器
    private String key = "8B1845F228CA3D723DC68AEF651CCCDD";
    SimpleDateFormat simpleDateFormat;
    String date;
    String word;

    boolean isConnect;
    private FloatingActionButton btnFloat;
    private View view;

    private Handler handler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_online, container, false);

        initDate();

        initViews();

        initHandler();

        setToolbar();

        initVisibility();

        setNetworkStatus();

        doRequest();

        return view;
    }

    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    //每日一句的内容和解释
                    case CONFIG_DAILY_SENTENCE:
                        configDailySentence((JsDailySentence) msg.obj);
                        break;
                    //每日一句的配图
                    case CONFIG_PICTURE:
                        configPicture((Bitmap) msg.obj);
                        break;
                    //查词结果
                    case CONFIG_SEARCH_WORD_RESULT:
                        configSearchWordResult((JsTranslation) msg.obj);
                        break;
                }
                //如果不需要进一步处理，则为真
                return false;
            }
        });
    }

    private void initDate() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        date = simpleDateFormat.format(new Date());
    }

    private void initViews() {
        toolbar = view.findViewById(R.id.toolbar);
        imageView = view.findViewById(R.id.ivDailyPic);
        content = view.findViewById(R.id.content);
        ph_en = view.findViewById(R.id.ph_en);
        ph_am = view.findViewById(R.id.ph_am);
        note = view.findViewById(R.id.note);
        btnEnPron = view.findViewById(R.id.btnEnPron);
        btnAmPron = view.findViewById(R.id.btnAmPron);
        btnAddToGlossary = view.findViewById(R.id.btnAddToGlossary);
        tvOut = view.findViewById(R.id.tvOut);
        tvWord = view.findViewById(R.id.tvWord);
        btnFloat = view.findViewById(R.id.btnFloat);
        cardView = view.findViewById(R.id.cardView);
    }

    private void setToolbar() {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.setTitle(date);
    }

    private void initVisibility() {
        btnEnPron.setVisibility(View.INVISIBLE);
        btnAmPron.setVisibility(View.INVISIBLE);
        btnAddToGlossary.setVisibility(View.INVISIBLE);
    }

    private void setNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            Toast.makeText(getActivity(), "设备没有联网", Toast.LENGTH_LONG).show();
            isConnect = false;
        } else isConnect = true;
    }

    private void doRequest(){

        if (isConnect) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    HttpUtil.sendOkHttpGetRequest(url + date, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String string = response.body().string();
                                JSONObject jsonObject = new JSONObject(string);
                                JsDailySentence jsDailySentence = new JsDailySentence(jsonObject);

                                //虽然Message的构造函数是公共的，但获取其中一个的最佳方法是调用Message.obtain（）
                                //或其中一个Handler.obtainMessage（）方法，这将从循环对象池中提取它们。
                                Message msg = Message.obtain(handler, CONFIG_DAILY_SENTENCE, jsDailySentence);
                                handler.sendMessage(msg);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }.start();

            //点击search dialog的确定按钮，或者软键盘上的确定按钮，将启动一个searchable activity（目前是MainActivity本身）
            btnFloat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //提前设置好搜索回调函数
                    ((MainActivity) getActivity()).setSearchCallBack(new MainActivity.SearchCallBack() {
                        @Override
                        public void setSearchCallback() {
                            searchRequest();
                        }
                    });
                    //启动search dialog（系统负责）
                    getActivity().onSearchRequested();
                }
            });
        }
    }

    private void searchRequest() {
        //在Manifest文件中已经设置SingleTop模式（启动时如果已经在栈顶，将直接使用给活动）
        word = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
        word = word.toLowerCase();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c >= 'a' && c <= 'z') {
                if (i == word.length() - 1) {

                    //用户点击确定之后，将关键字保存到历史纪录，声明好权限
                    SearchRecentSuggestions searchRecentSuggestions =
                            new SearchRecentSuggestions(
                                    getActivity(),
                                    OnlineSuggestionProvider.AUTHORITY,
                                    OnlineSuggestionProvider.MODE);
                    searchRecentSuggestions.saveRecentQuery(word, null);

                    if (isConnect)
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                String url = String.format("http://dict-co.iciba.com/api/dictionary.php?w=%s&type=json&key=%s", word, key);
                                HttpUtil.sendOkHttpGetRequest(url, new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {

                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        try {
                                            String string = response.body().string();
                                            JSONObject jsonObject = new JSONObject(string);
                                            JsTranslation jsTranslation = new JsTranslation(jsonObject);

                                            Message msg = Message.obtain(handler, CONFIG_SEARCH_WORD_RESULT, jsTranslation);
                                            handler.sendMessage(msg);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }.start();
                    else Toast.makeText(getActivity(), "无法加载", Toast.LENGTH_SHORT).show();
                }
            } else {
                // TODO: 2018/8/1 中文翻译待开发
                Toast.makeText(getActivity(), "请输入正确的字符", Toast.LENGTH_SHORT).show();
                break;
            }
        }

    }

    private void configDailySentence(final JsDailySentence jsDailySentence) {

        content.setText(jsDailySentence.getContent());
        note.setText(jsDailySentence.getNote());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = jsDailySentence.getPicture2();
                HttpUtil.sendOkHttpGetRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream inputStream = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();
                        Message msg = Message.obtain(handler, CONFIG_PICTURE, bitmap);
                        handler.sendMessage(msg);
                    }
                });
            }
        }).start();
    }

    private void configPicture(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    private void configSearchWordResult(final JsTranslation jsTranslation) {
        StringBuilder stringBuilder = new StringBuilder();

        //音标
        String en = "英式发音：" + jsTranslation.getPh_am();
        String am = "美式发音：" + jsTranslation.getPh_am();
        ph_en.setText(en);
        ph_am.setText(am);
        //发音
        btnEnPron.setVisibility(View.VISIBLE);
        btnAmPron.setVisibility(View.VISIBLE);

        cardView.setVisibility(View.VISIBLE);

        //注册播放按钮事件
        btnEnPron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jsTranslation.getPh_am_mp3() != null && !jsTranslation.getPh_en_mp3().equals(""))
                    playFromRemoteURL(jsTranslation.getPh_en_mp3());
                else Toast.makeText(getActivity(), "无音源", Toast.LENGTH_SHORT).show();
            }
        });
        btnAmPron.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jsTranslation.getPh_am_mp3() != null && !jsTranslation.getPh_am_mp3().equals(""))
                    playFromRemoteURL(jsTranslation.getPh_am_mp3());
                else Toast.makeText(getActivity(), "无音源", Toast.LENGTH_SHORT).show();
            }
        });
        //拼接存到数据库中文翻译
        final StringBuilder stringBuilder1 = new StringBuilder();
        for (int i = 0; i < jsTranslation.getParts().size(); i++) {
            stringBuilder1.append(jsTranslation.getParts().get(i));
            for (int j = 0; j < jsTranslation.getMeans().get(i).length(); j++) {
                try {
                    if (j != jsTranslation.getMeans().get(i).length() - 1)
                        stringBuilder1.append(jsTranslation.getMeans().get(i).get(j).toString());
                    else
                        stringBuilder1.append(jsTranslation.getMeans().get(i).get(j).toString()).append("\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //注册添加到生词本按钮的事件
        if (!jsTranslation.getWord().equals("无法查询该单词")) {
            if (DatabaseHelper.queryIsExist(word)) {
                btnAddToGlossary.setEnabled(false);
                btnAddToGlossary.setVisibility(View.VISIBLE);
                btnAddToGlossary.setText("已加入生词本");
            } else {
                btnAddToGlossary.setEnabled(true);
                btnAddToGlossary.setVisibility(View.VISIBLE);
                btnAddToGlossary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseHelper.insertGlossary(word, stringBuilder1.toString(), "");
                        btnAddToGlossary.setEnabled(false);
                        btnAddToGlossary.setText("已加入生词本");
                    }
                });
            }
        } else {
            btnAddToGlossary.setVisibility(View.INVISIBLE);
        }

        //释义 拼接用来显示的中文翻译
        for (int i = 0; i < jsTranslation.getParts().size(); i++) {
            stringBuilder.append(jsTranslation.getParts().get(i)).append("  ");
            for (int j = 0; j < jsTranslation.getMeans().get(i).length(); j++) {
                try {
                    if (j != jsTranslation.getMeans().get(i).length() - 1)
                        stringBuilder.append(jsTranslation.getMeans().get(i).get(j).toString());
                    else
                        stringBuilder.append(jsTranslation.getMeans().get(i).get(j).toString()).append("\n");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        tvOut.setText(stringBuilder.toString());
        tvWord.setText(jsTranslation.getWord());
    }

    private void playFromRemoteURL(String url) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }
}
