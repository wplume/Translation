package com.personal.windplume.translation.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class NewWord implements Parcelable {
    public static final Creator<NewWord> CREATOR = new Creator<NewWord>() {
        @Override
        public NewWord createFromParcel(Parcel source) {
            NewWord newWord = new NewWord();
            newWord.setEn_word(source.readString());
            newWord.setZh_word(source.readString());
            newWord.setExplanation(source.readString());
            return newWord;
        }

        @Override
        public NewWord[] newArray(int size) {
            return new NewWord[size];
        }
    };
    private String en_word;
    private String zh_word;
    private String explanation;

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
        dest.writeString(getEn_word());
        dest.writeString(getZh_word());
        dest.writeString(getExplanation());
    }
}
