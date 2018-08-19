package com.example.asus.translation;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JsTranslation {
    private static final String TAG = JsTranslation.class.getName();

    public String getWord() {
        return word;
    }

    private void setWord(String word) {
        this.word = word;
    }

    public String getPh_en() {
        return ph_en;
    }

    public void setPh_en(String ph_en) {
        this.ph_en = ph_en;
    }

    public String getPh_am() {
        return ph_am;
    }

    public void setPh_am(String ph_am) {
        this.ph_am = ph_am;
    }

    public String getPh_en_mp3() {
        return ph_en_mp3;
    }

    private void setPh_en_mp3(String ph_en_mp3) {
        this.ph_en_mp3 = ph_en_mp3;
    }

    public String getPh_am_mp3() {
        return ph_am_mp3;
    }

    private void setPh_am_mp3(String ph_am_mp3) {
        this.ph_am_mp3 = ph_am_mp3;
    }

    public ArrayList<String> getParts() {
        return parts;
    }

    public void setParts(ArrayList<String> parts) {
        this.parts = parts;
    }

    public ArrayList<JSONArray> getMeans() {
        return means;
    }

    public void setMeans(ArrayList<JSONArray> means) {
        this.means = means;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    private String word;
    private String explanation;
    private String ph_en;
    private String ph_am;
    private String ph_en_mp3;
    private String ph_am_mp3;

    //一个词性对应多个释义 part → means
    private ArrayList<String> parts = new ArrayList<>();
    private ArrayList<JSONArray> means = new ArrayList<>();

    public JsTranslation(JSONObject jsonObject) {
        try {
            //toString的参数是缩进的意思
            Log.d(TAG, jsonObject.toString(4));

            setWord(jsonObject.getString("word_name"));

            JSONArray symbols = jsonObject.getJSONArray("symbols");
            JSONObject js = symbols.getJSONObject(0);

            setPh_en(js.getString("ph_en"));
            setPh_am(js.getString("ph_am"));
            setPh_en_mp3(js.getString("ph_en_mp3"));
            setPh_am_mp3(js.getString("ph_am_mp3"));
            JSONArray parts = js.getJSONArray("parts");

            //拼装explanation（词性和对应的词义，使用“/”将它们划分）
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                String part = parts.getJSONObject(i).getString("part");
                JSONArray means = parts.getJSONObject(i).getJSONArray("means");
                getParts().add(part);
                getMeans().add(means);

                StringBuilder builder1 = new StringBuilder();
                for (int j = 0; j < means.length(); j++) {
                    if (j != means.length() - 1) {
                        builder1.append(means.get(j).toString()).append("；");
                    } else {
                        builder1.append(means.get(j).toString()).append("/");
                    }
                }

                builder.append(part).append(builder1);
            }
            setExplanation(builder.toString());
            Log.d(TAG, "JsTranslation: " + getExplanation());
        } catch (JSONException e) {
            setWord("Sorry！无法查询该单词");
            e.printStackTrace();
        }
    }
}
