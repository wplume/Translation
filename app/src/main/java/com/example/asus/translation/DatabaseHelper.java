package com.example.asus.translation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static com.example.asus.translation.db.TranslationDBSchema.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = SQLiteOpenHelper.class.getName();

    /**
     * 需要读取的xls文件名
     */
    public static final String XLS_FILENAME = "BioDicXls.xls";
    /**
     * 数据库文件名
     */
    private static final String DATABASE_FILENAME = "BioDic.db";

    DatabaseHelper(Context context) {
        super(context, DATABASE_FILENAME, null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, "创建了新的数据库文件");
        Log.d(TAG, "创建离线词典表和生词表");
        db.execSQL(
                "create table " + VocabularyTable.NAME + "("
                        + GlossaryTable.Col.EN_WORD + " VARCHAR,"
                        + GlossaryTable.Col.ZH_WORD + " VARCHAR,"
                        + GlossaryTable.Col.EXPLANATION + " VARCHAR" + ")"
        );
        db.execSQL(
                "create table " + GlossaryTable.NAME + "("
                        + GlossaryTable.Col.EN_WORD + " VARCHAR,"
                        + GlossaryTable.Col.ZH_WORD + " VARCHAR,"
                        + GlossaryTable.Col.EXPLANATION + " VARCHAR" + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "数据库更新");
        String s = String.format("drop table if exists %s", VocabularyTable.NAME);
        String s1 = String.format("drop table if exists %s", GlossaryTable.NAME);
        db.execSQL(s);
        db.execSQL(s1);
    }

    @SuppressWarnings("unused")
    public static void deleteDatabase(Context context) {
        if (context.getDatabasePath(DATABASE_FILENAME).exists()) {
            Log.d(TAG, "存在数据库文件");
            context.deleteDatabase(DATABASE_FILENAME);
            Log.d(TAG, "删除成功");
        }
    }
}
