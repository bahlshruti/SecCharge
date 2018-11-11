package com.aidor.secchargemobile.seccharge;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.aidor.projects.seccharge.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    Context context;
    List<ExpandableListMenuModel> mDataListHeader;
    HashMap<ExpandableListMenuModel, List<String>> mDataListChild;
    ExpandableListView view;
    View mView;

    public ExpandableListAdapter(Context mcontext, List<ExpandableListMenuModel> mDataListHeader, HashMap<ExpandableListMenuModel, List<String>> mDataListChild, ExpandableListView mview) {
        this.context = mcontext;
        this.mDataListHeader = mDataListHeader;
        this.mDataListChild = mDataListChild;

    }

    @Override
    public int getGroupCount() {
        return this.mDataListHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        return this.mDataListChild.get(this.mDataListHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mDataListHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mDataListChild.get(this.mDataListHeader.get(groupPosition)).get(childPosition);
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
        ExpandableListMenuModel model = (ExpandableListMenuModel) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.nav_menuitem_layout, null, false);
        }
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.txtMenuItem);
        lblListHeader.setText(model.getHeaderTitle());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childTitle = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.nav_submenu_layout, null);
        }
        TextView navChild = (TextView) convertView.findViewById(R.id.textViewSubmenu);
        navChild.setText(childTitle);




        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
