package com.raspi.easyfarming.device.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import com.alibaba.fastjson.JSONArray
import com.raspi.easyfarming.R
import java.util.ArrayList


class DeviceListAdapter: RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

    var mContext: Context?=null
    var mList:JSONArray?=null
    var viewHolder:ViewHolder?=null
    var isShow = false
    var CheckedIds:ArrayList<String>?=null

    constructor(mContext:Context?, mList: JSONArray?){
        this.mContext = mContext
        this.mList = mList
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device,p0,false))
    }

    //因为无法暂时无法解决为空的情况使用报错来处理
    override fun getItemCount(): Int {
        try {
            return mList!!.size
        }catch (e:Exception){
            return 0
        }
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        viewHolder  = p0
        val map = mList!![p1] as Map<String, String>
        p0.description.setText(map.get("lastActiveDate"))
        p0.name.text = map.get("name")
        p0.is_online.isChecked = map.get("isOnline") as Boolean
    }


    fun changCheckShow(isShow: Boolean) {
        this.isShow = isShow
        CheckedIds = ArrayList<String>()
        Log.e("DeviceListAdapter", isShow.toString() + "", null)
        if (isShow == false)
            viewHolder?.is_check?.isChecked = false
        notifyDataSetChanged()
    }

    class ViewHolder(view:View): RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.item_device_name)
        val description = view.findViewById<TextView>(R.id.item_device_description)
        val is_online = view.findViewById<Switch>(R.id.item_device_switch)
        val is_check = view.findViewById<CheckBox>(R.id.item_device_rb)
    }
}