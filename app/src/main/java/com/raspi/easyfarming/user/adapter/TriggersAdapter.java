package com.raspi.easyfarming.user.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.List;
import java.util.Map;
import java.util.logging.Handler;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class TriggersAdapter extends CommonBaseAdapter<Map> {

    public TriggersAdapter(Context context, List<Map> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
    }


    @Override
    protected void convert(ViewHolder viewHolder, Map map, int i) {
        viewHolder.setText(R.id.item_trigger_text, map.get("name").toString());
        viewHolder.setText(R.id.item_trigger_description, map.get("name").toString());
        ((Switch)viewHolder.getView(R.id.item_trigger_switch)).setChecked((boolean) map.get("isStart"));
    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_trigger;
    }




}
