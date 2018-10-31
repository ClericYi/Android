package com.raspi.easyfarming.spot.view;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import android.widget.RelativeLayout;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class VideoActivity extends AppCompatActivity {

    //常量
    private final String TAG = "VideoActivity";
    private final int GET_SUCCESS = 0;
    private final int GET_FAIL = 1;
    private final int GET_ERROR = 2;

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
        initDialog();//初始化弹窗
        initObject();//初始化数据
        initThread();//初始化线程
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


    /******************************** 线程 *******************************************/

    private void getUrlThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request = new Request.Builder()
                            .url(getResources().getString(R.string.URL_Device_getAllDevicesByGroup))
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String state = parseObject(result).get("state").toString();

                    if(state.equals("1")){
                        url = parseObject(parseObject(result).get("data").toString()).get("pushLiveUrl").toString();
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



    /******************************** 初始化  ****************************************/

    /**
     * 初始化弹窗
     */
    private void initDialog() {
        dialog = new ProgressDialog(getBaseContext());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("加载中...");
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getUrlThread();
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
                .setVideoTitle("测试视频")
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
        getCurPlay().onVideoPause();
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        getCurPlay().onVideoResume();
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getCurPlay().release();
        }
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
                    dialog.dismiss();
                    initVideo();//初始化视频
                    Log.e(TAG, "GET_SUCCESS", null);
                    break;
                case GET_FAIL:
                    dialog.dismiss();
                    Log.e(TAG, "GET_FAIL", null);
                    break;
                case GET_ERROR:
                    dialog.dismiss();
                    Log.e(TAG, "GET_ERROR", null);
                    break;
            }
            return false;
        }
    });
}
