package com.example.asus.translation;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Locale;

public class OnlineFragment extends android.support.v4.app.Fragment {
    ImageView imageView;
    TextView content;
    TextView note;
    EditText etInput;
    Button btnTranslate;
    Button btnEnPron;
    Button btnAmPron;
    TextView ph_en;
    TextView ph_am;
    TextView tvOut;
    String url = "http://open.iciba.com/dsapi/?date=";
    String translationUrl = "http://dict-co.iciba.com/api/dictionary.php";
    String key = "8B1845F228CA3D723DC68AEF651CCCDD";
    SimpleDateFormat simpleDateFormat;
    String date;
    String word;

    boolean isConnect;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_online, container, false);
        System.out.println("创建了新的fragment");
        simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.CHINA);
        date = simpleDateFormat.format(new java.util.Date());
        System.out.println(date);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        content = (TextView) view.findViewById(R.id.content);
        ph_en = (TextView) view.findViewById(R.id.ph_en);
        ph_am = (TextView) view.findViewById(R.id.ph_am);
        note = (TextView) view.findViewById(R.id.note);
        etInput = (EditText) view.findViewById(R.id.etInput);
        btnTranslate = (Button) view.findViewById(R.id.btnTranslate);
        btnEnPron = (Button) view.findViewById(R.id.btnEnPron);
        btnAmPron = (Button) view.findViewById(R.id.btnAmPron);
        tvOut = (TextView) view.findViewById(R.id.tvOut);
        //将音标按钮设置为不可见，之后如果是英文翻译才显示
        btnEnPron.setVisibility(View.INVISIBLE);
        btnAmPron.setVisibility(View.INVISIBLE);
        //获取每日一句 js
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null) {
            Toast.makeText(getActivity(), "设备没有联网", Toast.LENGTH_LONG).show();
            isConnect = false;
        } else isConnect = true;

        // TODO: 2017/11/27 连有但是WiFi没有网的情况，还有手机欠费的情况
        if (isConnect)
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    DailySentenceJs dailySentenceJs = new DailySentenceJs(getJSON(url + date));
                    Message message = new Message();
                    message.what = 0;
                    message.obj = dailySentenceJs;
                    handler.sendMessage(message);
                }
            }.start();
        //注册翻译事件
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word = etInput.getText().toString();
//                word = "abstract";
                if (isConnect)
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            String url = String.format("http://dict-co.iciba.com/api/dictionary.php?w=%s&type=json&key=%s", word, key);
                            TranslationJs translationJs = new TranslationJs(getJSON(url));
                            Message message = new Message();
                            message.what = 0;
                            message.obj = translationJs;
                            translationHandler.sendMessage(message);
                        }
                    }.start();
                else Toast.makeText(getActivity(), "无法加载", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
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
                    break;
            }
        }
    };
    public Handler picHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bitmap bitmap = (Bitmap) msg.obj;
            imageView.setImageBitmap(bitmap);
        }
    };
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
            btnEnPron.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playFromRemoteURL(translationJs.ph_en_mp3);
                }
            });
            btnAmPron.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playFromRemoteURL(translationJs.ph_am_mp3);
                }
            });
            //翻译
            for (int i = 0; i < translationJs.part.size(); i++) {
                stringBuilder.append(translationJs.part.get(i) + "\n" + "  ");
                for (int j = 0; j < translationJs.means.get(i).length(); j++) {
                    try {
                        if (j != translationJs.means.get(i).length() - 1)
                            stringBuilder.append(translationJs.means.get(i).get(j).toString());
                        else
                            stringBuilder.append(translationJs.means.get(i).get(j).toString() + "\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            tvOut.setText(stringBuilder.toString());
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
