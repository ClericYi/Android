package com.raspi.easyfarming.spot.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raspi.easyfarming.R
import kotlinx.android.synthetic.main.frag_spot_center.*

class SpotCenterFrag: Fragment(){

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.frag_spot_center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spot_tabhost.setup(context, fragmentManager, R.id.spot_maincontent)
        spot_tabhost.addTab(spot_tabhost.newTabSpec("实时数据").setIndicator("实时数据"), SpotFrag::class.java, null)
        spot_tabhost.addTab(spot_tabhost.newTabSpec("设备定位").setIndicator("设备定位"), MapFrag::class.java, null)
    }
}