package com.personal.windplume.translation.fragment;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import com.personal.windplume.translation.R;
import com.personal.windplume.translation.TranslationLab;
import com.personal.windplume.translation.adapter.AdapterLV;
import com.personal.windplume.translation.bean.OfflineWord;
import com.personal.windplume.translation.db.BeanCursorWrapper;

import java.util.ArrayList;
import java.util.List;

import static com.personal.windplume.translation.db.TranslationDBSchema.VocabularyTable;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentVocabulary extends Fragment {
    private static final String TAG = FragmentVocabulary.class.toString();

    ListView listView;
    AdapterLV adapterLV;
    View view;
    BeanCursorWrapper beanCursorWrapper;
    private List<FragmentCardMode.ShowFormat> showFormatList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_vocabulary, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.vocabulary);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = getActivity().findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        listView = view.findViewById(R.id.listView);

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
        beanCursorWrapper.close();
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
                beanCursorWrapper = TranslationLab.get(getActivity()).queryOfflineWord(
                        VocabularyTable.Col.EN_WORD + " like?",
                        new String[]{newText + "%"}
                );
                adapterLV = new AdapterLV(getActivity(), beanCursorWrapper);
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

    void queryAll() {
        //_id 是以为SimpleCursorAdapter from必须要有一个_id列
        //查询所有
        beanCursorWrapper = TranslationLab.get(getActivity()).queryOfflineWord(null, null);

        Log.d(TAG, "匹配个数:" + beanCursorWrapper.getCount());

        beanCursorWrapper.moveToFirst();
        for (beanCursorWrapper.isBeforeFirst(); !beanCursorWrapper.isAfterLast(); beanCursorWrapper.moveToNext()) {
            OfflineWord offlineWord = beanCursorWrapper.getOfflineWord();
            FragmentCardMode.ShowFormat showFormat = new FragmentCardMode.ShowFormat();
            showFormat.setEn_word(offlineWord.getEn_word());
            showFormat.setZh_word(offlineWord.getZh_word());
            showFormat.setExplanation(offlineWord.getExplanation());
            showFormatList.add(showFormat);
        }

        adapterLV = new AdapterLV(getActivity(), beanCursorWrapper);
        listView.setAdapter(adapterLV);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //第一个参数是父级，第二个参数是当前item，第三个是在listView中适配器里的位置，id是当前item在ListView里的第几行的位置
                closeSoftKeyBoard();

                FragmentCardMode fragmentCard = FragmentCardMode.newInstance(
                        position,
                        true,
                        (ArrayList<? extends Parcelable>) showFormatList
                );
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.aux_framelayout, fragmentCard)
                        .commit();
            }
        });
    }

}
