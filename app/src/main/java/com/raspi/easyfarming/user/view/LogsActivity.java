package com.raspi.easyfarming.user.view;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
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
import android.view.MenuItem;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.user.adapter.LogsListAdapter;
import com.raspi.easyfarming.utils.network.NetBroadcastReceiver;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class LogsActivity extends AppCompatActivity {

    //常量
    private String TAG = "LogsActivity";
    private LogsActivity self = this;
    private final int GETALLLOGS_SUCCESS = 1;
    private final int GETALLLOGS_FAIL = 2;
    private final int GETALLLOGS_ERROR = 3;
    private int PAGE = 0;
    private final int SIZE = 20;

    //广播
    private NetBroadcastReceiver netBroadcastReceiver;

    //控件
    @BindView(R.id.logs_rv)
    RecyclerView recyclerView;

    //适配器
    private LogsListAdapter logsListAdapter;

    //数据
    private List<Map> listMaps;

    //handler
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acticity_logs);
        ButterKnife.bind(self);
        initView();//初始化控件
        initHandler();//初始化handler
        initList();
        initRv();
    }

    /****************************  线程  **************************************/
    /**
     * 获取日志
     */
    private void getAllLogThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getResources().getString(R.string.URL_User_GetAllLog)+ PAGE+ "/" +SIZE;

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(data.equals("[]")){
                        handler.sendEmptyMessage(GETALLLOGS_FAIL);
                        return;
                    }
                    List<Map> result_devices = JSONArray.parseArray(data, Map.class);
                    listMaps.addAll(result_devices);
                    handler.sendEmptyMessage(GETALLLOGS_SUCCESS);
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GETALLLOGS_ERROR);
                }


            }
        }).start();
    }

    /**
     * 获取剩下未获取的日志
     */
    private void getRestLogThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = getResources().getString(R.string.URL_User_GetAllLog)+ PAGE+"/"+ SIZE;

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);
                    String data = parseObject(parseObject(result).get("data").toString()).get("data").toString();

                    if(data.equals("[]")){
                        handler.sendEmptyMessage(GETALLLOGS_FAIL);
                        return;
                    }
                    List<Map> result_devices = JSONArray.parseArray(data, Map.class);
                    listMaps.addAll(result_devices);
                    handler.sendEmptyMessage(GETALLLOGS_SUCCESS);
                }catch (Exception e){
                e.printStackTrace();
                handler.sendEmptyMessage(GETALLLOGS_ERROR);
            }


            }
        }).start();
    }

    /****************************  初始化  **************************************/

    /**
     * 初始化handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case GETALLLOGS_SUCCESS:
                        PAGE++;
                        logsListAdapter.notifyDataSetChanged();
                        break;
                    case GETALLLOGS_FAIL:
                        break;
                    case GETALLLOGS_ERROR:
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
        listMaps = new ArrayList<Map>();
        listMaps.clear();

        logsListAdapter = new LogsListAdapter(getBaseContext(), listMaps);
        recyclerView.setAdapter(logsListAdapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(), DividerItemDecoration.VERTICAL));
    }

    //初始化控件
    private void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * 为RcycleView增加下拉加载
     */
    private void initRv() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的ItemPosition ,角标值
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    //所有条目,数量值
                    int totalItemCount = listMaps.size();
                    Log.e(TAG, "下拉加载", null);
                    // 判断是否滚动到底部，并且是向右滚动
                    if (lastVisibleItem == (totalItemCount - 1)) {
                        //加载更多功能的代码
                        Log.e(TAG, "加载中", null);
                        getRestLogThread();
                    }
                }
            }

        });
    }


    /**
     * 生命周期中的初始化过程
     */
    @Override
    protected void onResume() {
        super.onResume();
        initNetBoardcastReceiver();//初始化广播
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(netBroadcastReceiver);//退出时注销广播
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
                        /*startCompanyThread();*/
                        getAllLogThread();

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
