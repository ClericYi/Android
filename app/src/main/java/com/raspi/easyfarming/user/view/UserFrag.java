package com.raspi.easyfarming.user.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.raspi.easyfarming.R;
import com.raspi.easyfarming.user.adapter.ListAdapter;
import com.raspi.easyfarming.login.view.LoginActivity;
import com.raspi.easyfarming.main.view.MainActivity;
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.alibaba.fastjson.JSON.parseObject;

public class UserFrag extends Fragment {

    //常量
    private static final String TAG = "UserFrag";
    private final static int[] icons = {
            R.drawable.ic_user_info,
            R.drawable.ic_user_texts,
            R.drawable.ic_user_triggers,
            R.drawable.ic_user_netconfig
    };

    private final static int[] texts = {
            R.string.personinfo,
            R.string.user_logs,
            R.string.trigger,
            R.string.netconfig
    };
    private final int QUIT_SUCCESS = 1;
    private final int QUIT_FAIL = 2;
    private final int QUIT_ERROR = 3;
    //handler
    private Handler handler;

    //控件
    private RecyclerView userList;
    private Button quit;
    private TextView username;


    //适配器
    private ListAdapter userListAdapter;

    //线程
    private Thread loginoutThread;

    //数据
    private String username_text;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.from(getContext()).inflate(R.layout.frag_user, container, false);
        initView(view);
        initHandler();//初始化Handler
        initUserList();//初始化功能列表
        initOnClick();//初始化点击事件
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        username_text = ((MainActivity)context).getUsername();
    }

    /**
     * 注销线程
     */
    public void startLoginOut(){
        loginoutThread  = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String url = getContext().getResources().getString(R.string.URL_LoginOut);

                    Request request = new Request.Builder()
                            .url(url)
                            .build();

                    Response response = okHttpClientModel.INSTANCE.getMOkHttpClient().newCall(request).execute();

                    String result = response.body().string();
                    Log.e(TAG, result, null);

                    String message = parseObject(result).get("message").toString();
                    if(!message.equals("注销成功!")) {
                        handler.sendEmptyMessage(QUIT_FAIL);
                        return;
                    }else {
                        handler.sendEmptyMessage(QUIT_SUCCESS);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    handler.sendEmptyMessage(QUIT_ERROR);
                }
            }
        });
        loginoutThread.start();
    }

    /**
     * 初始化点击事件
     */
    private void initOnClick() {
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLoginOut();
            }
        });
    }

    /**
     * 初始化功能列表
     */
    private void initUserList() {
        List<Map<String, Object>> listMaps = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < texts.length; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("text", texts[i]);
            map.put("icon", icons[i]);
            listMaps.add(map);
        }
        userListAdapter = new ListAdapter(listMaps,getContext());
        userList.setAdapter(userListAdapter);
        userList.setLayoutManager(new LinearLayoutManager(getContext()));
        userList.setHasFixedSize(true);
        userList.setItemAnimator(new DefaultItemAnimator());
        userList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    /**
     * 初始化Handler
     */
    private void initHandler() {
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case QUIT_SUCCESS:
                        Intent intent  = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                        break;
                    case QUIT_FAIL:
                        Toast.makeText(getContext(), "注销失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                    case QUIT_ERROR:
                        Toast.makeText(getContext(), "注销异常，请稍后再试", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    /**
     * 初始化控件
     * @param view
     */
    private void initView(View view) {
        userList = view.findViewById(R.id.frag_user_rv);
        quit = view.findViewById(R.id.frag_user_quit);
        username = view.findViewById(R.id.frag_user_username);
        username.setText("你好!"+username_text);
    }


}
