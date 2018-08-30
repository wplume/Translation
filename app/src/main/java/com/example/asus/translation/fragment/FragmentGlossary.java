package com.example.asus.translation.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import com.example.asus.translation.R;
import com.example.asus.translation.TranslationLab;
import com.example.asus.translation.adapter.AdapterLV;
import com.example.asus.translation.bean.NewWord;
import com.example.asus.translation.db.BeanCursorWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class FragmentGlossary extends Fragment implements Observer {
    private static final String TAG = FragmentGlossary.class.toString();

    View view;
    ListView listView;
    AdapterLV adapterLV;
    private BeanCursorWrapper beanCursorWrapper;

    private List<FragmentCardMode.ShowFormat> showFormatList = new ArrayList<>();
    private ActionModeCallback actionModeCallback;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_glossary, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.setSupportActionBar(toolbar);
        appCompatActivity.setTitle(R.string.glossary);

        TranslationLab.get(getActivity()).addObserver(this);

        listView = view.findViewById(R.id.listView);

        View footer = View.inflate(getActivity(), R.layout.footer, null);
        int height = getActivity().findViewById(R.id.bottom_navigation_view).getHeight();
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
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
                if (position != listView.getCount() - 1) {

                    FragmentCardMode fragmentCard = FragmentCardMode.newInstance(
                            position,
                            false,
                            (ArrayList<? extends Parcelable>) showFormatList
                    );

                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.aux_framelayout, fragmentCard)
                            .commit();
                }
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
        beanCursorWrapper.close();
        if (actionModeCallback.actionMode != null) {
            actionModeCallback.actionMode.finish();
        }
        TranslationLab.get(getActivity()).deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        showFormatList.clear();
        queryAll();
    }

    private void queryAll() {
        //查询所有
        beanCursorWrapper = TranslationLab.get(getActivity()).queryNewWord(null, null);

        Log.d(TAG, "查询到的生词个数:" + beanCursorWrapper.getCount());
        beanCursorWrapper.moveToFirst();

        adapterLV = new AdapterLV(getActivity(), beanCursorWrapper);
        listView.setAdapter(adapterLV);
        for (beanCursorWrapper.isBeforeFirst(); !beanCursorWrapper.isAfterLast(); beanCursorWrapper.moveToNext()) {
            NewWord newWord = beanCursorWrapper.getNewWord();
            FragmentCardMode.ShowFormat showFormat = new FragmentCardMode.ShowFormat();
            showFormat.setEn_word(newWord.getEn_word());
            showFormat.setZh_word(newWord.getZh_word());
            showFormat.setExplanation(newWord.getExplanation());
            showFormatList.add(showFormat);
        }
    }

    /*--------------------------------------------------------------------------------------------*/

    private class ActionModeCallback implements MultiChoiceModeListener {
        private ActionMode actionMode;

        /**
         * 在选择模式期间选中或取消选中某个项目时调用。item被选中时，ActionMode自身并没有什么变化，用来提示用户，
         * 所以这个方法是用来自定义item被选中的表现形式，比如说CheckBox。
         */
        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            adapterLV.notifyDataSetChanged();//只能改变item里面的view的状态，并不能改变item的个数
        }

        /**
         * 首次创建 ActionMode 时调用。提供的菜单将用于为 ActionMode 生成动作按钮。
         *
         * @param mode 正在创建的ActionMode
         * @param menu 用于填充action button的menu
         * @return 如果应创建操作模式，则为true;如果应该中止进入此模式，则为false。
         */
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d(TAG, "进入多选模式");
            actionMode = mode;
            mode.getMenuInflater().inflate(R.menu.listview_multi_choice_mode, menu);
            //进入多选模式之前要把所有的复选框设置为可见
            adapterLV.setVisible(CheckBox.VISIBLE);
            return true;
        }

        /**
         * 每当它无效时调用刷新ActionMode的action menu button
         *
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
         *
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
                            .setTitle("警告！")
                            .setMessage("真的要删除吗？确定已经记住了？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(TAG, "onClick: " + listView.getCount());
                                    //表尾占了一个，所以要-1
                                    for (int i = 0; i < listView.getCount() - 1; i++) {
                                        //遍历查看哪个是已选状态的，是的就删除
                                        if (listView.isItemChecked(i)) {
                                            beanCursorWrapper.moveToPosition(i);
                                            TranslationLab.get(getActivity()).deleteNewWord(beanCursorWrapper.getString(0));
                                        }
                                    }

                                    queryAll();//cursor重新查询
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
            adapterLV.setVisible(CheckBox.INVISIBLE);
        }
    }

}
