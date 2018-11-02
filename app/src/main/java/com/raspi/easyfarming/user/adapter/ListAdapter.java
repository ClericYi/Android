package com.raspi.easyfarming.user.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.raspi.easyfarming.R;
import com.raspi.easyfarming.user.view.WifiConnectActivity;
import com.raspi.easyfarming.user.view.LogsActivity;
import com.raspi.easyfarming.user.view.TriggersActivity;

import java.util.List;
import java.util.Map;

public class ListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Map<String, Object>> mList;

    public ListAdapter(List<Map<String, Object>> mList, Context mContext){
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_user, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        ViewHolder view = (ViewHolder) viewHolder;

        Map<String,Object> map = mList.get(i);
        final Integer text = (Integer) map.get("text");
        view.text.setText(text);
        view.icon.setImageResource((Integer)map.get("icon"));
        view.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(i == 1){
                    Intent intent = new Intent(mContext, LogsActivity.class);
                    mContext.startActivity(intent);
                } else if(i == 2){
                    Intent intent = new Intent(mContext, TriggersActivity.class);
                    mContext.startActivity(intent);
                }else if(i == 3){
                    Intent intent = new Intent(mContext, WifiConnectActivity.class);
                    mContext.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    //该适配使用的ViewHolder
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;//条目名称
        ImageView icon;//条目图标

         ViewHolder(View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_user_tv);
            icon = itemView.findViewById(R.id.item_user_ic);
        }
    }
}
