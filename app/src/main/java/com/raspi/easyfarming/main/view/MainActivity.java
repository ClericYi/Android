package com.raspi.easyfarming.main.view;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.raspi.easyfarming.R;
import com.raspi.easyfarming.device.view.CenterFrag;
import com.raspi.easyfarming.device.view.DeviceFrag;
import com.raspi.easyfarming.device.view.OnlineFrag;
import com.raspi.easyfarming.user.view.UserFrag;
import com.raspi.easyfarming.utils.network.NetBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    //常量
    private MainActivity self = MainActivity.this;
    private final String TAG ="MainAcitivity";

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    //控件
    @BindView(R.id.main_bnv)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.main_vp)
    ViewPager viewPager;

    //数据
    private List<Fragment> fragments;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(self);
        initView();//初始化控件
        initPager();//初始化ViewPager
        initBottomNav();//初始化bottomNavigationView
    }




    /***********************    初始化    ****************************/


    /**
     * 初始化bottomNavigationView
     */
    private void initBottomNav() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int position = 0;
                switch (menuItem.getItemId()){
                    case R.id.main_bnv_device:
                        position = 0;
                        break;
                    case R.id.main_bnv_user:
                        position = 1;
                        break;
                }
                viewPager.setCurrentItem(position);
                menuItem.setChecked(true);
                return false;
            }
        });
    }

    /**
     * 初始化分页
     */
    private void initPager() {
        viewPager.setOffscreenPageLimit(2);

        CenterFrag centerFrag  = new CenterFrag();
        UserFrag userFrag = new UserFrag();
        //添加fragment
        fragments = new ArrayList<Fragment>();
        fragments.add(centerFrag);
        fragments.add(userFrag);

        //适配器
        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });

        //页面切换事件
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            //图标Id
            int[] itemIds = {
                    R.id.main_bnv_device,
                    R.id.main_bnv_user
            };

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.setSelectedItemId(itemIds[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

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
