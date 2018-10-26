package com.raspi.easyfarming.device.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.raspi.easyfarming.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Map> mList;
    private boolean isShow = false;
    private List<String> CheckedId;
    private ViewHolder view;


    public List<String> getCheckedId() {
        if(CheckedId.size()<1) {
            return null;
        }
        return CheckedId;
    }


    public ListAdapter(@NonNull Context mContext, @NonNull List<Map> mList){
        this.mContext = mContext;
        this.mList = mList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        view = (ViewHolder) viewHolder;
        view.name.setText(mList.get(i).get("name").toString());
        view.description.setText(mList.get(i).get("lastActiveDate").toString());
        view.is_online.setChecked((boolean)mList.get(i).get("isOnline"));

        final String id = mList.get(i).get("id").toString();

        if(isShow){
            view.is_check.setVisibility(View.VISIBLE);
        }else{
            view.is_check.setVisibility(View.GONE);
            view.is_check.setChecked(false);
            CheckedId = new ArrayList<String>();
        }

        //跳转
        final CheckBox check = view.is_check;

        view.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("OnClick", check.isChecked()+"", null);

                if(!isShow) {
//                    Intent intent = new Intent(mContext, DeviceDetailActivity.class);
//                    intent.putExtra("id", id);
//                    mContext.startActivity(intent);
                }else {
                    boolean isChecked = check.isChecked();
                    check.setChecked(!isChecked);
                    if(isChecked == false){
                        CheckedId.add(id);
                    }else{
                        CheckedId.remove(id);
                    }
                    Log.e("List", CheckedId.size()+"", null);
                }

            }
        });


        //消息发送
        final String ctrl_messagre = view.control_text.getText().toString();
        view.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//

            }
        });
    }




    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void changCheckShow(boolean isShow){
        this.isShow = isShow;
        CheckedId = new ArrayList<>();
        Log.e("ListAdapter", isShow+"", null);
        if(isShow == false)
            view.is_check.setChecked(false);
        notifyDataSetChanged();
    }

    /**
     * 该适配需要使用使用到的Holder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{

        //填入控件类型
        private TextView name;
        private TextView description;
        private Switch is_online;
        private EditText control_text;
        private AppCompatButton send;
        private CheckBox is_check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //控件初始化
            name = (TextView) itemView.findViewById(R.id.item_device_name);
            description = (TextView) itemView.findViewById(R.id.item_device_description);
            is_online = (Switch) itemView.findViewById(R.id.item_device_switch);
            control_text = (EditText) itemView.findViewById(R.id.item_device_ctrl);
            send = (AppCompatButton) itemView.findViewById(R.id.item_device_send);
            is_check = (CheckBox)itemView.findViewById(R.id.item_device_rb);
        }

    }



}
