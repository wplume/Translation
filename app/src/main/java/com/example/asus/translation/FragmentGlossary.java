package com.example.asus.translation;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by asus on 2017/12/7.
 */
public class FragmentGlossary extends Fragment {
    View view;
    ListView listView;
    AdapterLV adapterLV;
    private Cursor cursor;

    ArrayList<String> enList = new ArrayList<>();
    ArrayList<String> zhList = new ArrayList<>();
    ArrayList<String> explanationList = new ArrayList<>();
    private ActionModeCallback actionModeCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_glossary, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.setTitle(R.string.glossary);

        listView = (ListView) view.findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        actionModeCallback = new ActionModeCallback();
        listView.setMultiChoiceModeListener(actionModeCallback);
        queryAll();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //第一个参数是父级，第二个参数是当前item，第三个是在listView中适配器里的位置，id是当前item在ListView里的第几行的位置
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(DatabaseHelper.EN_WORD_COL1, enList);
                bundle.putStringArrayList(DatabaseHelper.ZH_WORD_COL2, zhList);
                bundle.putStringArrayList(DatabaseHelper.EXPLANATION, explanationList);
                bundle.putInt("position", position);

                FragmentCardMode fragmentCard = new FragmentCardMode();
                fragmentCard.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().addToBackStack(null).
                        replace(R.id.aux_framelayout, fragmentCard).commit();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        if (actionModeCallback.actionMode != null)
            actionModeCallback.actionMode.finish();
    }

    void queryAll() {
        //_id 是以为SimpleCursorAdapter from必须要有一个_id列
        String[] col = new String[]{DatabaseHelper.EN_WORD_COL1 + " as _id", DatabaseHelper.ZH_WORD_COL2, DatabaseHelper.EXPLANATION};
        //查询所有
        cursor = DatabaseHelper.getDatabase().query(DatabaseHelper.GLOSSARY_TABLE_NAME, col, null, null, null, null, null);

        System.out.println("匹配个数:" + cursor.getCount());
//        int k = cursor.getColumnIndex(DatabaseHelper.EXPLANATION);
        cursor.moveToFirst();
        // TODO: 2017/12/3 ListView还要加个表尾
        adapterLV = new AdapterLV(getActivity(), cursor);
        listView.setAdapter(adapterLV);
        for (cursor.isBeforeFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            enList.add(cursor.getString(0));
            zhList.add(cursor.getString(1));
            explanationList.add(cursor.getString(2));
        }
    }

    /**
     * Created by asus on 2017/12/9.
     */
    private class ActionModeCallback implements AbsListView.MultiChoiceModeListener {
        private ActionMode actionMode;

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            actionMode = mode;
            adapterLV.notifyDataSetChanged();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            System.out.println("进入多选模式");
            mode.getMenuInflater().inflate(R.menu.listview_multi_choice_mode, menu);
            //进入多选模式之前要把所有的复选框设置为可见
            adapterLV.setVisible(true);
//            adapterLV.notifyDataSetChanged();
            // TODO: 2017/12/8 这个要设置为true
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * 点击了菜单按钮会触发的事件
         */
        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

            switch (item.getItemId()) {
                case R.id.selectAll:
                    if (listView.getCheckedItemCount() == listView.getCount()) {
                        //取消全选
                        listView.clearChoices();
                        item.setTitle("全选");
                    } else {
                        //全选
                        for (int i = 0; i < listView.getCount(); i++) {
                            listView.setItemChecked(i, true);
                        }
                        item.setTitle("取消全选");
                    }
                    break;
                case R.id.delete:
                    (new AlertDialog.Builder(getActivity())).setIcon(R.mipmap.ic_launcher)
                            .setTitle("")
                            .setMessage("真的要删除吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (int i = 0; i < listView.getCount(); i++) {
                                        //遍历查看哪个是已选状态的，是的就删除
                                        if (listView.isItemChecked(i)) {
                                            cursor.moveToPosition(i);
                                            String where = String.format("%s = '%s'", DatabaseHelper.EN_WORD_COL1, cursor.getString(0));
                                            DatabaseHelper.getDatabase().delete(DatabaseHelper.GLOSSARY_TABLE_NAME, where, null);
                                        }
                                    }

                                    //cursor重新查询
                                    queryAll();
                                    adapterLV.setVisible(true);
                                    //删除完直接结束actionMode
                                    mode.finish();
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create().show();
                    break;
            }
            adapterLV.notifyDataSetChanged();
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            System.out.println("退出多选模式");
            //退出后要把所有的复选框设为不可见
            adapterLV.setVisible(false);
//            adapterLV.notifyDataSetChanged();
        }
    }
}
