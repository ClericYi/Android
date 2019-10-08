package com.raspi.easyfarming.device.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raspi.easyfarming.R;

import java.util.List;
import java.util.Map;

public class DetailListAdapter extends RecyclerView.Adapter{

    private Context mComtext;
    private List<Map> mList;

    public DetailListAdapter(Context mComtext, List<Map> mList){
        this.mComtext = mComtext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mComtext).inflate(R.layout.item_device_detail_point, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHolder view = (ViewHolder) viewHolder;
        Map map = mList.get(i);
        Integer title = (Integer) map.get("title");
        String text = map.get("text").toString();
        view.title.setText(title);
        view.text.setText(text);

        if(text.equals("false")) {
            view.text.setText("离线");
        }
        else if(text.equals("true")){
            view.text.setText("在线");
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView title;
        private TextView text;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_device_detail_point_title);
            text = itemView.findViewById(R.id.item_device_detail_point_text);
        }
    }
}
