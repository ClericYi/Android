package com.raspi.easyfarming.main.view;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.raspi.easyfarming.R;
import com.raspi.easyfarming.utils.network.NetBroadcastReceiver;

public class MainActivity extends AppCompatActivity {

    //常量
    private MainActivity self = MainActivity.this;
    private final String TAG ="MainAcitivity";

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    //控件
    private BottomNavigationView bottomNavigationView;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();//初始化控件

    }



    /***********************    初始化    ****************************/

    /**
     * 初始化控件
     */
    private void initView() {
        getSupportActionBar().hide();
    }

    /**
     * 初始化网络广播
     */
    private void initNetBoardcastReceiver() {
        if (netBroadcastReceiver == null) {
            netBroadcastReceiver = new NetBroadcastReceiver();
            netBroadcastReceiver.setNetChangeListern(new NetBroadcastReceiver.NetChangeListener() {
                @Override
                public void onChangeListener(boolean status) {
                    if (status) {


                    } else {
                        Toast.makeText(self, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netBroadcastReceiver, filter);
    }

    /***********************    生命周期中的操作    ****************************/

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initNetBoardcastReceiver();
    }
}
