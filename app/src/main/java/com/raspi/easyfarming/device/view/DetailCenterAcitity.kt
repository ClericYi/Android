package com.raspi.easyfarming.device.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TabHost

import com.raspi.easyfarming.R
import kotlinx.android.synthetic.main.activity_detail_center.*

class DetailCenterAcitity: AppCompatActivity(){

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
    private fun initView() {
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        window.statusBarColor = resources.getColor(R.color.bnv_color)
    }

    /**
     * 初始化TabHost
     */
    private fun initTab() {
        detail_tabhost.setup(baseContext, supportFragmentManager, R.id.detail_maincontent)
        detail_tabhost.addTab(detail_tabhost.newTabSpec("设备信息").setIndicator("设备信息"), DetailFrag::class.java, null)
        detail_tabhost.addTab(detail_tabhost.newTabSpec("数据监控").setIndicator("数据监控"), ChartFrag::class.java, null)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            android.R.id.home -> finish();
        }
        return false
    }

}