package com.example.asus.translation.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class OfflineWord implements Parcelable {
    public static final Creator<OfflineWord> CREATOR = new Creator<OfflineWord>() {
        @Override
        public OfflineWord createFromParcel(Parcel in) {
            return new OfflineWord(in);
        }

        @Override
        public OfflineWord[] newArray(int size) {
            return new OfflineWord[size];
        }
    };
    private String en_word;
    private String zh_word;
    private String explanation;

    private OfflineWord(Parcel in) {
        en_word = in.readString();
        zh_word = in.readString();
        explanation = in.readString();
    }

    public OfflineWord() {
    }

    ;

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(en_word);
        dest.writeString(zh_word);
        dest.writeString(explanation);
    }
}
