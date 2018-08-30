package com.example.asus.translation;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.asus.translation.bean.NewWord;
import com.example.asus.translation.bean.OfflineWord;
import com.example.asus.translation.db.BeanCursorWrapper;
import com.example.asus.translation.db.TranslationDBSchema.VocabularyTable;

import java.util.Observable;

import static com.example.asus.translation.db.TranslationDBSchema.GlossaryTable;

public class TranslationLab extends Observable {

    private static final String TAG = TranslationLab.class.getName();

    private static TranslationLab translationLab;
    private SQLiteDatabase database;

    private TranslationLab(Context context) {
        //这里使用Application是为了防止context内存泄漏
        database = new DatabaseHelper(context.getApplicationContext()).getWritableDatabase();
    }

    public static TranslationLab get(Context context) {
        if (translationLab == null) {
            translationLab = new TranslationLab(context);
        }
        return translationLab;
    }

    /*--------------------------------------------------------------------------------------------*/

    private static ContentValues getContentValues(NewWord newWord) {
        ContentValues values = new ContentValues();
        values.put(GlossaryTable.Col.EN_WORD, newWord.getEn_word());
        values.put(GlossaryTable.Col.ZH_WORD, newWord.getZh_word());
        values.put(GlossaryTable.Col.EXPLANATION, newWord.getExplanation());
        return values;
    }

    private static ContentValues getContentValues(OfflineWord offlineWord) {
        ContentValues values = new ContentValues();
        values.put(VocabularyTable.Col.EN_WORD, offlineWord.getEn_word());
        values.put(VocabularyTable.Col.ZH_WORD, offlineWord.getZh_word());
        values.put(VocabularyTable.Col.EXPLANATION, offlineWord.getExplanation());
        return values;
    }

    public void addNewWord(NewWord newWord) {
        ContentValues values = getContentValues(newWord);
        //第二个参数nullColumnHack的作用，是为了防止values为null
        database.insert(GlossaryTable.NAME, null, values);

        setChanged();//Java内置的观察者模式需要先调用setChange()
        notifyObservers();
    }

    public boolean queryNewWord(String word) {
        return queryNewWord(GlossaryTable.Col.EN_WORD + " like?", new String[]{word})
                .getCount() != 0;
    }

    public BeanCursorWrapper queryNewWord(String whereClause, String[] whereArgs) {
        return new BeanCursorWrapper(database.query(
                GlossaryTable.NAME,
                null,//Column-null select all column
                whereClause,
                whereArgs,
                null,//groupBy
                null,//having
                null //orderBy
        ));
    }

    public void deleteNewWord(String word) {
        database.delete(
                GlossaryTable.NAME,
                GlossaryTable.Col.EN_WORD + " = " + "'" + word + "'",
                null
        );
    }

    public void addOfflineWord(OfflineWord offlineWord) {
        ContentValues values = getContentValues(offlineWord);
        //第二个参数nullColumnHack的作用，是为了防止values为null
        database.insert(VocabularyTable.NAME, null, values);
    }

    public boolean queryOfflineWord(String word) {
        return queryOfflineWord(VocabularyTable.Col.EN_WORD + " like?", new String[]{word})
                .getCount() != 0;
    }

    public BeanCursorWrapper queryOfflineWord(String whereClause, String[] whereArgs) {
        return new BeanCursorWrapper(database.query(
                VocabularyTable.NAME,
                null,//Column-null select all column
                whereClause,
                whereArgs,
                null,//groupBy
                null,//having
                null //orderBy
        ));
    }

    public void deleteOfflineWord(String word) {
        database.delete(
                VocabularyTable.NAME,
                VocabularyTable.Col.EN_WORD + " = " + "'" + word + "'",
                null
        );
    }
}
