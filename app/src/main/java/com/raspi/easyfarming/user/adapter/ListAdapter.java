package com.raspi.easyfarming.user.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.user.view.WifiConnectActivity;
import com.raspi.easyfarming.user.view.LogsActivity;
import com.raspi.easyfarming.user.view.TriggersActivity;

import java.util.List;
import java.util.Map;

public class ListAdapter extends CommonBaseAdapter<Map<String, Integer>> {

    public ListAdapter(Context context, List<Map<String, Integer>> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
    }


    @Override
    protected void convert(com.othershe.baseadapter.ViewHolder viewHolder, Map map, final int i) {
        ((TextView)viewHolder.getView(R.id.item_user_tv)).setText((Integer) map.get("text"));
        ((ImageView)viewHolder.getView(R.id.item_user_ic)).setImageResource((Integer)map.get("icon"));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_user;
    }
}
