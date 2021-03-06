package com.raspi.easyfarming.user.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.blankj.utilcode.util.ToastUtils;
import com.othershe.baseadapter.ViewHolder;
import com.othershe.baseadapter.interfaces.OnItemChildClickListener;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.user.adapter.TriggersAdapter;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class TriggersActivity extends AppCompatActivity {

    //常量
    private final TriggersActivity self = TriggersActivity.this;
    private final String TAG = "TriggerActivity";
    private int Page = 0;
    private int Size = 20;
    private final static int GETALLTRIGGERS_SUCCESS = 1;
    private final static int GETALLTRIGGERS_FAIL = 2;
    private final static int GETALLTRIGGERS_ERROR = 3;
    private final static int CHANGESTATE_SUCCESS = 4;
    private final static int CHANGESTATE_FAIL = 5;
    private final static int CHANGESTATE_ERROR = 6;

    //数据
    private List<Map> triggers;

    //适配器
    private TriggersAdapter triggersAdapter;

    //控件
    private RecyclerView trigger_rv;

    //Handler
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        initHanler();
        initView();//初始化控件
        initList();//初始化列表
        initRv();//RecycleView下拉加载
    }




    //添加触发器,弹出框
    private void addTriggerThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();
    }


    //获取触发器
    private void getAllTriggerThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getResources().getString(R.string.URL_User_GetAllTriggers)+ Page +"/"+ Size;

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(data.equals("[]")){
                        handler.sendEmptyMessage(GETALLTRIGGERS_FAIL);
                        return;
                    }
                    List<Map> map = JSONArray.parseArray(data, Map.class);
                    triggers.addAll(map);
                    handler.sendEmptyMessage(GETALLTRIGGERS_SUCCESS);

                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLTRIGGERS_ERROR);
                }
            }
        }).start();
    }

    //获取剩余触发器
    private void getRestTriggerThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getResources().getString(R.string.URL_User_GetAllTriggers)+ Page +"/"+ Size;

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();


                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(data.equals("[]")){
                        handler.sendEmptyMessage(GETALLTRIGGERS_FAIL);
                        return;
                    }else {
                        List<Map> map = JSONArray.parseArray(data, Map.class);
                        triggers.addAll(map);
                        handler.sendEmptyMessage(GETALLTRIGGERS_SUCCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLTRIGGERS_ERROR);
                }
            }
        }).start();
    }

    /**
     * 更改触发器状态
     * @param id
     */
    public void changeStatueThread(final String id){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String url = getResources().getString(R.string.URL_User_UpdateTriggerState)+id;
                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    if(parseObject(result).get("state").toString().equals("1")){
                        handler.sendEmptyMessage(CHANGESTATE_SUCCESS);
                    }else{
                        handler.sendEmptyMessage(CHANGESTATE_FAIL);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(CHANGESTATE_ERROR);
                }
            }
        }).start();
    }




    /************************* 生命周期 ***********************************/


    /**
     * 生命周期中的初始化过程
     */
    @Override
    protected void onResume() {
        super.onResume();
        initNetBoardcastReceiver();//初始化广播
    }



    /************************** 初始化 **********************************/
    /**
     * 初始化线程
     */
    private void initThread(){
        getAllTriggerThread();
    }

    /**
     * 为RcycleView增加下拉加载
     */
    private void initRv() {
        trigger_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) trigger_rv.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    //所有条目,数量值
                    int totalItemCount = triggers.size();
                    Log.e(TAG, "下拉加载", null);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == totalItemCount) {
                        //加载更多功能的代码
                        Log.e(TAG, "加载中", null);
                        getRestTriggerThread();
                    }else{
                        triggersAdapter.loadEnd();
                    }
                }
            }
        });
    }

    /**
     * 初始化Handler
     */
    private void initHanler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what){
                    case GETALLTRIGGERS_ERROR:
                        Toast.makeText(self, "获取错误", Toast.LENGTH_SHORT).show();
                        break;
                    case GETALLTRIGGERS_FAIL:
                        if(triggers.size()<0){
                            triggersAdapter.removeEmptyView();
                            View reloadLayout = LayoutInflater.from(getBaseContext()).inflate(R.layout.load_reload, (ViewGroup) trigger_rv.getParent(), false);
                            reloadLayout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    getAllTriggerThread();
                                }
                            });
                            triggersAdapter.setReloadView(reloadLayout);
                            Toast.makeText(getBaseContext(), "未寻找到您的触发器，点击按钮重试获取", Toast.LENGTH_SHORT).show();
                        }else{
                            triggersAdapter.loadEnd();
                        }
                        break;
                    case GETALLTRIGGERS_SUCCESS:
                        triggersAdapter.removeEmptyView();
                        if(triggers.size()%Size!=0){
                            triggersAdapter.loadEnd();
                        }
                        Page++;
                        triggersAdapter.notifyDataSetChanged();
                        break;
                    case CHANGESTATE_ERROR:
                        Log.e(TAG, "CHANGESTATE_ERROR", null);
                        break;
                    case CHANGESTATE_FAIL:
                        Log.e(TAG, "CHANGESTATE_FAIL", null);
                        Toast.makeText(self, "触发器状态更改失败", Toast.LENGTH_SHORT).show();
                        break;
                    case CHANGESTATE_SUCCESS:
                        Toast.makeText(self, "触发器状态更改成功",Toast.LENGTH_SHORT).show();
                        getAllTriggerThread();
                        break;

                }
                return false;
            }
        });
    }

    /**
     * 初始化列表
     */
    private void initList() {
        triggers = new ArrayList<>();
        //使用适配器
        triggersAdapter = new TriggersAdapter(getBaseContext(), triggers, true);

        View emptyView = LayoutInflater.from(this).inflate(R.layout.load_empty, (ViewGroup) trigger_rv.getParent(), false);
        triggersAdapter.setEmptyView(emptyView);
        triggersAdapter.setLoadingView(R.layout.load_loading);
        triggersAdapter.setLoadEndView(R.layout.load_end);
        triggersAdapter.setOnItemChildClickListener(R.id.item_trigger_switch, new OnItemChildClickListener<Map>() {
            @Override
            public void onItemChildClick(ViewHolder viewHolder, Map map, int i) {
                changeStatueThread(map.get("deviceScopeId").toString());
            }
        });
        trigger_rv.setAdapter(triggersAdapter);


        //设置样式
        trigger_rv.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        trigger_rv.setHasFixedSize(true);
        trigger_rv.setItemAnimator(new DefaultItemAnimator());
        //trigger_rv.addItemDecoration(new DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化控件
     */
    private void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        trigger_rv = (RecyclerView) findViewById(R.id.trigger_rv);
    }

    /**
     * 初始化网络广播
     */
    private void initNetBoardcastReceiver() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
        if (connectivityManager != null) {
            connectivityManager.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
                /**
                 * 网络可用的回调
                 */
                @Override
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    initThread();
                    Log.e(TAG, "onAvailable");
                }
                /**
                 * 网络丢失的回调
                 */
                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    ToastUtils.showShort("无可用的网络，请连接网络");
                }
            });
        }
    }

    /**
     * Home键的事件监听
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return false;
    }

}
