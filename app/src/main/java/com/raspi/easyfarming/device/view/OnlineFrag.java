package com.raspi.easyfarming.device.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.raspi.easyfarming.R;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class OnlineFrag extends Fragment {

    //常量
    private final String TAG = "OnlineFrag";
    private final int GET_SUCCESS = 1;
    private final int GET_FAIL = 2;
    private final int GET_ERROR = 3;

    //控件
    private TextView falseNum;
    private TextView trueNum;
    private TextView falseText;
    private ConstraintLayout onlineTrue;
    private ConstraintLayout onlineFalse;

    //数据
    private String falseNumResult;
    private String trueNumResult;

    //动画
    private ScaleAnimation anim;

    //View
    private View view;
    private int flag = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(view == null) {
            view = inflater.from(getContext()).inflate(R.layout.frag_device_online, null);
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        if(flag == 0) {
            initEach();//初始化所有
        }
        return view;
    }

    /**
     * 初始化所有
     */
    private void initEach() {
        initView(view);
        initAnim();
        flag = 1;
    }


    /****************************  线程  ************************************/

    public void getCurrentStateThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Request request = new Request.Builder()
                            .url(getContext().getResources().getString(R.string.URL_Device_GetCurrentState))
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    if(parseObject(result).get("state").toString().equals("1")){
                        falseNumResult = parseObject(parseObject(result).get("data").toString()).get("offLine").toString();
                        trueNumResult = parseObject(parseObject(result).get("data").toString()).get("onLine").toString();
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



    /*******************  初始化  ******************************/


    /**
     * 动画初始化
     */
    private void initAnim() {
        anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(1000);
        AnimationUtils.loadAnimation(getContext(),R.anim.online_scale);
        onlineTrue.startAnimation(anim);
        onlineFalse.startAnimation(anim);
    }

    /**
     * 初始化线程
     */
    private void initThread() {
        getCurrentStateThread();
    }

    /**
     * 控件初始化
     * @param view
     */
    private void initView(View view) {
        falseNum = view.findViewById(R.id.online_false).findViewById(R.id.online_num);
        trueNum = view.findViewById(R.id.online_true).findViewById(R.id.online_num);
        falseText = view.findViewById(R.id.online_false).findViewById(R.id.online_text);
        onlineFalse = view.findViewById(R.id.online_false);
        onlineTrue = view.findViewById(R.id.online_true);
        falseText.setText(getContext().getResources().getString(R.string.device_outline));
        view.findViewById(R.id.online_true).setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.ic_device_online_true));
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case GET_SUCCESS:
                    falseNum.setText(falseNumResult);
                    trueNum.setText(trueNumResult);
                    break;
                case GET_FAIL:
                    Log.e(TAG, "GET_FAIL", null);
                    break;
                case GET_ERROR:
                    Log.e(TAG, "GET_ERROR", null);
                    break;
            }
            return false;
        }
    });

    @Override
    public void onResume() {
        super.onResume();
        initThread();//初始化线程
    }
}
