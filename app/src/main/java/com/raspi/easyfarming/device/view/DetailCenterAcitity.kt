package com.raspi.easyfarming.device.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import com.raspi.easyfarming.R
import kotlinx.android.synthetic.main.activity_detail_center.*

class DetailCenterAcitity: AppCompatActivity(){

    //数据
    var id:String ?= "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_center)
        initView()//初始化控件
        initTab()//初始化TabHost
        initObject()//初始化数据
    }

    private fun initObject() {
        id = intent?.getStringExtra("id")
    }

    /**
     * 初始化控件
     */
    private fun initView() {
        supportActionBar?.title = "Device"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * 初始化TabHost
     */
    private fun initTab() {
        detail_tabhost.setup(baseContext, supportFragmentManager, R.id.detail_maincontent)
        detail_tabhost.addTab(detail_tabhost.newTabSpec("设备信息").setIndicator("设备信息"), DetailFrag::class.java, null)
        detail_tabhost.addTab(detail_tabhost.newTabSpec("数据监控").setIndicator("数据监控"), MqttFrag::class.java, null)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish();
        }
        return false
    }

}