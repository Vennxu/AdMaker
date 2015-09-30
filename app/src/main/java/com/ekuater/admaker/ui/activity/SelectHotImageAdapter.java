package com.ekuater.admaker.ui.activity;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ekuater.admaker.R;
import com.ekuater.admaker.datastruct.DayHotIssues;
import com.ekuater.admaker.datastruct.HotIssue;
import com.ekuater.admaker.ui.util.DateTimeUtils;
import com.ekuater.admaker.ui.widget.NoScrollGridView;

/**
 * Created by Administrator on 2015/8/11.
 */
public class SelectHotImageAdapter extends BaseExpandableListAdapter implements
        AdapterView.OnItemClickListener {

    private Context context;
    private DayHotIssues[] dayHotIssues;
    private LayoutInflater inflater;
    private NoScrollGridView gridView;
    private SelectChangeListener selectChangeListener;

    public SelectHotImageAdapter(Context context, DayHotIssues[] dayHotIssues){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.dayHotIssues = dayHotIssues;
    }

    public void addDayHotIssues(DayHotIssues[] dayHotIssues){
        this.dayHotIssues = dayHotIssues;
        notifyDataSetChanged();
    }

    public void setSelectChangeListener(SelectChangeListener selectChangeListener){
        this.selectChangeListener = selectChangeListener;
    }

    @Override
    public int getGroupCount() {
        return dayHotIssues == null ? 0 : dayHotIssues.length;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public DayHotIssues getGroup(int groupPosition) {
        return dayHotIssues[groupPosition];
    }

    @Override
    public HotIssue getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).getHotIssues()[childPosition];
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_select_hot_group_image, parent, false);
        TextView groupText = (TextView) convertView.findViewById(R.id.hot_group_text);
        groupText.setText(DateTimeUtils.getDateString(context,getGroup(groupPosition).getDate(),true));
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_select_hot_child_image_grid, null);
            gridView = (NoScrollGridView) convertView
                    .findViewById(R.id.hot_child_grid);
            gridView.setNumColumns(3);// 设置每行列数
            gridView.setGravity(Gravity.CENTER);// 位置居中
            gridView.setHorizontalSpacing(10);// 水平间隔
            gridView.setOnItemClickListener(this);
            gridView.setAdapter(new SelectHotImageGridAdapter(context, getGroup(groupPosition).getHotIssues()));// 设置菜单Adapter
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
                            int position, long id) {
        HotIssue hotissue = (HotIssue) adapterView
                .getItemAtPosition(position);
        if (hotissue != null && selectChangeListener != null) {
            selectChangeListener.onSelectChange(hotissue);
        }

    }

    public interface SelectChangeListener{
        void onSelectChange(HotIssue hotIssue);
    }
}
