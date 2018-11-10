package com.raspi.easyfarming.user.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.raspi.easyfarming.R;

import java.util.List;
import java.util.Map;


public class LogsListAdapter extends CommonBaseAdapter<Map> {


    public LogsListAdapter(Context context, List<Map> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
    }

    @Override
    protected void convert(ViewHolder viewHolder, Map map, int i) {
        viewHolder.setText(R.id.item_logs_date, "日期:"+map.get("date").toString());
        viewHolder.setText(R.id.item_logs_device, "设备号:"+map.get("deviceId").toString());
        viewHolder.getView(R.id.item_logs_do).setBackground( map.get("event").toString().equals("CONNECT")?mContext.getDrawable(R.drawable.ic_online_check_true):mContext.getDrawable(R.drawable.ic_online_check_false));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_logs;
    }
}
