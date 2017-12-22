package com.example.asus.translation;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentOffline extends Fragment {
    SQLiteDatabase database;
    ListView listView;
    AdapterLV adapterLV;
    View view;
    Cursor cursor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offline, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.setTitle(R.string.offline);


        listView = (ListView) view.findViewById(R.id.listView);
//        listView.addHeaderView(LayoutInflater.from(getActivity()).inflate(R.layout.footer_layout, null, false));
//        listView.addFooterView(LayoutInflater.from(getActivity()).inflate(R.layout.footer_layout, null, false));
        database = DatabaseHelper.getDatabaseHelper(getActivity()).getWritableDatabase();
        queryAll();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

    ArrayList<String> enList = new ArrayList<>();
    ArrayList<String> zhList = new ArrayList<>();
    ArrayList<String> explanationList = new ArrayList<>();

    void queryAll() {
        //_id 是以为SimpleCursorAdapter from必须要有一个_id列
        String[] col = new String[]{DatabaseHelper.EN_WORD_COL1 + " as _id", DatabaseHelper.ZH_WORD_COL2, DatabaseHelper.EXPLANATION};
        //查询所有
        cursor = database.query(DatabaseHelper.OFFLINE_DICTIONARY_TABLE_NAME, col, null, null, null, null, null);

        System.out.println("匹配个数:" + cursor.getCount());
        int i = cursor.getColumnIndex("_id");
        int j = cursor.getColumnIndex(DatabaseHelper.ZH_WORD_COL2);
        int k = cursor.getColumnIndex(DatabaseHelper.EXPLANATION);
        cursor.moveToFirst();
        for (cursor.isBeforeFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            enList.add(cursor.getString(i));
            zhList.add(cursor.getString(j));
            explanationList.add(cursor.getString(k));
        }
        // TODO: 2017/12/3 ListView还要加个表尾
        adapterLV = new AdapterLV(getActivity(), cursor);
        listView.setAdapter(adapterLV);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // TODO: 2017/11/29 id不知道是什么鬼
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //第一个参数是父级，第二个参数是当前item，第三个是在listView中适配器里的位置，id是当前item在ListView里的第几行的位置
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(DatabaseHelper.EN_WORD_COL1, enList);
                bundle.putStringArrayList(DatabaseHelper.ZH_WORD_COL2, zhList);
                bundle.putStringArrayList(DatabaseHelper.EXPLANATION, explanationList);
                bundle.putInt("position", position);
                bundle.putBoolean("isVisible_btnFloat", true);
                FragmentCard fragmentCard = new FragmentCard();
                fragmentCard.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).
                        replace(R.id.aux_framelayout, fragmentCard).commit();
            }
        });
    }

}
