package com.example.asus.translation;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    private static final String TAG = FragmentGlossary.class.toString();

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_glossary, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.setTitle(R.string.glossary);

        listView = view.findViewById(R.id.listView);

        View footer = View.inflate(getActivity(), R.layout.footer, null);
        int height = getActivity().findViewById(R.id.bottom_navigation_view).getHeight();
        AbsListView.LayoutParams params=new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height);
        footer.setLayoutParams(params);
        listView.addFooterView(footer);

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

    //fragment调用hide()或者show()，生命周期的回调函数将不执行
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (actionModeCallback.actionMode != null)
                actionModeCallback.actionMode.finish();
        }
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

        Log.d(TAG, "匹配个数:" + cursor.getCount());
//        int k = cursor.getColumnIndex(DatabaseHelper.EXPLANATION);
        cursor.moveToFirst();

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

        //在选择模式期间选中或取消选中某个项目时调用。
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            actionMode = mode;
            adapterLV.notifyDataSetChanged();
        }

        /**
         * 首次创建 ActionMode 时调用。提供的菜单将用于为 ActionMode 生成动作按钮。
         * @param mode 正在创建的ActionMode
         * @param menu 用于填充action button的menu
         * @return 如果应创建操作模式，则为true;如果应该中止进入此模式，则为false。
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "进入多选模式");
            mode.getMenuInflater().inflate(R.menu.listview_multi_choice_mode, menu);
            //进入多选模式之前要把所有的复选框设置为可见
            adapterLV.setVisible(true);
//            adapterLV.notifyDataSetChanged();
            return true;
        }

        /**
         * 每当它无效时调用刷新ActionMode的action menu button
         * @param mode ActionMode正在准备中
         * @param menu 用于填充操作按钮的菜单
         * @return 如果菜单或操作模式已更新，则为true，否则为false。
         */
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        /**
         * 被调用以报告用户单击操作按钮。
         * @param mode 当前的ActionMode
         * @param item 单击的项目
         * @return 如果此回调处理事件，则返回true;如果标准MenuItem调用应继续，则返回false。
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
            Log.d(TAG, "退出多选模式");
            //退出后要把所有的复选框设为不可见
            adapterLV.setVisible(false);
//            adapterLV.notifyDataSetChanged();
        }
    }
}
