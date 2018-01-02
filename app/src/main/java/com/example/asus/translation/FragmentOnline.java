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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FragmentOnline extends android.support.v4.app.Fragment {
    ImageView imageView;
    TextView content;
    TextView note;
    Button btnEnPron;
    Button btnAmPron;
    Button btnAddToGlossary;
    TextView ph_en;
    TextView ph_am;
    TextView tvOut;
    TextView tvWord;
    String url = "http://open.iciba.com/dsapi/?date=";
    String key = "8B1845F228CA3D723DC68AEF651CCCDD";
    SimpleDateFormat simpleDateFormat;
    String date;
    String word;

    boolean isConnect;
    private FloatingActionButton btnFloat;
    private View view;
    private Toolbar toolbar;
    private CardView cardView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_online, container, false);

        initDate();

        initViews();

        setToolbar();

        initVisibility();

        setNetworkStatus();

        // TODO: 2017/11/27 连有但是WiFi没有网的情况，还有手机欠费的情况
        if (isConnect) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    DailySentenceJs dailySentenceJs = new DailySentenceJs(getJSON(url + date));
                    Message message = new Message();
                    message.what = 0;
                    message.obj = dailySentenceJs;
                    sentenceHandler.sendMessage(message);
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
        //注册翻译事件
//        btnTranslate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                word = etInput.getText().toString();
//                word = word.toLowerCase();
//                if (isConnect)
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            super.run();
//                            String url = String.format("http://dict-co.iciba.com/api/dictionary.php?w=%s&type=json&key=%s", word, key);
//                            TranslationJs translationJs = new TranslationJs(getJSON(url));
//                            Message message = new Message();
//                            message.what = 0;
//                            message.obj = translationJs;
//                            translationHandler.sendMessage(message);
//                        }
//                    }.start();
//                else Toast.makeText(getActivity(), "无法加载", Toast.LENGTH_SHORT).show();
//            }
//        });
        return view;
    }


    private void initVisibility() {
        btnEnPron.setVisibility(View.INVISIBLE);
        btnAmPron.setVisibility(View.INVISIBLE);
        btnAddToGlossary.setVisibility(View.INVISIBLE);
    }

    private void initViews() {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        imageView = (ImageView) view.findViewById(R.id.ivDailyPic);
        content = (TextView) view.findViewById(R.id.content);
        ph_en = (TextView) view.findViewById(R.id.ph_en);
        ph_am = (TextView) view.findViewById(R.id.ph_am);
        note = (TextView) view.findViewById(R.id.note);
        btnEnPron = (Button) view.findViewById(R.id.btnEnPron);
        btnAmPron = (Button) view.findViewById(R.id.btnAmPron);
        btnAddToGlossary = (Button) view.findViewById(R.id.btnAddToGlossary);
        tvOut = (TextView) view.findViewById(R.id.tvOut);
        tvWord = (TextView) view.findViewById(R.id.tvWord);
        btnFloat = (FloatingActionButton) view.findViewById(R.id.btnFloat);
        cardView = (CardView) view.findViewById(R.id.cardView);
    }

    private void initDate() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        date = simpleDateFormat.format(new Date());
    }

    private void setToolbar() {
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.setTitle(date);
    }

    private void setNetworkStatus() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            Toast.makeText(getActivity(), "设备没有联网", Toast.LENGTH_LONG).show();
            isConnect = false;
        } else isConnect = true;
    }

    private void searchRequest() {
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
                                TranslationJs translationJs = new TranslationJs(getJSON(url));
                                Message message = new Message();
                                message.obj = translationJs;
                                translationHandler.sendMessage(message);
                            }
                        }.start();
                    else Toast.makeText(getActivity(), "无法加载", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "请输入正确的字符", Toast.LENGTH_SHORT).show();
                break;
            }
        }

    }

    //配置每日一句
    public Handler sentenceHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final DailySentenceJs dailySentenceJs = (DailySentenceJs) msg.obj;
            content.setText(dailySentenceJs.content);
            note.setText(dailySentenceJs.note);

            //开启新的线程获取图片
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    Message message = new Message();
                    message.what = 0;
                    message.obj = getPic(dailySentenceJs.picture);
                    picHandler.sendMessage(message);
                }
            }.start();
        }
    };
    //配置每日一图的图片
    public Handler picHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap) msg.obj;
            imageView.setImageBitmap(bitmap);
        }
    };
    //配置翻译相关信息
    public Handler translationHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final TranslationJs translationJs = (TranslationJs) msg.obj;
            StringBuilder stringBuilder = new StringBuilder();

            //音标
            String en = "英式发音：" + translationJs.ph_en;
            String am = "美式发音：" + translationJs.ph_am;
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
                    if (translationJs.ph_en_mp3 != null && !translationJs.ph_en_mp3.equals(""))
                        playFromRemoteURL(translationJs.ph_en_mp3);
                    else Toast.makeText(getActivity(), "无音源", Toast.LENGTH_SHORT).show();
                }
            });
            btnAmPron.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (translationJs.ph_am_mp3 != null && !translationJs.ph_am_mp3.equals(""))
                        playFromRemoteURL(translationJs.ph_am_mp3);
                    else Toast.makeText(getActivity(), "无音源", Toast.LENGTH_SHORT).show();
                }
            });
            //注册添加生词本按钮事件
            //拼接存到数据库中文翻译
            final StringBuilder stringBuilder1 = new StringBuilder();
            for (int i = 0; i < translationJs.parts.size(); i++) {
                stringBuilder1.append(translationJs.parts.get(i));
                for (int j = 0; j < translationJs.means.get(i).length(); j++) {
                    try {
                        if (j != translationJs.means.get(i).length() - 1)
                            stringBuilder1.append(translationJs.means.get(i).get(j).toString());
                        else
                            stringBuilder1.append(translationJs.means.get(i).get(j).toString()).append("\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (translationJs.word.equals("无法查询该单词")) {

                if (DatabaseHelper.queryIsExist(word)) {
                    btnAddToGlossary.setEnabled(false);
                    btnAddToGlossary.setText("已加入生词本");
                } else {
                    btnAddToGlossary.setEnabled(true);
                    btnAddToGlossary.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatabaseHelper.insertGlossary(word, stringBuilder1.toString(), "");
                            Toast.makeText(getActivity(), "已加入生词本", Toast.LENGTH_SHORT).show();
                            btnAddToGlossary.setEnabled(false);
                            btnAddToGlossary.setText("已加入生词本");
                        }
                    });
                }
            }

            //翻译 拼接用来显示的中文翻译
            for (int i = 0; i < translationJs.parts.size(); i++) {
                stringBuilder.append(translationJs.parts.get(i)).append("  ");
                for (int j = 0; j < translationJs.means.get(i).length(); j++) {
                    try {
                        if (j != translationJs.means.get(i).length() - 1)
                            stringBuilder.append(translationJs.means.get(i).get(j).toString());
                        else
                            stringBuilder.append(translationJs.means.get(i).get(j).toString()).append("\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            tvOut.setText(stringBuilder.toString());
            tvWord.setText(translationJs.word);
        }
    };

    //获取每日一图的图片
    public Bitmap getPic(String url) {
        Bitmap bitmap = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(7000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 连接服务器，获取 json
     */
    private JSONObject getJSON(String url) {
        JSONObject js = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(7000);
            connection.connect();
            InputStream is = connection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
            try {
                js = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return js;
    }

    private void playFromRemoteURL(String url) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare(); // might take long! (for buffering, etc)
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }
}
