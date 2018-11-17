package com.raspi.easyfarming.device.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.TabHost
import com.blankj.utilcode.util.ToastUtils

import com.raspi.easyfarming.R
import kotlinx.android.synthetic.main.activity_detail_center.*

class DetailCenterAcitity: AppCompatActivity(){
    //常量
    private val TAG = "DetailCenterActivity"

    //数据
    var id:String ?= "0"
    var name:String ?="Device"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_center)
        initObject()//初始化数据
        initView()//初始化控件
        initTab()//初始化TabHost
    }

    private fun initObject() {
        id = intent?.getStringExtra("id")
        name = intent?.getStringExtra("name")
    }

    /**
     * 初始化控件
     */
    @SuppressLint("NewApi")
    private fun initView() {
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.statusBarColor = resources.getColor(R.color.bnv_color,null)
    }

    /**
     * 初始化TabHost
     */
    private fun initTab() {
        detail_tabhost.setup(baseContext, supportFragmentManager, R.id.detail_maincontent)
        detail_tabhost.addTab(detail_tabhost.newTabSpec("设备信息").setIndicator("设备信息"), DetailFrag::class.java, null)
        detail_tabhost.addTab(detail_tabhost.newTabSpec("数据监控").setIndicator("数据监控"), ChartFrag::class.java, null)
    }

    /**
     * 初始化网络广播
     */
    private fun initNetBoardcastReceiver() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
        connectivityManager?.requestNetwork(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {
            /**
             * 网络可用的回调
             */
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.e(TAG, "onAvailable")
            }

            /**
             * 网络丢失的回调
             */
            override fun onLost(network: Network) {
                super.onLost(network)
                ToastUtils.showShort("无可用的网络，请连接网络")
            }

            /**
             * 当建立网络连接时，回调连接的属性
             */
            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                Log.e(TAG, "onLinkPropertiesChanged")
            }

            /**
             * 按照官方的字面意思是，当我们的网络的某个能力发生了变化回调，那么也就是说可能会回调多次
             *
             *
             * 之后在仔细的研究
             */
            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                Log.e(TAG, "onCapabilitiesChanged")
            }

            /**
             * 在网络失去连接的时候回调，但是如果是一个生硬的断开，他可能不回调
             */
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                Log.e(TAG, "onLosing")
            }

            /**
             * 按照官方注释的解释，是指如果在超时时间内都没有找到可用的网络时进行回调
             */
            override fun onUnavailable() {
                super.onUnavailable()
                Log.e(TAG, "onUnavailable")
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish();
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        initNetBoardcastReceiver()
    }

}