package com.raspi.easyfarming.device.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raspi.easyfarming.R
import kotlinx.android.synthetic.main.frag_device.*

class DeviceCenterFrag: Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.frag_device, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        device_tabhost.setup(context, fragmentManager, R.id.device_maincontent)
        device_tabhost.addTab(device_tabhost.newTabSpec("监控台").setIndicator("监控台"), OnlineFrag::class.java, null)
        device_tabhost.addTab(device_tabhost.newTabSpec("设备列表").setIndicator("设备列表"), DeviceFrag::class.java, null)
    }
}