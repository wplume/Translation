package com.example.asus.translation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

class TranslationJs {
    String word;
    String ph_en;
    String ph_am;
    String ph_en_mp3;
    String ph_am_mp3;
    ArrayList<String> parts = new ArrayList<>();
    ArrayList<JSONArray> means = new ArrayList<>();

    TranslationJs(JSONObject jsonObject) {
        try {
            //toString的参数是缩进的意思
            System.out.println(jsonObject.toString(4));

            word = jsonObject.getString("word_name");

            JSONArray symbols = jsonObject.getJSONArray("symbols");
            JSONObject js = symbols.getJSONObject(0);

            ph_en = js.getString("ph_en");
            ph_am = js.getString("ph_am");
            ph_en_mp3 = js.getString("ph_en_mp3");
            ph_am_mp3 = js.getString("ph_am_mp3");
            JSONArray parts = js.getJSONArray("parts");
            for (int i = 0; i < parts.length(); i++) {
                this.parts.add(parts.getJSONObject(i).getString("part"));
                means.add(parts.getJSONObject(i).getJSONArray("means"));
            }
        } catch (JSONException e) {
            word = "无法查询该单词";
            e.printStackTrace();
        }
    }
}
