package com.example.asus.translation;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by asus on 2017/12/3.
 */
public class FragmentCard extends Fragment {
    FloatingActionButton btnFloat;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewpager, container, false);
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        ArrayList<View> viewArrayList = new ArrayList<>();
        final ArrayList<String> enList = getArguments().getStringArrayList(DatabaseHelper.EN_WORD_COL1);
        final ArrayList<String> zhList = getArguments().getStringArrayList(DatabaseHelper.ZH_WORD_COL2);
        final ArrayList<String> explanationList = getArguments().getStringArrayList(DatabaseHelper.EXPLANATION);
        //生成viewArrayList
        assert enList != null;
        for (int i = 0; i < enList.size(); i++) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.adapter_viewpager, container, false);
            TextView enTv = (TextView) v.findViewById(R.id.en_word);
            TextView zhTv = (TextView) v.findViewById(R.id.zh_word);
            TextView explanation = (TextView) v.findViewById(R.id.explanation);
            //设置对应的文本
            enTv.setText(enList.get(i));
            assert zhList != null;
            zhTv.setText(zhList.get(i));
            assert explanationList != null;
            explanation.setText(explanationList.get(i));
            viewArrayList.add(v);
        }
        AdapterVP adapterVP = new AdapterVP(viewArrayList);
        viewPager.setAdapter(adapterVP);
        //根据之前点击的位置，viewpager也应该显示相应的位置
        viewPager.setCurrentItem(getArguments().getInt("position"));
        //判断是否需要设置添加生词本事件，从生词本fragment过来的，是不需要添加的
        if (getArguments().getBoolean("isVisible_btnFloat", false)) {
            btnFloat = (FloatingActionButton) view.findViewById(R.id.btnFloat);
            btnFloat.setVisibility(View.VISIBLE);
            btnFloat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = viewPager.getCurrentItem();
                    String word = enList.get(position);
                    if (DatabaseHelper.queryIsExist(word)) {
                        Toast.makeText(getActivity(), "已存在", Toast.LENGTH_SHORT).show();
                    } else {
                        assert zhList != null;
                        assert explanationList != null;
                        DatabaseHelper.insertGlossary(word, zhList.get(position), explanationList.get(position));
                        Toast.makeText(getActivity(), "已加入生词本", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        return view;
    }
}
