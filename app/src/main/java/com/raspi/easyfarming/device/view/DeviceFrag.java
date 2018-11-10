package com.raspi.easyfarming.device.view;


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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.blankj.utilcode.util.ToastUtils;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.interfaces.OnItemChildClickListener;
import com.othershe.baseadapter.interfaces.OnItemClickListener;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.device.adapter.ListAdapter;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import org.angmarch.views.NiceSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;


public class DeviceFrag extends Fragment implements AMapLocationListener {

    //常量
    private final String TAG = "DeviceFrag";
    private final int GETALLDEVICE_SUCCESS = 0;
    private final int GETALLDEVICE_FAIL = 1;
    private final int GETALLDEVICE_ERROR = 2;
    private final int GETALLGROUP_SUCCESS = 3;
    private final int GETALLGROUP_FAIL = 4;
    private final int GETALLGROUP_ERROR = 5;
    private final int DELETEDEVICE_SUCCESS = 9;
    private final int DELETEDEVICE_FAIL = 10;
    private final int DELETEDEVICE_ERROR = 11;
    private final int ADDGROUP_SUCCESS = 12;
    private final int ADDGROUP_FAIL = 13;
    private final int ADDGROUP_ERROR = 14;
    private final int ADDDEVICE_SUCCESS = 15;
    private final int ADDDEVICE_FAIL = 16;
    private final int ADDDEVICE_ERROR = 17;
    private final int GETLOCAL_FAIL = 18;
    private final int SENDCMD_SUCCESS = 19;
    private final int SENDCMD_FAIL = 20;
    private final int SENDCMD_ERROR = 21;

    public static final okhttp3.MediaType JSON
            = okhttp3.MediaType.parse("application/json; charset=UTF-8");
    private int PAGE = 0;
    private int SIZE = 10;

    //控件
    private NiceSpinner groupSpinner;
    private RecyclerView device_rv;
    private TextView device_edit;
    private ImageView device_add;
    private TextView device_delete;


    //数据
    private List<Map> devices;
    private List<String> groups;
    private List<String> groupName;
    private List<String> groupNum;
    private boolean isShow = false;
    private String longitude="0";
    private String latitude="0";


    //适配器
    private ListAdapter listAdapter;
    private ArrayAdapter<String> groupAdapter;

    //Handler
    private Handler handler;

