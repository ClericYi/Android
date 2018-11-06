package com.raspi.easyfarming.device.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
        viewHolder.getView(R.id.item_device_rb).setVisibility(isShow?View.VISIBLE:View.GONE);
        switch (CheckNull(map.get("deviceType"))) {
            case "TEMP":
                ((ImageView)viewHolder.getView(R.id.item_device_img))
                        .setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_temp_true):mContext.getDrawable(R.drawable.ic_temp_false));
                break;
            case "HUMID":
                ((ImageView)viewHolder.getView(R.id.item_device_img))
                        .setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_humid_true):mContext.getDrawable(R.drawable.ic_humid_false));
                break;
            case "PRESSURE":
                ((ImageView)viewHolder.getView(R.id.item_device_img)).
                        setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_pressure_true):mContext.getDrawable(R.drawable.ic_pressure_false));
                break;
            case "RAIN":
                ((ImageView)viewHolder.getView(R.id.item_device_img))
                        .setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_rain_true):mContext.getDrawable(R.drawable.ic_rain_false));
                break;
            case "ILLUMINANCE":
                ((ImageView)viewHolder.getView(R.id.item_device_img))
                        .setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_ill_true):mContext.getDrawable(R.drawable.ic_ill_false));
                break;
            case "LIVE":
                ((ImageView)viewHolder.getView(R.id.item_device_img))
                        .setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_spot_true):mContext.getDrawable(R.drawable.ic_spot_false));
                break;
            case "DEVICE":
                ((ImageView)viewHolder.getView(R.id.item_device_img))
                        .setImageDrawable((boolean)map.get("isOnline")?
                                mContext.getDrawable(R.drawable.ic_device_self_true):mContext.getDrawable(R.drawable.ic_device_self_false));
                break;
        }
    }

    private String CheckNull(Object text){
        if(text==null)
            return "DEVICE";
        else
            return text.toString();
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
