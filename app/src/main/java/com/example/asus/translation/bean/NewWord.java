package com.example.asus.translation.bean;

public class NewWord {
    public String getEn_word() {
        return en_word;
    }

    public void setEn_word(String en_word) {
        this.en_word = en_word;
    }

    public String getZh_word() {
        return zh_word;
    }

    public void setZh_word(String zh_word) {
        this.zh_word = zh_word;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    private String en_word;
    private String zh_word;
    private String explanation;
}