    //View
    private View view;
    private int flag = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.from(getContext()).inflate(R.layout.frag_device_list, null);
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
        initGPS();
        initView(view);//初始化控件
        initList();//初始化列表
        initHandler();//初始化Handler
        initOnClick();//初始化点击事件
        initRv();//为RecycleView增加下拉加载;
        flag = 1;
    }


    /**************************** 线程 ******************************/
    /**
     * 获取所有设备线程
     */
    public void getAllDevicesThread(){
        PAGE = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Request request = null;
                    if(groupSpinner.getSelectedIndex()==0) {
                        request = new Request.Builder()
                                .url(getContext().getResources().getString(R.string.URL_Device_GetAllDevices) + PAGE + "/" + SIZE)
                                .build();
                    }else{
                        request= new Request.Builder()
                                .url(getContext().getResources().getString(R.string.URL_Device_getAllDevicesByGroup)+groupNum.get(groupSpinner.getSelectedIndex())+"/" + PAGE + "/" + SIZE)
                                .build();
                    }

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String getResult = parseObject(parseObject(result).get("data").toString()).get("data").toString();
                    if(getResult.equals("[]")){
                        devices.clear();
                        handler.sendEmptyMessage(GETALLDEVICE_FAIL);
                        return;
                    }else {
                        devices.clear();
                        List<Map> result_devices = JSONArray.parseArray(getResult, Map.class);
                        devices.addAll(result_devices);
                        handler.sendEmptyMessage(GETALLDEVICE_SUCCESS);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLDEVICE_ERROR);
                }
            }
        }).start();
    }

    /**
     * 获取剩余未获取设备线程
     */
    public void getRestDevicesThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Request request = null;
                    if(groupSpinner.getSelectedIndex()==0) {
                        request = new Request.Builder()
                                .url(getContext().getResources().getString(R.string.URL_Device_GetAllDevices) + PAGE + "/" + SIZE)
                                .build();
                    }else{
                        request= new Request.Builder()
                                .url(getContext().getResources().getString(R.string.URL_Device_getAllDevicesByGroup)+groupNum.get(groupSpinner.getSelectedIndex())+"/" + PAGE + "/" + SIZE)
                                .build();
                    }

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String getResult = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(getResult.equals("[]")){
                        handler.sendEmptyMessage(GETALLDEVICE_FAIL);
                        return;
                    }else {
                        List<Map> result_devices = JSONArray.parseArray(getResult, Map.class);
                        devices.addAll(result_devices);
                        handler.sendEmptyMessage(GETALLDEVICE_SUCCESS);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLDEVICE_ERROR);
                }
            }
        }).start();
    }

    /**
     * 获得所有分组线程
     */
    public void getAllGroupThread(){
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

                    if(result_Groups.size()<1){
                        handler.sendEmptyMessage(GETALLGROUP_FAIL);
                        return;
                    }else {
                        for (int i = 0; i < result_Groups.size(); i++) {
                            groupName.add(parseObject(result_Groups.get(i)).get("name").toString());
                            groupNum.add(parseObject(result_Groups.get(i)).get("id").toString());
                        }
                        handler.sendEmptyMessage(GETALLGROUP_SUCCESS);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLGROUP_ERROR);
                }
            }
        }).start();
    }


    /**
     * 添加分组
     */
    private void addGroupThread(final String name, final String comment){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getContext().getResources().getString(R.string.URL_Device_AddGroup);

                    Map map = new HashMap();
                    map.put("groupName", name);
                    map.put("comment", comment);

                    Log.e(TAG, toJSONString(map),null);

                    RequestBody body = RequestBody.create(JSON, toJSONString(map));

                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();
                    String result = response.body().string();

                    String getResult = parseObject(result).get("state").toString();

                    if(!getResult.equals("1")){
                        handler.sendEmptyMessage(ADDGROUP_FAIL);
                        return;
                    }else {
                        Log.e(TAG, result, null);
                        handler.sendEmptyMessage(ADDGROUP_SUCCESS);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 添加设备
     * @param name
     * @param comment
     * @param groupId
     * @param local
     * @param type
     * @param locationText
     */
    private void createDeviceThread(final String name, final String comment, final String groupId, final String local, final String type, final String locationText){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(name.equals("")||comment.equals("")||local.equals("")||type.equals("")||groupId.equals("")){
                        handler.sendEmptyMessage(ADDDEVICE_FAIL);
                        return;
                    }
                    if(longitude.equals("0")||latitude.equals("0")){
                        handler.sendEmptyMessage(GETLOCAL_FAIL);
                        return;
                    }

                    Map map = new HashMap();
                    map.put("deviceDescribe", comment);
                    map.put("deviceName", name);
                    map.put("deviceType", type);
                    map.put("groupId", groupId);
                    map.put("locationDescribe", local);
                    map.put("latitude", latitude);
                    map.put("longitude", longitude);

                    Log.e(TAG, toJSONString(map),null);

                    RequestBody body = RequestBody.create(JSON, toJSONString(map));

                    Request request = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_Device_createDevice))
                            .post(body)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();
                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String getResult = parseObject(result).get("state").toString();

                    if(!getResult.equals("1")){
                        handler.sendEmptyMessage(ADDDEVICE_FAIL);
                        return;
                    }else {
                        Log.e(TAG, result, null);
                        handler.sendEmptyMessage(ADDDEVICE_SUCCESS);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(ADDDEVICE_ERROR);
                }
            }
        }).start();
    }

    /**
     * 删除设备线程
     */
    public void deleteDeviceThread(final List<String> deviceid){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < deviceid.size(); i++) {
                        String url = getContext().getResources().getString(R.string.URL_Device_DeleteDevice) + deviceid.get(i);

                        Request request = new Request.Builder()
                                .url(url)
                                .delete()
                                .build();

                        Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                        String result = response.body().string();
                        Log.e(TAG, result, null);
                        String getResult = parseObject(result).get("state").toString();

                        if(!getResult.equals("1")){
                            handler.sendEmptyMessage(DELETEDEVICE_FAIL);
                            return;
                        }
                    }
                    handler.sendEmptyMessage(DELETEDEVICE_SUCCESS);
                } catch(Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(DELETEDEVICE_ERROR);
                }

            }
        }).start();
    }


    /**
     * 发送指令
     */
    public void sendCmdToThread(final String data, final String deviceId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> payload = new HashMap<String, String>();
                    payload.put("cmd", data);
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("payload", toJSONString(payload));
                    map.put("deviceId", deviceId);

                    RequestBody body = RequestBody.create(JSON, toJSONString(map));


                    Request request = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_Device_SendCmdDevice))
                            .post(body)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String getResult = parseObject(result).get("state").toString();

                    if(!getResult.equals("1")){
                        handler.sendEmptyMessage(SENDCMD_FAIL);
                        return;
                    }
                    handler.sendEmptyMessage(SENDCMD_SUCCESS);
                } catch(Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(SENDCMD_ERROR);
                }
            }
        }).start();
    }


    /*****************************  初始化  *************************/

    /**
     * 初始化线程
     */
    private void initThread() {
        getAllDevicesThread();
        getAllGroupThread();
    }

    /**
     * 初始化点击事件
     */
    private void initOnClick() {

        //编辑设备
        device_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShow = !isShow;
                device_delete.setVisibility(isShow?View.VISIBLE:View.GONE);
                device_edit.setText(isShow?"取消":"编辑");
                device_add.setVisibility(isShow?View.GONE:View.VISIBLE);
                listAdapter.changCheckShow(isShow);
            }
        });

        //删除设备
        device_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> list = listAdapter.getCheckedId();
                if(list == null || list.size() == 0)
                    return;
                deleteDeviceThread(list);
            }
        });

        //添加设备
        device_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_device_add, null);
                final NiceSpinner spinner = view.findViewById(R.id.dialog_adddevice_group);
                initDialogSpinner(spinner);
                new AlertDialog.Builder(getContext())
                        .setTitle("添加设备")
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                TextView name = view.findViewById(R.id.dialog_adddevice_name);
                                TextView comment = view.findViewById(R.id.dialog_adddevice_comment);
                                TextView local = view.findViewById(R.id.dialog_adddevice_local);
                                TextView type = view.findViewById(R.id.dialog_adddevice_type);
                                createDeviceThread(name.getText().toString(),
                                        comment.getText().toString(),
                                        groupNum.get(spinner.getSelectedIndex()),
                                        local.getText().toString(),
                                        type.getText().toString(),
                                        "1"
                                );
                            }


                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });


    }

    /**
     * 初始化Spinner
     * @param spinner
     */
    public void initDialogSpinner(NiceSpinner spinner){
        groupAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groupName);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(groupAdapter);
    }


    /**
     * 为RcycleView增加下拉加载
     */
    private void initRv() {
        device_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) device_rv.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    //所有条目,数量值
                    int totalItemCount = devices.size();
                    Log.e(TAG, "下拉加载", null);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == totalItemCount) {
                        //加载更多功能的代码
                        Log.e(TAG, "加载中", null);
                        getRestDevicesThread();
                    }
                }
            }



        });
    }

    /**
     * 初始化Spinner
     */
    private void initGroup(){
        groups = new ArrayList<>();
        groups.add("所有设备");
        if(groupName.size()>0) {
            groups.addAll(groupName);
        }
        groups.add("添加分组");
        groupAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, groups);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(groupAdapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i < groups.size()-1){
                    getAllDevicesThread();
                } else {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View addView = inflater.inflate(R.layout.dialog_group_add, null);
                    //添加分组选项
                    new AlertDialog.Builder(getContext())
                            .setTitle("添加分组")
                            .setView(addView)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    TextView addgroup_name = addView.findViewById(R.id.dialog_addgroup_name);
                                    TextView addgroup_comment = addView.findViewById(R.id.dialog_addgroup_comment);
                                    addGroupThread(addgroup_name.getText().toString(), addgroup_comment.getText().toString());
                                }
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    /**
     * 初始化RecycleView
     */
    private void initList() {
        devices = new ArrayList<>();
        //使用适配器
        listAdapter = new ListAdapter(getContext(), devices, true);
        //页面缓冲
        View emptyView = LayoutInflater.from(getContext()).inflate(R.layout.load_empty, (ViewGroup) device_rv.getParent(), false);
        listAdapter.setEmptyView(emptyView);
        listAdapter.setLoadingView(R.layout.load_loading);
        listAdapter.setLoadEndView(R.layout.load_end);


        //适配器中每一项配置点击事件
        listAdapter.setOnItemClickListener(new OnItemClickListener<Map>() {
            @Override
            public void onItemClick(ViewHolder viewHolder, Map map, int i) {
                if(isShow){
                    ((CheckBox)viewHolder.getView(R.id.item_device_rb)).setChecked(!((CheckBox)viewHolder.getView(R.id.item_device_rb)).isChecked());
                    listAdapter.getCheckedId().add(map.get("id").toString());
                    Log.e("listAdapter", ""+listAdapter.getCheckedId().size(), null);
                }else{
                    Intent intent = new Intent(getContext(), DetailCenterAcitity.class);
                    intent.putExtra("id", map.get("id").toString());
                    intent.putExtra("name", map.get("name").toString());
                    getContext().startActivity(intent);
                }
            }
        });

        listAdapter.setOnItemChildClickListener(R.id.item_device_send, new OnItemChildClickListener<Map>() {
            @Override
            public void onItemChildClick(ViewHolder viewHolder, Map map, int i) {
                sendCmdToThread(((TextView)viewHolder.getView(R.id.item_device_ctrl)).getText().toString(),map.get("id").toString());
            }
        });

        device_rv.setAdapter(listAdapter);


        //设置样式
        device_rv.setLayoutManager(new LinearLayoutManager(getContext()));
        device_rv.setHasFixedSize(true);
        device_rv.setItemAnimator(new DefaultItemAnimator());
        device_rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化控件
     * @param view
     */
    private void initView(View view) {
        groupSpinner =  view.findViewById(R.id.frag_deivce_spinner);
        device_rv =  view.findViewById(R.id.frag_deivce_rv);
        device_edit =  view.findViewById(R.id.frag_device_edit);
        device_add =  view.findViewById(R.id.frag_device_add);
        device_delete =  view.findViewById(R.id.frag_device_delete);
    }

    /**
     * 初始化handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what){
                    case GETALLDEVICE_SUCCESS:
                        if(devices.size()%SIZE!=0){
                            listAdapter.loadEnd();
                        }
                        PAGE++;
                        listAdapter.notifyDataSetChanged();
                        Log.e(TAG, "获取所有设备成功", null);
                        break;
                    case GETALLDEVICE_FAIL:
                        if(devices.size()<1) {
                            listAdapter.removeEmptyView();
                            final View reloadLayout = LayoutInflater.from(getContext()).inflate(R.layout.load_reload, (ViewGroup) device_rv.getParent(), false);
                            reloadLayout.findViewById(R.id.load_reload_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getAllDevicesThread();
                                }
                            });
                            listAdapter.setReloadView(reloadLayout);
                            Toast.makeText(getContext(), "该群组没有设备了,点击按钮重试", Toast.LENGTH_SHORT).show();
                        }else{
                            listAdapter.loadEnd();
                        }
                        break;
                    case GETALLDEVICE_ERROR:
                        Log.e(TAG, "获取所有设备失败", null);
                        break;
                    case DELETEDEVICE_SUCCESS:
                        getAllDevicesThread();
                        Toast.makeText(getContext(), "设备删除成功", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "删除设备成功", null);
                        break;
                    case DELETEDEVICE_FAIL:
                        getAllDevicesThread();
                        listAdapter.getCheckedId().clear();
                        Toast.makeText(getContext(), "设备已被删除", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "设备已被删除", null);
                        break;
                    case DELETEDEVICE_ERROR:
                        Log.e(TAG, "删除设备失败", null);
                        break;
                    case ADDGROUP_SUCCESS:
                        getAllGroupThread();
                        Log.e(TAG, "添加分组成功", null);
                        break;
                    case ADDGROUP_FAIL:
                        Log.e(TAG, "添加分组失败", null);
                        break;
                    case ADDGROUP_ERROR:
                        Log.e(TAG, "添加分组失败", null);
                        break;
                    case GETALLGROUP_SUCCESS:
                        initGroup();
                        Log.e(TAG, "获取所有分组成功", null);
                        break;
                    case GETALLGROUP_FAIL:
                        initGroup();
                        break;
                    case GETALLGROUP_ERROR:
                        Toast.makeText(getContext(), "获取分组失败", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "获取所有分组失败", null);
                        break;
                    case ADDDEVICE_SUCCESS:
                        getAllDevicesThread();
                        Toast.makeText(getContext(), "设备添加成功", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "设备添加成功", null);
                        break;
                    case ADDDEVICE_FAIL:
                        Toast.makeText(getContext(), "设备添加失败", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "设备添加失败", null);
                        break;
                    case ADDDEVICE_ERROR:
                        Log.e(TAG, "设备添加失败", null);
                        break;
                    case GETLOCAL_FAIL:
                        ToastUtils.showShort("定位中或请检查您的定位是否开启");
                        break;
                    case SENDCMD_SUCCESS:
                        ToastUtils.showShort("指令发送成功");
                        Log.e(TAG, "发送成功", null);
                        break;
                    case SENDCMD_FAIL:
                        ToastUtils.showShort("指令发送失败");
                        Log.e(TAG, "发送失败", null);
                        break;
                    case SENDCMD_ERROR:
                        Log.e(TAG, "发送错误", null);
                        break;
                }
                return false;
            }
        });
    }

    /*********************** 生命周期 *************************/
    @Override
    public void onResume() {
        super.onResume();
        initThread();//初始化线程
    }



    /************************************ 定位******************************************/
    /**
     *   实现GPS的方法
     */
    public void initGPS() {
        //AmapGPS
        AMapLocationClient mlocationClient;
        AMapLocationClientOption mLocationOption = null;
        Log.e(TAG, "GPS", null);
        mlocationClient = new AMapLocationClient(getContext());
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(10000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mlocationClient.startLocation();
    }

    /**
     * 定位监听事件
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                latitude = String.format("%.2f",aMapLocation.getLatitude());//获取纬度
                longitude =  String.format("%.2f",aMapLocation.getLongitude());//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError","location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

}
