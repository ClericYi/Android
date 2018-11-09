package com.raspi.easyfarming.device.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.device.model.EchartsLineBean;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseArray;
import static com.alibaba.fastjson.JSON.parseObject;

public class ChartFrag extends Fragment {

    //常量
    private String TAG = "ChartFrag";
    private final int GET_SUCCESS = 0;
    private final int GET_FAIL = 1;
    private final int GET_ERROR = 2;

    //控件
    private WebView webView;
    private ProgressDialog dialog;

    //数据
    private List<String> dates;
    private List<String> datas;
    EchartsLineBean echartsLineBean;

    //View
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.from(getContext()).inflate(R.layout.frag_chart, null);
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        initView(view);
        initThread();
        return view;
    }

    /************************************** 线程  ***************************************/

    private void getDataByday(){
        dialog = new ProgressDialog(getContext());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("加载中...");

        datas = new ArrayList<>();
        dates = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request  = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_Data_Day))
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();
                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    if(parseObject(result).get("state").toString().equals("1")){
                        List<Map> mapList = parseArray(parseObject(result).get("data").toString(), Map.class);
                        for(int i=0; i<mapList.size(); i++){
                            datas.add(mapList.get(i).get("data").toString());
                            dates.add(mapList.get(i).get("date").toString());
                        }
                        echartsLineBean = new EchartsLineBean();
                        echartsLineBean.setType("line");
                        echartsLineBean.setTitle("温度检测");
                        echartsLineBean.setSteps(datas);
                        echartsLineBean.setTimes(dates);
                        Log.e(TAG, dates.toString(), null);

                        handler.sendEmptyMessage(GET_SUCCESS);
                    }else{
                        handler.sendEmptyMessage(GET_FAIL);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }


    /************************************** 初始化 **************************************/

    /**
     * 初始化webView
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(){
        //进行webwiev的一堆设置
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/echart/myechart.html");
        Log.e(TAG, "WebView", null);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                dialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //最好在这里调用js代码 以免网页未加载完成
                webView.loadUrl("javascript:createChart('line'," + new Gson().toJson(echartsLineBean) + ");");
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getDataByday();
    }

    /**
     * 初始化控件
     * @param view
     */
    private void initView(View view) {
        webView = view.findViewById(R.id.frag_chart_webview);
    }

    /**
     * 初始化Handler
     */
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case GET_ERROR:
                    Log.e(TAG, "GET_ERROR",null);
                    break;
                case GET_SUCCESS:
                    initWebView();
                    Log.e(TAG, "GET_SUCCESS",null);
                    break;
                case GET_FAIL:
                    Log.e(TAG, "GET_FAIL",null);
                    break;


            }
            return false;
        }
    });

}
