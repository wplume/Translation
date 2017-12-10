package com.example.asus.translation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by asus on 2017/11/16.
 */
public class TranslationJs {
    public String ph_en;
    public String ph_am;
    public String ph_en_mp3;
    public String ph_am_mp3;
    public ArrayList<String> parts = new ArrayList<>();
    public ArrayList<JSONArray> means = new ArrayList<>();

    TranslationJs(JSONObject jsonObject) {
        try {
            //toString的参数是缩进的意思
            System.out.println(jsonObject.toString(4));

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
            e.printStackTrace();
        }
    }
}
