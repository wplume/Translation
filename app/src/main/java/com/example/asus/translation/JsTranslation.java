package com.example.asus.translation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class JsTranslation {
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

    private String word;
    private String ph_en;
    private String ph_am;
    private String ph_en_mp3;
    private String ph_am_mp3;

    //一个词性对应多个释义 part → means
    private ArrayList<String> parts = new ArrayList<>();
    private ArrayList<JSONArray> means = new ArrayList<>();

    JsTranslation(JSONObject jsonObject) {
        try {
            //toString的参数是缩进的意思
            System.out.println(jsonObject.toString(4));

            setWord(jsonObject.getString("word_name"));

            JSONArray symbols = jsonObject.getJSONArray("symbols");
            JSONObject js = symbols.getJSONObject(0);

            setPh_en(js.getString("ph_en"));
            setPh_am(js.getString("ph_am"));
            setPh_en_mp3(js.getString("ph_en_mp3"));
            setPh_am_mp3(js.getString("ph_am_mp3"));
            JSONArray parts = js.getJSONArray("parts");
            for (int i = 0; i < parts.length(); i++) {
                getParts().add(parts.getJSONObject(i).getString("part"));
                getMeans().add(parts.getJSONObject(i).getJSONArray("means"));
            }
        } catch (JSONException e) {
            word = "无法查询该单词";
            e.printStackTrace();
        }
    }
}
