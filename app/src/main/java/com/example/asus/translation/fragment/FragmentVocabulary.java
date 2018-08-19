package com.example.asus.translation.fragment;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.asus.translation.R;
import com.example.asus.translation.TranslationLab;
import com.example.asus.translation.adapter.AdapterLV;

import java.util.ArrayList;
import java.util.List;

import static com.example.asus.translation.db.TranslationDBSchema.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentVocabulary extends Fragment {
    private static final String TAG = FragmentVocabulary.class.toString();

    ListView listView;
    AdapterLV adapterLV;
    View view;
    Cursor cursor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_offline, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        listView = view.findViewById(R.id.listView);

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(toolbar);
            appCompatActivity.setTitle(R.string.vocabulary);
        }

        //Fragment中要设置这个menu才能显示
        setHasOptionsMenu(true);

        //addFooterView需要在setAdapter之前调用
        View footer = View.inflate(getActivity(), R.layout.footer, null);
        int height = getActivity().findViewById(R.id.bottom_navigation_view).getHeight();
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        footer.setLayoutParams(params);
        listView.addFooterView(footer);

        //设置空视图
        listView.setEmptyView(LayoutInflater.from(getActivity()).inflate(R.layout.empty_view, null));

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
                cursor = TranslationLab.get(getActivity()).queryOfflineWord(
                        VocabularyTable.Col.EN_WORD + " like?",
                        new String[]{newText + "%"}
                );
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
        Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager manager = ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE));
            if (manager != null) {
                manager.hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
            }
        }
    }

    private List<String> enList = new ArrayList<>();
    private List<String> zhList = new ArrayList<>();
    private List<String> explanationList = new ArrayList<>();

    void queryAll() {
        //_id 是以为SimpleCursorAdapter from必须要有一个_id列
        //查询所有
        cursor = TranslationLab.get(getActivity()).queryOfflineWord(null, null);

        Log.d(TAG, "匹配个数:" + cursor.getCount());

        int i = cursor.getColumnIndex(VocabularyTable.Col.EN_WORD);
        int j = cursor.getColumnIndex(VocabularyTable.Col.ZH_WORD);
        int k = cursor.getColumnIndex(VocabularyTable.Col.EXPLANATION);
        cursor.moveToFirst();
        for (cursor.isBeforeFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            enList.add(cursor.getString(i));
            zhList.add(cursor.getString(j));
            explanationList.add(cursor.getString(k));
        }

        adapterLV = new AdapterLV(getActivity(), cursor);
        listView.setAdapter(adapterLV);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //第一个参数是父级，第二个参数是当前item，第三个是在listView中适配器里的位置，id是当前item在ListView里的第几行的位置
                closeSoftKeyBoard();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(VocabularyTable.Col.EN_WORD, (ArrayList<String>) enList);
                bundle.putStringArrayList(VocabularyTable.Col.ZH_WORD, (ArrayList<String>) zhList);
                bundle.putStringArrayList(VocabularyTable.Col.EXPLANATION, (ArrayList<String>) explanationList);
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
