package com.example.asus.translation;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class AdapterVP extends PagerAdapter {
    private ArrayList<View> arrayList = null;

    AdapterVP(ArrayList<View> arrayList) {
        super();
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(arrayList.get(position));
        return arrayList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(arrayList.get(position));
    }
}
