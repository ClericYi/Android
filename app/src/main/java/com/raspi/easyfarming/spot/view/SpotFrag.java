package com.raspi.easyfarming.spot.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.interfaces.OnItemClickListener;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.spot.adapter.SpotAdapter;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import org.angmarch.views.NiceSpinner;
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
    private final int DATA_CHANGE = 4;
    private final int GETALLGROUP_FAIL = 5;
    private final int GETALLGROUP_SUCCESS = 6;
    private final int GETALLGROUP_ERROR = 7;
    private final int GET_EMPTY = 8;


    //控件
    private RecyclerView recyclerView;
    private NiceSpinner spinner;

    //handler
    private Handler handler;

    //适配器
    private SpotAdapter spotAdapter;
    private ArrayAdapter<String> groupAdapter;

    //数据
    private List<Map> listMap;
    private int PAGE = 0;
    private final int Size = 20;
    private List<String> groups;
    private List<String> groupName;
    private List<String> groupNum;

    //mqtt
    private String[] RECENVETOPICFORMAT;
    private MqttAndroidClient mqttAndroidClient;
    String serverUri = "tcp://39.108.153.134:1883";
    String deviceId = "websocket_client";
    private boolean connectSuccess;

    //View
    private View view;
    private int flag = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.from(getContext()).inflate(R.layout.frag_spot_list, null);
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        if (flag == 0) {
            initEach();//初始化所有
        }
        return view;
    }

    /**
     * 初始化所有
     */
    private void initEach() {
        initView(view);//初始化控件
        initHandler();//初始化Handler
        initList();//初始化列表
        initRv();//实现下拉加载
        flag = 1;
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
                Log.e(TAG, "The Connection was lost.");
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
        Log.i(TAG, "topic" + gatewayId);
        Log.i(TAG, " receive : " + message);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < RECENVETOPICFORMAT.length; i++) {
                    if (gatewayId.equals(RECENVETOPICFORMAT[i])) {
                        if (!listMap.get(i).get("value").equals(message)) {
                            if (RECENVETOPICFORMAT[i].substring(12).indexOf("TEMP") != -1) {
                                listMap.get(i).put("value", message);
                            } else if (RECENVETOPICFORMAT[i].substring(12).indexOf("HUMID") != -1) {
                                listMap.get(i).put("value", message);
                            } else if (RECENVETOPICFORMAT[i].substring(12).indexOf("RAIN") != -1) {
                                listMap.get(i).put("value", message);
                            } else if (RECENVETOPICFORMAT[i].substring(12).indexOf("ILL") != -1) {
                                listMap.get(i).put("value", message);
                            } else if (RECENVETOPICFORMAT[i].substring(12).indexOf("PRE") != -1) {
                                listMap.get(i).put("value", message);
                            }
                            handler.sendEmptyMessage(DATA_CHANGE);
                        }
                        return;
                    }
                }
            }
        }).start();
    }


    /*********************** 线程 *********************************/

    /**
     * 获取在线设备线程
     */
    private void getAllOnlineDevicesByAppUserThread() {
        PAGE = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = null;
                    if (spinner.getSelectedIndex() == 0) {
                        request = new Request.Builder()
                                .url(getContext().getResources().getString(R.string.URL_User_getAllOnlineDevicesByAppUser) + PAGE + "/" + Size)
                                .build();
                    } else {
                        request = new Request.Builder()
                                .url(getContext().getResources().getString(R.string.URL_User_getAllOnlineDevicesByAppUserByGroup) + groupNum.get(spinner.getSelectedIndex()- 1))
                                .build();
                    }

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String state = parseObject(result).get("state").toString();


                    if (!state.equals("1")) {
                        handler.sendEmptyMessage(GET_FAIL);
                    } else {
                        listMap.clear();
                        String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();
                        if (data.equals("[]")) {
                            handler.sendEmptyMessage(GET_EMPTY);
                            return;
                        }

                        List<Map> list = parseArray(data, Map.class);
                        RECENVETOPICFORMAT = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            RECENVETOPICFORMAT[i] = list.get(i).get("topic").toString();
                        }
                        listMap.addAll(list);
                        handler.sendEmptyMessage(GET_SUCCESS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }

    /**
     * 获取在线设备
     */
    private void getRestOnlineDevicesByAppUserThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_User_getAllOnlineDevicesByAppUser) + PAGE + "/" + Size)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String state = parseObject(result).get("state").toString();


                    if (!state.equals("1")) {
                        handler.sendEmptyMessage(GET_FAIL);
                    } else {
                        String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();
                        listMap.addAll(parseArray(data, Map.class));
                        handler.sendEmptyMessage(GET_SUCCESS);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }

    /**
     * 获得所有分组线程
     */
    public void getAllGroupThread() {
        groupName = new ArrayList<String>();
        groupNum = new ArrayList<String>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getContext().getResources().getString(R.string.URL_Device_GetAllGroups);

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    List<String> result_Groups = JSONArray.parseArray(parseObject(result).get("data").toString(), String.class);

                    if (result_Groups.size() < 1) {
                        handler.sendEmptyMessage(GETALLGROUP_FAIL);
                        return;
                    } else {
                        for (int i = 0; i < result_Groups.size(); i++) {
                            groupName.add(parseObject(result_Groups.get(i)).get("name").toString());
                            groupNum.add(parseObject(result_Groups.get(i)).get("id").toString());
                        }
                        handler.sendEmptyMessage(GETALLGROUP_SUCCESS);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLGROUP_ERROR);
                }
            }
        }).start();
    }

    /************************ 初始化  ******************************/

    /**
     * 初始化Spinner
     */
    private void initGroup() {
        groups = new ArrayList<>();
        groups.add("所有设备");
        if (groupName.size() > 0) {
            groups.addAll(groupName);
        }
        groupAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, groups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(groupAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getAllOnlineDevicesByAppUserThread();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * 为RcycleView增加下拉加载
     */
    private void initRv() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (spinner.getSelectedIndex() != 0) {
                    return;
                }
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    //所有条目,数量值
                    int totalItemCount = listMap.size();
                    Log.e(TAG, "下拉加载", null);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == totalItemCount) {
                        //加载更多功能的代码
                        Log.e(TAG, "加载中", null);
                        getRestOnlineDevicesByAppUserThread();
                    }
                }
            }
        });
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getAllGroupThread();
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

        spotAdapter.setOnItemClickListener(new OnItemClickListener<Map>() {
            @Override
            public void onItemClick(ViewHolder viewHolder, Map map, int i) {
                if (map.get("type").toString().equals("LIVE")) {
                    Intent intent = new Intent(getContext(), VideoActivity.class);
                    intent.putExtra("id", map.get("id").toString());
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapter(spotAdapter);
        //设置样式
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * 初始化控件
     *
     * @param view
     */
    private void initView(View view) {
        recyclerView = view.findViewById(R.id.frag_spot_rv);
        spinner = view.findViewById(R.id.frag_spot_spinner);
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                View reload = null;
                switch (msg.what) {
                    case GET_SUCCESS:
                        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                        initSDK(getContext(), RECENVETOPICFORMAT);
                        connectServer();
                        PAGE++;
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case GET_EMPTY:
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        spotAdapter.removeEmptyView();
                        spotAdapter.notifyDataSetChanged();
                        reload = LayoutInflater.from(getContext()).inflate(R.layout.load_reload, (ViewGroup) recyclerView.getParent(), false);
                        spotAdapter.setReloadView(reload);
                        reload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getAllOnlineDevicesByAppUserThread();
                            }
                        });
                        Log.e("Spot", "Get_Enpty", null);
                        break;
                    case GET_FAIL:
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        spotAdapter.notifyDataSetChanged();
                        reload = LayoutInflater.from(getContext()).inflate(R.layout.load_reload, (ViewGroup) recyclerView.getParent(), false);
                        spotAdapter.setReloadView(reload);
                        reload.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getAllOnlineDevicesByAppUserThread();
                            }
                        });
                        Log.e("Spot", "Get_FAIL", null);
                        break;
                    case DATA_CHANGE:
                        spotAdapter.notifyDataSetChanged();
                        break;
                    case GETALLGROUP_SUCCESS:
                        initGroup();
                        getAllOnlineDevicesByAppUserThread();
                        Log.e(TAG, "获取所有分组成功", null);
                        break;
                    case GETALLGROUP_FAIL:
                        initGroup();
                        getAllOnlineDevicesByAppUserThread();
                        Log.e(TAG, "获取所有分组失败", null);
                        break;
                    case GETALLGROUP_ERROR:
                        Log.e(TAG, "获取所有分组失败", null);
                        break;
                }
                return false;
            }
        });
    }

    /***************** 生命周期*************************/

    @Override
    public void onResume() {
        super.onResume();
        initThread();//初始化线程
    }
}
