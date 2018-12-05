package com.personal.asus.translation.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.personal.asus.translation.R;

public class AdapterLV extends BaseAdapter {

    private static final String TAG = AdapterLV.class.getName();

    private Context context;
    private Cursor cursor;
    private int isVisible = CheckBox.INVISIBLE;

    public void setVisible(int visible) {
        isVisible = visible;
    }

    public AdapterLV(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * 当ListView被用户滚动的时候，这个方法就会一直被调用，以此来加载布局
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            //设置适配器的xml文件
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_lv, parent, false);

            //viewHold是为了减少findViewById的次数
            viewHolder = new ViewHolder();
            viewHolder.textView1 = convertView.findViewById(R.id.en_word);
            viewHolder.textView2 = convertView.findViewById(R.id.zh_word);
            viewHolder.checkBox = convertView.findViewById(R.id.checkBox);

            //setTag是为了减少inflate的次数
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        cursor.moveToPosition(position);
        viewHolder.textView1.setText(cursor.getString(0));
        viewHolder.textView2.setText(!cursor.getString(1).equals("") ? cursor.getString(1) : cursor.getString(2));

        //是否可见
        viewHolder.checkBox.setVisibility(isVisible);

        //是否勾选
        viewHolder.checkBox.setChecked(((ListView) parent).isItemChecked(position));
        return convertView;
    }

    private class ViewHolder {
        TextView textView1;
        TextView textView2;
        CheckBox checkBox;
    }
}
