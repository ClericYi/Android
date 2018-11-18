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
import com.othershe.baseadapter.interfaces.OnLoadMoreListener;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.user.adapter.LogsListAdapter;
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

    //控件
    @BindView(R.id.logs_rv)
    RecyclerView recyclerView;

    //适配器
    private LogsListAdapter logsListAdapter;

    //数据
    private List<Map> listMaps;
    private int getSize = 0;

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
                    getSize = result_devices.size();
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
     * 初始化线程
     */
    private void initThread(){
        getAllLogThread();
    }

    /**
     * 初始化handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case GETALLLOGS_SUCCESS:
                        logsListAdapter.removeEmptyView();
                        if(getSize < SIZE){
                            logsListAdapter.loadEnd();
                        }
                        PAGE++;
                        logsListAdapter.notifyDataSetChanged();
                        break;
                    case GETALLLOGS_FAIL:
                        logsListAdapter.removeEmptyView();
                        break;
                    case GETALLLOGS_ERROR:
                        logsListAdapter.removeEmptyView();
                        final View reloadLayout = LayoutInflater.from(getBaseContext()).inflate(R.layout.load_reload, (ViewGroup) recyclerView.getParent(), false);
                        reloadLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getAllLogThread();
                            }
                        });
                        logsListAdapter.setReloadView(reloadLayout);
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

        logsListAdapter = new LogsListAdapter(getBaseContext(), listMaps,true);
        //页面缓冲
        View emptyView = LayoutInflater.from(getBaseContext()).inflate(R.layout.load_empty, (ViewGroup) recyclerView.getParent(), false);
        logsListAdapter.setEmptyView(emptyView);
        logsListAdapter.setLoadingView(R.layout.load_loading);
        logsListAdapter.setLoadEndView(R.layout.load_loading);
        recyclerView.setAdapter(logsListAdapter);
        //设置加载更多触发的事件监听
        logsListAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(boolean isReload) {
                Log.e(TAG, "OnLoadMore",null);
            }
        });

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
                    if (lastVisibleItem == totalItemCount) {
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
                    initThread();//初始化线程
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
