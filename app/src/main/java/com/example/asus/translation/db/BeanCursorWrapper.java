package com.example.asus.translation.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.example.asus.translation.bean.NewWord;
import com.example.asus.translation.bean.OfflineWord;
import com.example.asus.translation.db.TranslationDBSchema.GlossaryTable;

public class BeanCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public BeanCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public OfflineWord getOfflineWord() {
        String en_word = getString(getColumnIndex(TranslationDBSchema.VocabularyTable.Col.EN_WORD));
        String zh_word = getString(getColumnIndex(TranslationDBSchema.VocabularyTable.Col.ZH_WORD));
        String explanation = getString(getColumnIndex(TranslationDBSchema.VocabularyTable.Col.EXPLANATION));

        OfflineWord offlineWord=new OfflineWord();
        offlineWord.setEn_word(en_word);
        offlineWord.setZh_word(zh_word);
        offlineWord.setExplanation(explanation);
        return offlineWord;
    }

    public NewWord getNewWord() {
        String en_word = getString(getColumnIndex(GlossaryTable.Col.EN_WORD));
        String zh_word = getString(getColumnIndex(GlossaryTable.Col.ZH_WORD));
        String explanation = getString(getColumnIndex(GlossaryTable.Col.EXPLANATION));

        NewWord newWord = new NewWord();
        newWord.setEn_word(en_word);
        newWord.setZh_word(zh_word);
        newWord.setExplanation(explanation);
        return newWord;
    }
}
