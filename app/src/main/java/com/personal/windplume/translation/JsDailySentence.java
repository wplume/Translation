package com.personal.windplume.translation;

import org.json.JSONException;
import org.json.JSONObject;

public class JsDailySentence {
    private String content;
    private String note;
    private String picture;
    private String picture2;

    public JsDailySentence(JSONObject jsonObject){
        try {
            setContent(jsonObject.getString("content"));
            setNote(jsonObject.getString("note"));
            setPicture(jsonObject.getString("picture"));
            setPicture2(jsonObject.getString("picture2"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPicture2() {
        return picture2;
    }

    public void setPicture2(String picture2) {
        this.picture2 = picture2;
    }
}
