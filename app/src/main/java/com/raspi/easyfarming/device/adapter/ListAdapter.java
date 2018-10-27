package com.raspi.easyfarming.device.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.raspi.easyfarming.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListAdapter extends CommonBaseAdapter<Map> {

    private boolean isShow = false;
    private List<String> CheckedId;

    public ListAdapter(Context context, List<Map> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
        CheckedId = new ArrayList<>();
    }

    @Override
    protected void convert(ViewHolder viewHolder, Map map, int i) {
        viewHolder.setText(R.id.item_device_name, map.get("name").toString());
        viewHolder.setText(R.id.item_device_description, map.get("lastActiveDate").toString());
        ((Switch)viewHolder.getView(R.id.item_device_switch)).setChecked((boolean)map.get("isOnline"));
        viewHolder.getView(R.id.item_device_rb).setVisibility(isShow?View.VISIBLE:View.GONE);
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_device;
    }


    public List<String> getCheckedId() {
        return CheckedId;
    }


    public void changCheckShow(boolean isShow){
        this.isShow = isShow;
        CheckedId.clear();
        Log.e("ListAdapter", isShow+"", null);
        notifyDataSetChanged();
    }


}
