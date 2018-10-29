package com.raspi.easyfarming.spot.adapter;

import android.content.Context;
import android.widget.ImageView;

import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.base.CommonBaseAdapter;
import com.raspi.easyfarming.R;

import java.util.List;
import java.util.Map;

public class SpotAdapter extends CommonBaseAdapter<Map> {
    public SpotAdapter(Context context, List<Map> datas, boolean isOpenLoadMore) {
        super(context, datas, isOpenLoadMore);
    }

    @Override
    protected void convert(ViewHolder viewHolder, Map map, int i) {
        viewHolder.setText(R.id.item_spot_text, map.get("name").toString());
        switch (map.get("type").toString()){
            case "TEMP":
                viewHolder.setText(R.id.item_spot_data, map.get("value").toString()+'℃');
                ((ImageView)viewHolder.getView(R.id.item_spot_img)).setImageDrawable(mContext.getDrawable(R.drawable.ic_temp));
                break;
            case "HUMID":
                viewHolder.setText(R.id.item_spot_data, map.get("value").toString()+'％');
                ((ImageView)viewHolder.getView(R.id.item_spot_img)).setImageDrawable(mContext.getDrawable(R.drawable.ic_humid));
                break;
            case "PRESSURE":
                viewHolder.setText(R.id.item_spot_data, map.get("value").toString()+"hPa");
                ((ImageView)viewHolder.getView(R.id.item_spot_img)).setImageDrawable(mContext.getDrawable(R.drawable.ic_pressure));
                break;
            case "RAIN":
                if(map.get("value").toString().equals("loading...")) {
                    viewHolder.setText(R.id.item_spot_data, "loading...");
                }else if(Float.parseFloat(map.get("value").toString())>100){
                    viewHolder.setText(R.id.item_spot_data, "下雨中");
                }else{
                    viewHolder.setText(R.id.item_spot_data, "未下雨");
                }
                ((ImageView)viewHolder.getView(R.id.item_spot_img)).setImageDrawable(mContext.getDrawable(R.drawable.ic_rain));
                break;
            case "ILLUMINANCE":
                viewHolder.setText(R.id.item_spot_data, map.get("value").toString()+"lx");
                ((ImageView)viewHolder.getView(R.id.item_spot_img)).setImageDrawable(mContext.getDrawable(R.drawable.ic_ill));
                break;
            case "LIVE":
                viewHolder.setText(R.id.item_spot_data, "监控中");
                ((ImageView)viewHolder.getView(R.id.item_spot_img)).setImageDrawable(mContext.getDrawable(R.drawable.ic_spot));
                break;
        }


    }

    @Override
    protected int getItemLayoutId() {
        return R.layout.item_spot;
    }
}
