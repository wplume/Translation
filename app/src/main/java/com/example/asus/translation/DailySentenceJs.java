package com.example.asus.translation;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by asus on 2017/11/13.
 */
public class DailySentenceJs {
    public String content;
    public String note;
    public String picture;
    public String picture2;
    public DailySentenceJs(JSONObject jsonObject){
        try {
            this.content = jsonObject.getString("content");
            this.note = jsonObject.getString("note");
            this.picture = jsonObject.getString("picture");
            this.picture2 = jsonObject.getString("picture2");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
