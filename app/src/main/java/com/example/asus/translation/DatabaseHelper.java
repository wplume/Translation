package com.example.asus.translation;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 数据库辅助类
 */
class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * 需要读取的xls文件名
     */
    public static final String XLS_FILENAME = "BioDicXls.xls";
    /**
     * 数据库文件名
     */
    public static final String DATABASE_FILENAME = "BioDic.db";
    /**
     * 离线词典表
     */
    public static final String OFFLINE_DICTIONARY_TABLE_NAME = "dictionary";
    /**
     * 生词表
     */
    public static final String GLOSSARY_TABLE_NAME = "glossary";
    /**
     * 第一列 英文单词
     */
    public static final String EN_WORD_COL1 = "en_word";
    /**
     * 第二列 中文单词
     */
    public static final String ZH_WORD_COL2 = "zh_word";
    /**
     * 第三列 详细解释
     */
    public static final String EXPLANATION = "explanation";
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;
    Context context;

    public static DatabaseHelper getDatabaseHelper(Context context) {
        if (databaseHelper == null) {
            System.out.println("生成DatabaseHelper单例");
            databaseHelper = new DatabaseHelper(context, DATABASE_FILENAME, null, 3);
            database = databaseHelper.getWritableDatabase();
        }
        return databaseHelper;
    }

    public static SQLiteDatabase getDatabase() {
        return database;
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        System.out.println("创建了新的数据库文件");
        System.out.println("创建离线词典表和生词表");
        db.execSQL("create table " + OFFLINE_DICTIONARY_TABLE_NAME +
                "(" + EN_WORD_COL1 + " VARCHAR," + ZH_WORD_COL2 + " VARCHAR," + EXPLANATION + " VARCHAR" + ")");
        db.execSQL("create table " + GLOSSARY_TABLE_NAME +
                "(" + EN_WORD_COL1 + " VARCHAR," + ZH_WORD_COL2 + " VARCHAR," + EXPLANATION + " VARCHAR" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("数据库更新");
        String s = String.format("drop table if exists %s", OFFLINE_DICTIONARY_TABLE_NAME);
        String s1 = String.format("drop table if exists %s", GLOSSARY_TABLE_NAME);
        db.execSQL(s);
        db.execSQL(s1);
    }

    void write() {
        System.out.println("将xls文件数据读入数据库的表");
        try {
            //.xls文件放在assets文件夹
            InputStream inputStream = context.getAssets().open(XLS_FILENAME);
            //数据流方式读取，不过文件方式读取的话会更快
            NPOIFSFileSystem fileSystem = new NPOIFSFileSystem(inputStream);
            HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                if (row.getRowNum() != 0) {
                    System.out.println(row.getRowNum() + " " + row.getCell(0).toString() + " " + row.getCell(1).toString() + " " + row.getCell(2).toString() + "\n");
                    insert(OFFLINE_DICTIONARY_TABLE_NAME, row.getCell(0).toString(), row.getCell(1).toString(), row.getCell(2).toString());
                }
            }
            System.out.println("读取完成");
            inputStream.close();
            fileSystem.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void insert(String table_name, String en_word, String zh_word, String explanation) {
        ContentValues values = new ContentValues();
        values.put(EN_WORD_COL1, en_word);
        values.put(ZH_WORD_COL2, zh_word);
        if (explanation != null)
            values.put(EXPLANATION, explanation);
        databaseHelper.getWritableDatabase().insert(table_name, EN_WORD_COL1, values);
        System.out.println("插入成功");
        values.clear();
    }

    private static Cursor query(String tableName, String col, String value) {
        return database.query(tableName, new String[]{col}, DatabaseHelper.EN_WORD_COL1 + " like?", new String[]{value}, null, null, null);
    }

    public static boolean queryIsExist(String enWord) {
        boolean isExist;
        Cursor cursor = query(DatabaseHelper.GLOSSARY_TABLE_NAME, DatabaseHelper.EN_WORD_COL1, enWord);
        if (cursor.getCount() != 0) {
            isExist = true;
        } else {
            isExist = false;
        }
        cursor.close();
        return isExist;
    }

    public static void insertGlossary(String enWord, String zhWord, String explanation) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.EN_WORD_COL1, enWord);
        values.put(DatabaseHelper.ZH_WORD_COL2, zhWord);
        values.put(DatabaseHelper.EXPLANATION, explanation);
        DatabaseHelper.getDatabase().insert(DatabaseHelper.GLOSSARY_TABLE_NAME, DatabaseHelper.EN_WORD_COL1, values);
    }

    public static void deleteDatabase(Context context) {
        if (context.getDatabasePath(DATABASE_FILENAME).exists()) {
            System.out.println("存在数据库文件");
            context.deleteDatabase(DATABASE_FILENAME);
            System.out.println("删除成功");
        }
    }
}
