package com.raspi.easyfarming.device.view;

import android.content.DialogInterface;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.device.adapter.ListAdapter;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import org.angmarch.views.NiceSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;


public class DeviceFrag extends Fragment {

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


    public static final okhttp3.MediaType JSON
            = okhttp3.MediaType.parse("application/json; charset=UTF-8");
    private int PAGE = 0;
    private int SIZE = 10;

    //控件
    private NiceSpinner groupSpinner;
    private RecyclerView device_rv;
    private TextView device_edit;
    private TextView device_add;
    private TextView device_delete;


    //数据
    private List<Map> devices;
    private List<String> groups;
    private List<String> groupName;
    private List<String> groupNum;
    private boolean isShow = false;


    //适配器
    private ListAdapter listAdapter;
    private ArrayAdapter<String> groupAdapter;

    //Handler
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.from(getContext()).inflate(R.layout.frag_device_list, container, false);
        initView(view);//初始化控件
        initList();//初始化列表
        initHandler();//初始化Handler
        initOnClick();//初始化点击事件
        initRv();//为RecycleView增加下拉加载;
        return view;
    }

    /**
     * 获取所有设备线程
     */
    public void getAllDevicesThread(){
        PAGE = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Request request = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_Device_GetAllDevices)+PAGE+"/"+SIZE)
                            .build();

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

                    String url = getContext().getResources().getString(R.string.URL_Device_GetAllDevices)+ PAGE + "/"+SIZE;

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String getResult = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    Map map = new HashMap();

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
                    groupSpinner.setVisibility(View.GONE);
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int firstVisibleItem = manager.findFirstCompletelyVisibleItemPosition();
                    //所有条目,数量值
                    int totalItemCount = devices.size();
                    Log.e(TAG, "下拉加载", null);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount - 1)) {
                        //加载更多功能的代码
                        Log.e(TAG, "加载中", null);
                        getRestDevicesThread();
                    }
                    //判断是否为开启
                    if(firstVisibleItem == 0 && device_rv.canScrollVertically(1)){
                        groupSpinner.setVisibility(View.VISIBLE);
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
                if(i == 0){

                } else if(i < groups.size()-1 && i > 0) {

                } else {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View addview = inflater.inflate(R.layout.dialog_group_add, null);
                    //添加分组选项
                    new AlertDialog.Builder(getContext())
                            .setTitle("填写需要添加设备的信息")
                            .setView(addview)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    TextView addgroup_name = addview.findViewById(R.id.dialog_addgroup_name);
                                    TextView addgroup_comment = addview.findViewById(R.id.dialog_addgroup_comment);
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
        listAdapter = new ListAdapter(getContext(), devices);
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
        groupSpinner = (NiceSpinner) view.findViewById(R.id.frag_deivce_spinner);
        device_rv = (RecyclerView) view.findViewById(R.id.frag_deivce_rv);
        device_edit = (TextView) view.findViewById(R.id.frag_device_edit);
        device_add = (TextView) view.findViewById(R.id.frag_device_add);
        device_delete = (TextView) view.findViewById(R.id.frag_device_delete);
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
                        PAGE++;
                        listAdapter.notifyDataSetChanged();
                        Log.e(TAG, "获取所有设备成功", null);
                        break;
                    case GETALLDEVICE_FAIL:
                        listAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "不存在更多设备", Toast.LENGTH_SHORT).show();
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
}
