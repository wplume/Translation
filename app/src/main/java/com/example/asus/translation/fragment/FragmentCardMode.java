package com.example.asus.translation.fragment;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asus.translation.R;
import com.example.asus.translation.TranslationLab;
import com.example.asus.translation.adapter.AdapterVP;
import com.example.asus.translation.bean.NewWord;
import com.example.asus.translation.db.TranslationDBSchema;

import java.util.ArrayList;
import java.util.List;

public class FragmentCardMode extends Fragment {
    private static final String TAG = FragmentCardMode.class.getName();

    FloatingActionButton btnFloat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card, container, false);

        final ViewPager viewPager = view.findViewById(R.id.viewpager);

        ArrayList<View> viewArrayList = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {
            final List<String> enList = bundle.getStringArrayList(TranslationDBSchema.GlossaryTable.Col.EN_WORD);
            final List<String> zhList = bundle.getStringArrayList(TranslationDBSchema.GlossaryTable.Col.ZH_WORD);
            final List<String> explanationList = bundle.getStringArrayList(TranslationDBSchema.GlossaryTable.Col.EXPLANATION);


            //生成viewArrayList
            for (int i = 0; i < enList.size(); i++) {

                View v = inflater.inflate(R.layout.adapter_viewpager, container, false);
                TextView enTv = v.findViewById(R.id.en_word);
                TextView zhTv = v.findViewById(R.id.zh_word);
                TextView explanation = v.findViewById(R.id.explanation);
                //设置对应的文本
                enTv.setText(enList.get(i));
                zhTv.setText(zhList.get(i));
                explanation.setText(explanationList.get(i));
                viewArrayList.add(v);
            }
            AdapterVP adapterVP = new AdapterVP(viewArrayList);
            viewPager.setAdapter(adapterVP);
            //根据之前点击的位置，viewpager也应该显示相应的位置
            viewPager.setCurrentItem(getArguments().getInt("position"));
            //判断是否需要设置添加生词本事件，从生词本fragment过来的，是不需要添加的
            if (getArguments().getBoolean("isVisible_btnFloat", false)) {
                btnFloat = view.findViewById(R.id.btnFloat);
                btnFloat.setVisibility(View.VISIBLE);
                btnFloat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = viewPager.getCurrentItem();
                        String word = enList.get(position);

                        if (TranslationLab.get(getActivity()).queryNewWord(word)) {
                            Toast.makeText(getActivity(), "已存在", Toast.LENGTH_SHORT).show();
                        } else {
                            assert zhList != null;
                            assert explanationList != null;

                            NewWord newWord = new NewWord();
                            newWord.setEn_word(word);
                            newWord.setZh_word(zhList.get(position));
                            newWord.setExplanation(explanationList.get(position));
                            TranslationLab.get(getActivity()).addNewWord(newWord);

                            Toast.makeText(getActivity(), "已加入生词本", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        return view;
    }

    // TODO: 2018/6/25 完美全屏待解决

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUI();
    }

    private void hideSystemUI() {
        Activity activity = getActivity();
        if (activity != null) {
            Window window = activity.getWindow();
            View decorView = window.getDecorView();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        showSystemUI();
    }

    private void showSystemUI() {
        View decorView = getActivity().getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }


}
