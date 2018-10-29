package com.raspi.easyfarming.spot.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.raspi.easyfarming.R;
import com.raspi.easyfarming.spot.adapter.SpotAdapter;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;

public class SpotFrag extends Fragment {

    //常量
    private final String TAG = "SpotFrag";
    private final int GET_SUCCESS = 1;
    private final int GET_FAIL = 2;
    private final int GET_ERROR = 3;
    private final int TEMP = 4;
    private final int HUMID = 5;
    private final int PRESSURE = 6;
    private final int ILLUMINANCE = 7;
    private final int RAIN = 8;

    //控件
    private RecyclerView recyclerView;

    //handler
    private Handler handler;

    //适配器
    private SpotAdapter spotAdapter;

    //数据
    private List<Map> listMap;
    private int PAGE = 0;
    private final int Size = 20;

    //mqtt
    private String[] RECENVETOPICFORMAT;
    private MqttAndroidClient mqttAndroidClient;
    String serverUri = "tcp://39.108.153.134:1883";
    String deviceId = "websocket_client";
    private boolean connectSuccess;
    private String[] point;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View  view = inflater.from(getContext()).inflate(R.layout.frag_spot_list, container, false);
        initView(view);//初始化控件
        initHandler();//初始化Handler
        initList();//初始化列表
        initThread();//初始化线程
        return view;
    }

    /******************************************  MQTT数据处理*********************************************************/

    /**
     * 初始化SDK
     *
     * @param context context
     */
    public void initSDK(Context context, final String[] topics) {
        mqttAndroidClient = new MqttAndroidClient(context.getApplicationContext(), serverUri, deviceId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.d(TAG, "Reconnected to : " + serverURI);
                } else {
                    Log.d(TAG, "Connected to: " + serverURI);

                }
                connectSuccess = true;
                // 订阅
                subscribeToTopic(topics);
            }

            @Override
            public void connectionLost(Throwable cause) {
                connectSuccess = false;
                Log.e(TAG, "The Connection was lost." + cause.getLocalizedMessage());
            }

            // THIS DOES NOT WORK!
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG, "Incoming message: " + topic + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }


    /**
     * 连接远程服务
     */
    public void connectServer() {
        MqttConnectOptions mqttConnectOptions = initMqttConnectionOptions(deviceId, deviceId);
        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    connectSuccess = true;
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e(TAG, "Failed to connect to: " + serverUri);
                    exception.printStackTrace();
                    Log.d(TAG, "onFailure: " + exception.getCause());
                    connectSuccess = false;
                }
            });

        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    // 初始化Mqtt连接选项
    private MqttConnectOptions initMqttConnectionOptions(String userName, String password) {
        MqttConnectOptions mOptions = new MqttConnectOptions();
        mOptions.setAutomaticReconnect(true);//断开后，是否自动连接
        mOptions.setCleanSession(true);//是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mOptions.setConnectionTimeout(100);//设置超时时间，单位为秒
        mOptions.setUserName(userName);//设置用户名。跟Client ID不同。用户名可以看做权限等级
        mOptions.setPassword(password.toCharArray());//设置登录密码
        mOptions.setKeepAliveInterval(200);//心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        mOptions.setMaxInflight(10);//允许同时发送几条消息（未收到broker确认信息）
        mOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);//选择MQTT版本
        return mOptions;
    }

    /**
     * 订阅mqtt消息
     */
    private void subscribeToTopic(String[] topics) {
        try {
            if (topics == null || topics.length == 0) {
                return;
            }
            int[] qoc = new int[topics.length];
            IMqttMessageListener[] mqttMessageListeners = new IMqttMessageListener[topics.length];
            for (int i = 0; i < topics.length; i++) {
                IMqttMessageListener mqttMessageListener = new IMqttMessageListener() {
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        // message Arrived!消息送达后做出的处理
                        Log.d(TAG, topic + " : " + new String(message.getPayload()));
                        handleReceivedMessage(new String(message.getPayload()), topic);
                    }
                };
                mqttMessageListeners[i] = mqttMessageListener;
                Log.d(TAG, "subscribeToTopic: qoc= " + qoc[i]);
            }
            mqttAndroidClient.subscribe(topics, qoc, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d(TAG, "Failed to subscribe");
                }
            }, mqttMessageListeners);

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }

    }

    // 处理消息
    private void handleReceivedMessage(final String message, final String gatewayId) {
        //可以发送一条广播通知程序
        Log.i(TAG, "topic"+gatewayId);
        Log.i(TAG, " receive : " + message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0; i<RECENVETOPICFORMAT.length; i++) {
                    if(gatewayId.equals(RECENVETOPICFORMAT[i])){
                        if(RECENVETOPICFORMAT[i].indexOf("TEMP")!=-1) {
                            listMap.get(i).put("value", message);
                            handler.sendEmptyMessage(TEMP);
                        }else if(RECENVETOPICFORMAT[i].indexOf("HUMID")!=-1) {
                            listMap.get(i).put("value", message);
                            handler.sendEmptyMessage(HUMID);
                        }else if(RECENVETOPICFORMAT[i].indexOf("RAIN")!=-1) {
                            listMap.get(i).put("value", message);
                            handler.sendEmptyMessage(RAIN);
                        }else if(RECENVETOPICFORMAT[i].indexOf("ILLUMINANCE")!=-1) {
                            listMap.get(i).put("value", message);
                            handler.sendEmptyMessage(ILLUMINANCE);
                        }else if(RECENVETOPICFORMAT[i].indexOf("PRESSURE")!=-1) {
                            listMap.get(i).put("value", message);
                            handler.sendEmptyMessage(PRESSURE);
                        }
                        break;
                    }
                }
            }
        }).start();
    }

    private void close() {
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient.disconnect();
                mqttAndroidClient.unregisterResources();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    /*********************** 线程 *********************************/

    /**
     * 获取在线设备线程
     */
    private void getAllOnlineDevicesByAppUserThread(){
        PAGE = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request  = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_User_getAllOnlineDevicesByAppUser)+PAGE+"/"+Size)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(data.equals("[]")){
                        handler.sendEmptyMessage(GET_FAIL);
                    }else{
                        int size = Integer.valueOf(parseObject(parseObject(result).get("data").toString()).get("totalElements").toString());
                        RECENVETOPICFORMAT = new String[size];
                        List<Map> list = parseArray(data, Map.class);
                        for(int i = 0; i<size; i++){
                            RECENVETOPICFORMAT[i] = list.get(i).get("topic").toString();
                        }
                        listMap.addAll(list);
                        handler.sendEmptyMessage(GET_SUCCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }

    /**
     * 获取在线设备
     */
    private void getRestOnlineDevicesByAppUserThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request  = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_User_getAllOnlineDevicesByAppUser)+PAGE+"/"+Size)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(data.equals("[]")){
                        handler.sendEmptyMessage(GET_FAIL);
                    }else{
                        listMap.addAll(parseArray(data, Map.class));
                        handler.sendEmptyMessage(GET_SUCCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }


    /************************ 初始化  ******************************/

    /**
     * 初始化线程
     */
    private void initThread() {
        getAllOnlineDevicesByAppUserThread();
    }

    /**
     * 初始化列表
     */
    private void initList() {
        listMap = new ArrayList<>();
        //使用适配器
        spotAdapter = new SpotAdapter(getContext(), listMap, true);
        //页面缓冲
        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.load_empty, (ViewGroup) recyclerView.getParent(), false);
        spotAdapter.setEmptyView(emptyView);
        spotAdapter.setLoadingView(R.layout.load_loading);
        spotAdapter.setLoadEndView(R.layout.load_end);

        recyclerView.setAdapter(spotAdapter);
        //设置样式
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * 初始化控件
     * @param view
     */
    private void initView(View view) {
        recyclerView = view.findViewById(R.id.frag_spot_rv);
    }

    /**
     * 初始化Handler
     */
    private void initHandler(){
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case GET_SUCCESS:
                        //初始化
                        Log.e("Spot", "Success", null);
                        initSDK(getContext(), RECENVETOPICFORMAT);
                        connectServer();
                        PAGE++;
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case GET_FAIL:
                        Log.e("Spot", "Get_FAIL", null);
                        break;
                    case TEMP:
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case HUMID:
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case PRESSURE:
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case ILLUMINANCE:
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case RAIN:
                        spotAdapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });
    }

    /***************** 生命周期*************************/
    @Override
    public void onDestroy() {
        close();
        super.onDestroy();
    }
}