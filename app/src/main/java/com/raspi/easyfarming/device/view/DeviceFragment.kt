package com.raspi.easyfarming.device.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSON.parseObject
import com.alibaba.fastjson.JSONArray
import com.raspi.easyfarming.R
import com.raspi.easyfarming.device.adapter.DeviceListAdapter
import com.raspi.easyfarming.okHttpClientModel
import okhttp3.Request

class DeviceFragment: Fragment() {

    //常量
    private val TAG = "DeviceFragment"
    private var PAGE = 0
    private val SIZE = 10
    private val GETALLDEVICE_SUCCESS = 1
    private val GETALLDEVICE_FAIL = 2
    private val GETALLDEVICE_ERROR = 3

    //数据
    private val devices:JSONArray ?= null

    //控件
    private var devicesList: RecyclerView?=null
    private var devicesEdit: TextView?=null

    //适配器
    private var deviceListAdapter:DeviceListAdapter ?= null

    //上下文
    private var mContext:Context?=null

    @SuppressLint("ValidFragment")
    constructor(context: Context){
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_device,container,false)
        initView(view)//初始化控件部分样式
        initList()//初始化列表
        return view
    }

    /**
     * 获取所有设备线程
     */
    fun startgetAllDevicesThread() {
        Log.e("GetAllDevices", "进入Thread",null)
        PAGE = 0
        devices?.clear()
        Thread(Runnable {
            kotlin.run {
                try {
                    val url = mContext?.resources?.getString(R.string.URL_Device_GetAllDevices) + PAGE + "/" + SIZE

                    val request = Request.Builder()
                            .url(url)
                            .build()

                    val response = okHttpClientModel.mOkHttpClient?.newCall(request)?.execute()

                    val result = response?.body()?.string()
                    Log.e(TAG, result, null)
                    val getResult = parseObject(parseObject(result).get("data")?.toString()).get("data")?.toString()
                    Log.e(TAG, getResult, null)
                    if (getResult == "[]") {
                        handler.sendEmptyMessage(GETALLDEVICE_FAIL)
                        return@Runnable
                    } else {
                        val getDevicesResult = JSON.parseArray(getResult)
                        devices?.addAll(getDevicesResult)
                        Log.e(TAG, ""+getDevicesResult.size, null)
                        handler.sendEmptyMessage(GETALLDEVICE_SUCCESS)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    handler.sendEmptyMessage(GETALLDEVICE_ERROR)
                }
            }
        }).start()
    }

    /*              初始化                 */

    /**
     * 初始化控件部分样式
     */
    private fun initView(view: View?) {
        devicesList = view?.findViewById(R.id.frag_deivce_rv)
        devicesEdit = view?.findViewById(R.id.frag_device_editdevice)
    }

    /**
     * 初始化列表
     */
    private fun initList(){
        deviceListAdapter = DeviceListAdapter(context,devices)
        devicesList?.layoutManager = LinearLayoutManager(context)
        devicesList?.adapter = deviceListAdapter
        //样式设置
        devicesList?.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        devicesList?.setHasFixedSize(true)
        devicesList?.itemAnimator = DefaultItemAnimator()
    }

    /**
     * 初始化Handler
     */
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                GETALLDEVICE_SUCCESS -> {
                    Log.e(TAG, "获取所有设备成功", null)
                    deviceListAdapter!!.notifyDataSetChanged()
                }
                GETALLDEVICE_FAIL -> Log.e(TAG, "无设备", null)
                GETALLDEVICE_ERROR -> Log.e(TAG, "设备获取失败" ,null)
            }
        }
    }

}