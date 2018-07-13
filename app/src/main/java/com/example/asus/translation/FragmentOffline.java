package com.example.asus.translation;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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
    private String[] col;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offline, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(toolbar);
        }
        if (appCompatActivity != null) {
            appCompatActivity.setTitle(R.string.offline);
        }

        //Fragment中要设置这个menu才能显示
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.listView);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

//        searchView.setIconifiedByDefault(true);
//        searchView.setIconified(true);

        searchView.setSubmitButtonEnabled(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                closeSoftKeyBoard();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cursor = DatabaseHelper.query(
                        DatabaseHelper.OFFLINE_DICTIONARY_TABLE_NAME,
                        col,
                        newText + "%");
                adapterLV = new AdapterLV(getActivity(), cursor);
                listView.setAdapter(adapterLV);
                return false;
            }
        });
    }

    /**
     * 关闭软键盘
     */
    private void closeSoftKeyBoard() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
    }

    private ArrayList<String> enList = new ArrayList<>();
    private ArrayList<String> zhList = new ArrayList<>();
    private ArrayList<String> explanationList = new ArrayList<>();

    void queryAll() {
        //_id 是以为SimpleCursorAdapter from必须要有一个_id列
        col = new String[]{DatabaseHelper.EN_WORD_COL1 + " as _id", DatabaseHelper.ZH_WORD_COL2, DatabaseHelper.EXPLANATION};
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
                closeSoftKeyBoard();
                //第一个参数是父级，第二个参数是当前item，第三个是在listView中适配器里的位置，id是当前item在ListView里的第几行的位置
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(DatabaseHelper.EN_WORD_COL1, enList);
                bundle.putStringArrayList(DatabaseHelper.ZH_WORD_COL2, zhList);
                bundle.putStringArrayList(DatabaseHelper.EXPLANATION, explanationList);
                bundle.putInt("position", position);
                bundle.putBoolean("isVisible_btnFloat", true);
                FragmentCardMode fragmentCard = new FragmentCardMode();
                fragmentCard.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).
                        replace(R.id.aux_framelayout, fragmentCard).commit();
            }
        });
    }

}
