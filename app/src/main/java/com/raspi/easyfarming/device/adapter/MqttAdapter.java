package com.raspi.easyfarming.device.adapter;

import android.content.Context;

import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.raspi.easyfarming.R;

import java.util.List;
import java.util.Map;

public class MqttAdapter extends CommonBaseAdapter<Map> {

    public MqttAdapter(Context context, List<Map> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
    }

    @Override
    protected void convert(ViewHolder viewHolder, Map map, int i) {

    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_mqtt;
    }
}
