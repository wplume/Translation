package com.example.asus.translation;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class OfflineFragment extends Fragment {
    /**
     * 需要读取的xls文件名
     */
    public static final String XLS_FILENAME = "BioDicXls.xls";
    /**
     * 数据库文件名
     */
    public static final String DATABASE_FILENAME = "BioDic.db";
    /**
     * 表名
     */
    public static final String TABLE_NAME = "dictionary";
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
    private SQLiteDatabase database;
    boolean isDatabaseFileExist;
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_offline, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        //因为 new DatabaseHelper()无论存在不存在，都会创建一个数据库文件，所以需要提前标志，用来判断需不需要写入数据
        if ((new File(getActivity().getDatabasePath(DATABASE_FILENAME).getPath())).exists()) {
            isDatabaseFileExist = true;
            System.out.println("数据库文件已存在");
        } else {
            isDatabaseFileExist = false;
            System.out.println("数据库文件不存在");
        }
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity(), DATABASE_FILENAME, null, 1);
        database = databaseHelper.getWritableDatabase();
        //用户第一次安装该软件时，需要向数据库文件写入数据
        if (!isDatabaseFileExist) {
            write();
            isDatabaseFileExist = true;
        }
        query();
        return view;
    }

    /**
     * 数据库辅助类
     */
    class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            System.out.println("创建了新的数据库文件");
            System.out.println("创建表和它的属性");
            db.execSQL("create table " + TABLE_NAME +
                    "(" + EN_WORD_COL1 + " VARCHAR," + ZH_WORD_COL2 + " VARCHAR," + EXPLANATION + " VARCHAR" + ")");
        }
    }

    void write() {
        System.out.println("将xls文件数据读入数据库的表");
        try {
            InputStream inputStream = getActivity().getAssets().open(XLS_FILENAME);
            //数据流方式读取，不过文件方式读取的话会更快
            NPOIFSFileSystem fileSystem = new NPOIFSFileSystem(inputStream);
            HSSFWorkbook workbook = new HSSFWorkbook(fileSystem.getRoot(), true);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();
            while (rows.hasNext()) {
                HSSFRow row = (HSSFRow) rows.next();
                if (row.getRowNum() != 0) {
                    System.out.println(row.getRowNum() + " " + row.getCell(0).toString() + " " + row.getCell(1).toString() + " " + row.getCell(2).toString() + "\n");
                    insert(row.getCell(0).toString(), row.getCell(1).toString(), row.getCell(2).toString());
                }
            }
            System.out.println("读取完成");
            inputStream.close();
            fileSystem.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void insert(String en_word, String zh_word, String explanation) {
        ContentValues values = new ContentValues();
        values.put(EN_WORD_COL1, en_word);
        values.put(ZH_WORD_COL2, zh_word);
        values.put(EXPLANATION, explanation);
        database.insert(TABLE_NAME, EN_WORD_COL1, values);
        System.out.println("插入成功");
        values.clear();
    }

    void query() {
        //_id 是以为SimpleCursorAdapter from必须要有一个_id列
        String[] col = new String[]{EN_WORD_COL1 + " as _id", ZH_WORD_COL2};
        String whereBefore = EN_WORD_COL1 + " =?";
        String Biocomputing = "Biocomputing%'";
        String[] whereAfter = new String[]{Biocomputing};

        Cursor cursor = database.query(
                TABLE_NAME,
                col,
                null, null,
                null, null, null);
//        Cursor cursor = database.query(
//                TABLE_NAME,
//                col,
//                EN_WORD_COL1 + " like?",
//                new String[]{"%" + "Biocomputing" + "%"},
//                null, null, null);
//        Cursor cursor = database.rawQuery("select en_word,zh_word from dictionary where en_word=?", new String[]{"'%" + "a" + "%'"});
        System.out.println("匹配个数:" + cursor.getCount());
        int i = cursor.getColumnIndex("_id");
        int j = cursor.getColumnIndex(ZH_WORD_COL2);
        cursor.moveToFirst();
        for (cursor.isBeforeFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            System.out.println(cursor.getString(i) + "\t" + cursor.getString(j) + "\n");
        }
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.adapter,
                cursor,
                new String[]{"_id", ZH_WORD_COL2},
                new int[]{R.id.en_word, R.id.zh_word},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(simpleCursorAdapter);
        // TODO: 2017/11/29 这个有待考究
        //这个不能关闭
//        cursor.close();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //第一个参数是父级，第二个参数是当前item，第三个是在listview中适配器里的位置，id是当前item在ListView里的第几行的位置
                TextView textView = (TextView) view.findViewById(R.id.en_word);
                System.out.println(textView.getText().toString());
            }
        });
    }

    void deleteTable() {
        System.out.println("删除表");
//        database.execSQL("drop table ? cascade", new String[]{TABLE_NAME});
        database.execSQL("drop table dictionary");
    }

    void deleteDatabase() {
        if (getActivity().getDatabasePath(DATABASE_FILENAME).exists()) {
            System.out.println("存在数据库文件");
            getActivity().deleteDatabase(DATABASE_FILENAME);
            System.out.println("删除成功");
        }
    }
}
