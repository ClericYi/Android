package com.raspi.easyfarming.spot.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.spot.model.MarketBean;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;

public class MapFrag extends Fragment {

    //常量
    private final String TAG = "MapFrag";
    private final int GET_SUCCESS = 0;
    private final int GET_FAILED = 1;
    private final int GET_ERROR = 2;

    //handler
    private Handler handler;

    //控件
    private MapView mapView;
    private AMap aMap;

    //数据
    private List<MarketBean> marketList;

    //View
    private View view;
    private int flag = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.from(getContext()).inflate(R.layout.frag_map, null);
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        if(flag == 0){
            initEach(savedInstanceState);
        }
        return view;
    }

    /**
     * 初始化所有
     */
    private void initEach(@Nullable Bundle savedInstanceState) {
        initView(view);
        initHandler();
        initMap(savedInstanceState);//初始化地图
        flag = 1;
    }



    /************************************ 函数 ********************************************/
    private void addMoreMarket() {

        for (int i = 0; i < marketList.size(); i++) {
            aMap.addMarker(new MarkerOptions().anchor(1.5f, 3.5f)
                    .position(new LatLng(marketList.get(i).getLatitude(),//设置纬度
                            marketList.get(i).getLongitude()))//设置经度
                    .title(marketList.get(i).getTitle())//设置标题
                    .draggable(false) //设置Marker可拖动
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
        }
    }


    /************************************* 线程 *******************************************/

    private void getDeviceLocationThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request= new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_User_GetAllDeviceLocationByUser))
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    String getResult = parseObject(result).get("data").toString();
                    Log.e(TAG, getResult, null);

                    if(getResult.equals("[]")){
                        handler.sendEmptyMessage(GET_FAILED);
                        return;
                    }else {
                        List<Map> list= parseArray(getResult, Map.class);
                        marketList = new ArrayList<>();
                        for(int i=0; i<list.size(); i++){
                            Map map = list.get(i);
                            if (map.get("value").toString().equals("[]")) {
                                continue;
                            }
                            List<Double> local = parseArray(map.get("value").toString(), Double.class);
                            marketList.add(new MarketBean(local.get(1), local.get(0), map.get("name").toString()));
                        }
                        handler.sendEmptyMessage(GET_SUCCESS);
                    }

                }catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }


    /************************************** 初始化 *******************************************/


    /**
     * 初始化地图
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，实现地图生命周期管理
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
            //设置显示定位按钮 并且可以点击
//            UiSettings settings = aMap.getUiSettings();
            // 是否显示定位按钮
//            settings.setMyLocationButtonEnabled(true);
            aMap.setMyLocationEnabled(false);//显示定位层并且可以触发定位,默认是flase
        }
    }

    /**
     * 初始化控件
     * @param view
     */
    private void initView(View view) {
        mapView = view.findViewById(R.id.map);
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getDeviceLocationThread();
    }

    /**
     * 初始化handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what){
                    case GET_SUCCESS:
                        addMoreMarket();
                        Log.e(TAG, "获取成功",null);
                        break;
                    case GET_FAILED:
                        ToastUtils.showShort("没有在线设备");
                        Log.e(TAG, "获取",null);
                        break;
                    case GET_ERROR:
                        Log.e(TAG, "获取失败",null);
                        break;
                }
                return false;
            }
        });
    }

    /************************************* 生命周期 ****************************************/
    @Override
    public void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        initThread();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mapView.onSaveInstanceState(outState);
    }


}
