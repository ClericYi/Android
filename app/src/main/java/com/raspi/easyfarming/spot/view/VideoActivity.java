package com.raspi.easyfarming.spot.view;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.esp.smartconfig.sweet.SuccessTickView;
import com.raspi.easyfarming.R;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;
import com.raspi.easyfarming.utils.widget.LandLayoutVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.alibaba.fastjson.JSON.toJSONString;

public class VideoActivity extends AppCompatActivity {

    //常量
    private final String TAG = "VideoActivity";
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final int GET_SUCCESS = 0;
    private final int GET_FAIL = 1;
    private final int GET_ERROR = 2;
    private final int START_SUCCESS = 3;
    private final int START_FAIL = 4;
    private final int START_ERROR = 5;
    private final int RTMP_SUCCESS = 6;
    private final int RTMP_FAIL = 7;
    private final int RTMP_ERROR = 8;
    private final int STOP_SUCCESS = 9;
    private final int STOP_FAIL = 10;
    private final int STOP_ERROR = 11;

    //控件
    @BindView(R.id.video_nested_scroll)
    NestedScrollView postDetailNestedScroll;

    @BindView(R.id.video_llv)
    LandLayoutVideo detailPlayer;

    private ProgressDialog dialog;

    //数据
    private boolean isPlay;
    private boolean isPause;
    private String url;
    private String id;

    private OrientationUtils orientationUtils;

    private GSYVideoOptionBuilder gsyVideoOptionBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        ButterKnife.bind(this);
        initObject();//初始化数据
        initThread();//初始化线程
        initClick();//初始化点击事件
    }




    /******************************** 线程 *******************************************/

    /**
     * 关闭设备视频
     */
    private void stopVideoThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("stop", url);

                    Map<String, String> map = new HashMap<>();
                    map.put("deviceId", id);
                    map.put("payload", toJSONString(payload));

                    RequestBody requestBody = RequestBody.create(JSON, toJSONString(map));

                    Request request = new Request.Builder()
                            .post(requestBody)
                            .url(getResources().getString(R.string.URL_Video_Start))
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String state = parseObject(result).get("state").toString();

                    if(state.equals("1")){
                        handler.sendEmptyMessage(STOP_SUCCESS);
                    }else {
                        handler.sendEmptyMessage(STOP_FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(STOP_ERROR);
                }
            }
        }).start();
    }


    /**
     * 开启设备视频
     */
    private void startVideoThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> payload = new HashMap<>();
                    payload.put("start", url);

                    Map<String, String> map = new HashMap<>();
                    map.put("deviceId", id);
                    map.put("payload", toJSONString(payload));

                    RequestBody requestBody = RequestBody.create(JSON, toJSONString(map));

                    Request request = new Request.Builder()
                            .post(requestBody)
                            .url(getResources().getString(R.string.URL_Video_Start))
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String state = parseObject(result).get("state").toString();

                    if(state.equals("1")){
                        handler.sendEmptyMessage(START_SUCCESS);
                    }else {
                        handler.sendEmptyMessage(START_FAIL);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.sendEmptyMessage(START_ERROR);
                }
            }
        }).start();
    }


    /**
     * 获取M3U8格式
     */
    private void getM3u8UrlThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request = new Request.Builder()
                            .url(getResources().getString(R.string.URL_Video_M3u8)+id)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String state = parseObject(result).get("state").toString();

                    if(state.equals("1")){
                        url = parseObject(parseObject(result).get("data").toString()).get("liveUrl").toString();
                        handler.sendEmptyMessage(GET_SUCCESS);
                    }else {
                        handler.sendEmptyMessage(GET_FAIL);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(GET_ERROR);
                }
            }
        }).start();
    }

    /**
     * 获取RTMP视频
     */
    private void getRtmpUrlThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request = new Request.Builder()
                            .url(getResources().getString(R.string.URL_Video_Rtmp)+id)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String state = parseObject(result).get("state").toString();

                    if(state.equals("1")){
                        url = parseObject(parseObject(result).get("data").toString()).get("pushLiveUrl").toString();
                        handler.sendEmptyMessage(RTMP_SUCCESS);
                    }else {
                        handler.sendEmptyMessage(RTMP_FAIL);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(RTMP_ERROR);
                }
            }
        }).start();
    }




    /******************************** 初始化  ****************************************/


    /**
     * 初始化点击事件
     */
    private void initClick() {
        detailPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GSYVideoManager.backFromWindowFull(getBaseContext())) {
                    orientationUtils.backToProtVideo();
                    return;
                }else{
                    finish();
                }
            }
        });
    }


    /**
     * 初始化数据
     */
    private void initObject() {
        Intent intent = getIntent();
        if(intent!=null&& intent.hasExtra("id")){
            id = intent.getStringExtra("id");
        }
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getRtmpUrlThread();
    }


    /**
     * 初始化视频播放
     */
    private void initVideo() {
        resolveNormalVideoUI();
        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        gsyVideoOptionBuilder = new GSYVideoOptionBuilder()
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekRatio(1)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle("视频监控")
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        orientationUtils.setEnable(true);
                        isPlay = true;
                    }

                    @Override
                    public void onQuitFullscreen(String url, Object... objects) {
                        super.onQuitFullscreen(url, objects);
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                    }
                });
        gsyVideoOptionBuilder.build(detailPlayer);

        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();
                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                detailPlayer.startWindowFullscreen(VideoActivity.this, true, true);
            }
        });

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        stopVideoThread();
        getCurPlay().onVideoPause();
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        startVideoThread();
        getCurPlay().onVideoResume();
        super.onResume();
        isPause = false;
        initNetBoardcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getCurPlay().release();
        }
        stopVideoThread();
        //GSYPreViewManager.instance().releaseMediaPlayer();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }

    private GSYVideoPlayer getCurPlay() {
        if (detailPlayer.getFullWindowPlayer() != null) {
            return  detailPlayer.getFullWindowPlayer();
        }
        return detailPlayer;
    }

    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case GET_SUCCESS:
                    initVideo();//初始化视频
                    Log.e(TAG, "GET_SUCCESS", null);
                    break;
                case GET_FAIL:
                    dialog.dismiss();
                    Log.e(TAG, "GET_FAIL", null);
                    break;
                case GET_ERROR:
                    Log.e(TAG, "GET_ERROR", null);
                    break;
                case START_ERROR:
                    Log.e(TAG, "START_ERROR", null);
                    break;
                case START_FAIL:
                    Log.e(TAG, "START_FAIL", null);
                    break;
                case START_SUCCESS:
                    getM3u8UrlThread();
                    Log.e(TAG, "START_SUCCESS", null);
                    break;
                case RTMP_ERROR:
                    Log.e(TAG, "RTMP_ERROR", null);
                    break;
                case RTMP_FAIL:
                    Log.e(TAG, "RTMP_FAIL", null);
                    break;
                case RTMP_SUCCESS:
                    startVideoThread();
                    Log.e(TAG, "RTMP_SUCCESS", null);
                    break;
                case STOP_SUCCESS:
                    Log.e(TAG, "STOP_SUCCESS", null);
                    break;
                case STOP_FAIL:
                    Log.e(TAG, "STOP_FAIL", null);
                    break;
                case STOP_ERROR:
                    Log.e(TAG, "STOP_ERROR", null);
                    break;
            }
            return false;
        }
    });

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

}
