package com.personal.windplume.translation.fragment;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.personal.windplume.translation.R;
import com.personal.windplume.translation.TranslationLab;
import com.personal.windplume.translation.adapter.AdapterVP;
import com.personal.windplume.translation.bean.NewWord;
import com.personal.windplume.translation.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class FragmentCardMode extends Fragment {
    private static final String TAG = FragmentCardMode.class.getName();
    private static final String POSITION = "position";
    private static final String IS_VISIBLE_BTN_FLOAT = "IS_VISIBLE_BTN_FLOAT";


    FloatingActionButton btnFloat;

    public static FragmentCardMode newInstance(int currentPosition, boolean isVisibleBanFloat, ArrayList<? extends Parcelable> value) {
        FragmentCardMode fragmentCardMode = new FragmentCardMode();
        Bundle bundle = new Bundle();
        bundle.putInt(POSITION, currentPosition);
        bundle.putBoolean(IS_VISIBLE_BTN_FLOAT, isVisibleBanFloat);
        bundle.putParcelableArrayList(TAG, value);

        fragmentCardMode.setArguments(bundle);
        return fragmentCardMode;
    }

    // TODO: 2018/6/25 完美全屏待解决

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_card, container, false);

        final ViewPager viewPager = view.findViewById(R.id.viewpager);

        ArrayList<View> viewArrayList = new ArrayList<>();
        Bundle bundle = getArguments();
        if (bundle != null) {

            final List<ShowFormat> showFormatList = bundle.getParcelableArrayList(TAG);
            Log.d(TAG, String.format("接收到%s个", showFormatList.size()));

            //生成viewArrayList
            for (int i = 0; i < showFormatList.size(); i++) {

                View v = inflater.inflate(R.layout.adapter_viewpager, container, false);
                TextView enTv = v.findViewById(R.id.en_word);
                TextView zhTv = v.findViewById(R.id.zh_word);
                TextView explanation = v.findViewById(R.id.explanation);

                enTv.setText(showFormatList.get(i).getEn_word());
                zhTv.setText(showFormatList.get(i).getZh_word());
                explanation.setText(StringUtil.replace(showFormatList.get(i).getExplanation()));
                viewArrayList.add(v);
            }
            AdapterVP adapterVP = new AdapterVP(viewArrayList);
            viewPager.setAdapter(adapterVP);

            //根据之前点击的位置，viewpager也应该显示相应的位置
            viewPager.setCurrentItem(getArguments().getInt("position"));

            //判断是否需要设置添加生词本事件按钮，从生词本fragment过来的，是不需要显示的
            if (getArguments().getBoolean(IS_VISIBLE_BTN_FLOAT, false)) {
                btnFloat = view.findViewById(R.id.btnFloat);
                btnFloat.setVisibility(View.VISIBLE);
                btnFloat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = viewPager.getCurrentItem();

                        //还要查询生词本中是否已经存在
                        String word = showFormatList.get(position).getEn_word();
                        if (TranslationLab.get(getActivity()).queryNewWord(word)) {
                            Toast.makeText(getActivity(), "已存在", Toast.LENGTH_SHORT).show();
                        } else {

                            NewWord newWord = new NewWord();
                            newWord.setEn_word(word);
                            newWord.setZh_word(showFormatList.get(position).getZh_word());
                            newWord.setExplanation(showFormatList.get(position).getExplanation());
                            TranslationLab.get(getActivity()).addNewWord(newWord);

                            Toast.makeText(getActivity(), "已加入生词本", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        return view;
    }

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

    public static class ShowFormat implements Parcelable {
        public static final Creator<ShowFormat> CREATOR = new Creator<ShowFormat>() {
            @Override
            public ShowFormat createFromParcel(Parcel in) {
                return new ShowFormat(in);
            }

            @Override
            public ShowFormat[] newArray(int size) {
                return new ShowFormat[size];
            }
        };
        private String en_word;
        private String zh_word;
        private String explanation;

        ShowFormat(Parcel in) {
            en_word = in.readString();
            zh_word = in.readString();
            explanation = in.readString();
        }

        public ShowFormat() {

        }

        public String getEn_word() {
            return en_word;
        }

        public void setEn_word(String en_word) {
            this.en_word = en_word;
        }

        public String getZh_word() {
            return zh_word;
        }

        public void setZh_word(String zh_word) {
            this.zh_word = zh_word;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(en_word);
            dest.writeString(zh_word);
            dest.writeString(explanation);
        }
    }
}
