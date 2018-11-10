package com.raspi.easyfarming.device.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.device.adapter.DetailListAdapter;
import com.raspi.easyfarming.device.adapter.DetailOtherListAdapter;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class DetailFrag extends Fragment {
    //常量
    private static final String TAG = "DeviceDetailActivity";
    private final int DEVICE_SUCCESS =1;
    private final int DEVICE_FAIL =2;
    private final int DEVICE_ERROR =3;

    //数据
    private String id;
    private String result;
    com.alibaba.fastjson.JSONObject map;
    private int[] baseTitle = {
            R.string.device_detail_id,
            R.string.device_detail_name,
            R.string.device_detail_online
    };


    private int[] moreTitle = {
            R.string.device_detail_description,
            R.string.device_detail_location_describe,
            R.string.device_detail_location_longitude,
            R.string.device_detail_location_latitude,
            R.string.device_detail_group,
            R.string.device_detail_group_des
    };
    private List<Map> baseListMaps;
    private List<Map> moreListMaps;


    //控件
    RecyclerView deviceBaseList;
    RecyclerView deviceMoreList;


    //handler
    private Handler handler;

    //适配器
    private DetailListAdapter deviceDetailListAdapter;
    private DetailOtherListAdapter deviceDetailOtherListAdapter;

    //View
    private View view;
    private int flag = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.from(getContext()).inflate(R.layout.frag_device_detail, null);
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        if(flag == 0){
            initEach();//初始化所有
        }
        return view;
    }

    /**
     * 初始化所有
     */
    private void initEach(){
        initView(view);//初始化控件
        initHandler();//初始化Handler
        initList();//初始化列表
        flag = 1;
    }


    /******************************************* 线程处理 *********************************************/

    /**
     * 启动线程
     */
    public void getInformationThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Request request = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_Device_GetDeviceDetail)+id)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    result = response.body().string();
                    map = parseObject(parseObject(result).get("data").toString());

                    Log.e(TAG, result, null);
                    if(result.equals("{}")) {
                        handler.sendEmptyMessage(DEVICE_FAIL);
                        return;
                    } else {
                        //处理数据
                        baseListMaps.clear();
                        moreListMaps.clear();
                        baseResultDeal(map);
                        moreResultDeal(map);
                        handler.sendEmptyMessage(DEVICE_SUCCESS);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "run: Login error!", null);
                    handler.sendEmptyMessage(DEVICE_ERROR);
                }
            }

            /**
             * 用于数据处理
             * @param map
             */
            private void baseResultDeal(Map map) {

                for(int i = 0; i<baseTitle.length; i++) {
                    Map map1 = new HashMap();
                    if(i == 0 ) {
                        map1.put("text", CheckNull(map.get("openId")));
                    }else if(i == 1){
                        map1.put("text", CheckNull(map.get("name")));
                    }else if(i == 2){
                        map1.put("text", CheckNull(map.get("isOnline")));
                    }
                    map1.put("title", baseTitle[i]);
                    baseListMaps.add(map1);
                }
            }

            /**
             * 用于数据处理
             * @param map
             */
            private void moreResultDeal(Map map) {

                for(int i = 0; i<moreTitle.length; i++) {
                    Map map1 = new HashMap();
                    if(i == 0 ) {
                        map1.put("text", CheckNull(map.get("describe")));
                    }else if(i == 1){
                        map1.put("text", CheckNull(parseObject(map.get("location").toString()).get("describe")));
                    }else if(i == 2){
                        map1.put("text", CheckNull(parseObject(map.get("location").toString()).get("longitude")));
                    }else if(i == 3 ) {
                        map1.put("text", CheckNull(parseObject(map.get("location").toString()).get("latitude")));
                    }else if(i == 4){
                        map1.put("text", CheckNull(parseObject(map.get("group").toString()).get("name")));
                    }else if(i == 5){
                        map1.put("text", CheckNull(parseObject(map.get("group").toString()).get("comment")));
                    }
                    map1.put("title", moreTitle[i]);
                    moreListMaps.add(map1);
                }
            }

            private String CheckNull(Object text){
                if(text == null)
                    return "无信息";
                else
                    return text.toString();
            }

        }).start();
    }


    /*********************************** 初始化 *********************************/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        id = ((DetailCenterAcitity)context).getId();
    }

    /**
     * 初始化功能列表
     */
    private void initList() {
        baseListMaps = new ArrayList<Map>();

        deviceDetailListAdapter = new DetailListAdapter(getContext(), baseListMaps);
        deviceBaseList.setAdapter(deviceDetailListAdapter);
        deviceBaseList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceBaseList.setHasFixedSize(true);
        deviceBaseList.setItemAnimator(new DefaultItemAnimator());
        deviceBaseList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));


        moreListMaps = new ArrayList<Map>();
        deviceDetailOtherListAdapter = new DetailOtherListAdapter(getContext(), moreListMaps);
        deviceMoreList.setAdapter(deviceDetailOtherListAdapter);
        deviceMoreList.setLayoutManager(new LinearLayoutManager(getContext()));
        deviceMoreList.setHasFixedSize(true);
        deviceMoreList.setItemAnimator(new DefaultItemAnimator());
        deviceMoreList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getInformationThread();
    }

    /**
     * 初始化控件
     * @param view
     */
    private void initView(View view) {
        deviceBaseList = view.findViewById(R.id.device_detail_baseinfo);
        deviceMoreList = view.findViewById(R.id.device_detail_moreinfo);
    }

    /**
     * 初始化handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case DEVICE_SUCCESS:
                        deviceDetailListAdapter.notifyDataSetChanged();
                        deviceDetailOtherListAdapter.notifyDataSetChanged();
                        break;
                    case DEVICE_FAIL:
                        Toast.makeText(getContext(),"没有查询到数据", Toast.LENGTH_SHORT ).show();
                        break;
                    case DEVICE_ERROR:
                        Toast.makeText(getContext(),"无可用的网络，请连接网络", Toast.LENGTH_SHORT ).show();
                        break;
                }
                return false;
            }
        });
    }

    /******************************** 生命周期 **********************************/
    @Override
    public void onResume() {
        super.onResume();
        initThread();//初始化线程
    }
}
