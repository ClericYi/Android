package com.raspi.easyfarming.user.adapter;

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


public class LogsListAdapter extends RecyclerView.Adapter {

    private List<Map> mList;
    private Context mContext;

    public LogsListAdapter(Context mContext, List<Map> mList){
        this.mList = mList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHodler(LayoutInflater.from(mContext).inflate(R.layout.item_logs, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        ViewHodler view = (ViewHodler) viewHolder;
        Map map = mList.get(i);
        view.date.setText(map.get("date").toString());
        view.device.setText("设备号:"+map.get("deviceId").toString());
        if(map.get("event").toString().equals("CONNECT"))
            view.doing.setText("上线");
        else
            view.doing.setText("下线");

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHodler extends RecyclerView.ViewHolder{

        private TextView date;
        private TextView device;
        private TextView doing;

        public ViewHodler(@NonNull View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.item_logs_date);
            device = (TextView) itemView.findViewById(R.id.item_logs_device);
            doing = (TextView) itemView.findViewById(R.id.item_logs_do);
        }
    }

}
