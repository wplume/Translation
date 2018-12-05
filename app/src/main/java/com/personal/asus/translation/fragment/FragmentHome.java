package com.personal.asus.translation.fragment;

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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.personal.asus.translation.JsDailySentence;
import com.personal.asus.translation.JsTranslation;
import com.personal.asus.translation.OnlineSuggestionProvider;
import com.personal.asus.translation.R;
import com.personal.asus.translation.TranslationLab;
import com.personal.asus.translation.bean.NewWord;
import com.personal.asus.translation.util.HttpUtil;

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

public class FragmentHome extends Fragment {


    private static final String TAG = FragmentHome.class.toString();
    private static final int CONFIG_DAILY_SENTENCE = 0;
    private static final int CONFIG_PICTURE = 1;
    private static final int CONFIG_SEARCH_WORD_RESULT = 2;
    private static final String URL = "http://open.iciba.com/dsapi/?date=";
    /**
     * 金山api申请到的key，查词的时候需要和词一起传给服务器
     */
    private static final String KEY = "8B1845F228CA3D723DC68AEF651CCCDD";
    SimpleDateFormat simpleDateFormat;
    String date;
    boolean isConnect;
    private ImageView imageView;
    private TextView content;
    private TextView note;
    private Button btnEnPron;
    private Button btnAmPron;
    private Button btnAddToGlossary;
    private TextView ph_en;
    private TextView ph_am;
    private TextView tvExplanation;
    private TextView tvWord;
    private Toolbar toolbar;
    private CardView cardView;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //防止Activity内存泄漏
        handler.removeCallbacksAndMessages(null);
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
        tvExplanation = view.findViewById(R.id.tvExplanation);
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

    private void doRequest() {

        if (isConnect) {
            HttpUtil.sendOkHttp_GetRequest(URL + date, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "onFailure: " + e.getMessage());
                    e.printStackTrace();
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

            //点击search dialog的确定按钮，或者软键盘上的确定按钮，将启动一个searchable activity（目前是MainActivity本身）
            btnFloat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //启动search dialog（系统负责）
                    getActivity().onSearchRequested();
                }
            });
        }
    }

    public void searchRequest(String word) {
        word = word.toLowerCase();

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c < 'a' || c > 'z') {
                // TODO: 2018/8/1 中文翻译待开发
                Toast.makeText(getActivity(), "请输入正确的字符", Toast.LENGTH_SHORT).show();
                break;
            } else {
                if (i == word.length() - 1) {

                    //用户点击确定之后，将关键字保存到历史纪录，声明好权限
                    SearchRecentSuggestions searchRecentSuggestions = new SearchRecentSuggestions(
                            getActivity(),
                            OnlineSuggestionProvider.AUTHORITY,
                            OnlineSuggestionProvider.MODE
                    );

                    searchRecentSuggestions.saveRecentQuery(word, null);

                    if (isConnect) {
                        String url = String.format("http://dict-co.iciba.com/api/dictionary.php?w=%s&type=json&key=%s", word, KEY);
                        HttpUtil.sendOkHttp_GetRequest(url, new Callback() {
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
                    } else {
                        Toast.makeText(getActivity(), "无法加载", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    private void configDailySentence(final JsDailySentence jsDailySentence) {

        content.setText(jsDailySentence.getContent());
        note.setText(jsDailySentence.getNote());

        String url = jsDailySentence.getPicture2();
        HttpUtil.sendOkHttp_GetRequest(url, new Callback() {
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

    private void configPicture(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

    private void configSearchWordResult(final JsTranslation jsTranslation) {

        if (!jsTranslation.getWord().equals("Sorry！无法查询该单词")) {
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

            boolean isExist = TranslationLab.get(getActivity()).queryNewWord(jsTranslation.getWord());
            if (isExist) {
                btnAddToGlossary.setEnabled(false);
                btnAddToGlossary.setVisibility(View.VISIBLE);
                btnAddToGlossary.setText("已加入生词本");
            } else {
                btnAddToGlossary.setEnabled(true);
                btnAddToGlossary.setVisibility(View.VISIBLE);
                btnAddToGlossary.setText("加入生词本");
                btnAddToGlossary.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NewWord newWord = new NewWord();
                        newWord.setEn_word(jsTranslation.getWord());
                        newWord.setZh_word("");
                        newWord.setExplanation(jsTranslation.getExplanation());
                        TranslationLab.get(getActivity()).addNewWord(newWord);
                        btnAddToGlossary.setEnabled(false);
                        btnAddToGlossary.setText("已加入生词本");
                    }
                });
            }

            tvExplanation.setText(jsTranslation.getExplanation().replace('/', '\n'));
            tvWord.setText(jsTranslation.getWord());
        } else {
            Toast.makeText(getActivity(), jsTranslation.getWord(), Toast.LENGTH_SHORT).show();
            btnAddToGlossary.setVisibility(View.INVISIBLE);
        }
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
