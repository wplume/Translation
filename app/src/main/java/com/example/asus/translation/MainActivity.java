package com.example.asus.translation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.CHINA);
        date = simpleDateFormat.format(new java.util.Date());
        System.out.println(date);

        imageView = (ImageView) findViewById(R.id.imageView);
        content = (TextView) findViewById(R.id.content);
        ph_en = (TextView) findViewById(R.id.ph_en);
        ph_am = (TextView) findViewById(R.id.ph_am);
        note = (TextView) findViewById(R.id.note);
        etInput = (EditText) findViewById(R.id.etInput);
        btnTranslate = (Button) findViewById(R.id.btnTranslate);
        btnEnPron = (Button) findViewById(R.id.btnEnPron);
        btnAmPron = (Button) findViewById(R.id.btnAmPron);
        tvOut = (TextView) findViewById(R.id.tvOut);

        btnEnPron.setVisibility(View.INVISIBLE);
        btnAmPron.setVisibility(View.INVISIBLE);
        //获取每日一句 js
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

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                word = etInput.getText().toString();
//                word = "abstract";
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
            }
        });
        //翻译
//        new AsyncTask<String, String, String>() {
//
//            @Override
//            protected String doInBackground(String... params) {
//                try {
//                    URL url = new URL(params[0]);
//                    try {
//                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                        connection.setDoOutput(true);
//                        connection.setRequestMethod("POST");
//
//                        OutputStream os = connection.getOutputStream();
//                        OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//                        BufferedWriter bw = new BufferedWriter(osw);
//                        String s = String.format("w=what&type=json&key=%s", key);
//                        System.out.println(s);
//                        bw.write(s);
//                        bw.flush();
//
//                        bw.close();
//                        osw.close();
//                        os.close();
//
//                        InputStream is = connection.getInputStream();
//                        InputStreamReader isr = new InputStreamReader(is);
//                        BufferedReader br = new BufferedReader(isr);
//                        String line;
//                        StringBuilder stringBuilder = new StringBuilder();
//                        while ((line = br.readLine()) != null) {
//                            stringBuilder.append(line);
//                        }
//                        System.out.println(stringBuilder);
//
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//            }
//        }.execute(translationUrl);
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

    //获取 json
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
    private void playFromRemoteURL(String url){
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
