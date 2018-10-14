package com.raspi.easyfarming.login.view

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import com.alibaba.fastjson.JSON.parseObject
import com.alibaba.fastjson.JSON.toJSONString
import com.raspi.easyfarming.R
import com.raspi.easyfarming.main.view.MainActivity
import com.raspi.easyfarming.okHttpClientModel
import com.raspi.easyfarming.utils.network.NetBroadcastReceiver
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.item_login.view.*
import okhttp3.*
import java.util.ArrayList
import java.util.HashMap

class LoginActivity : AppCompatActivity() {

    //常量
    private val TAG = "LoginActivity"
    private val self = this
    private val JSON = MediaType.parse("application/json; charset=utf-8")
    private val LOGIN_SUCCESS = 1
    private val LOGIN_FAIL = 2
    private val LOGIN_ERROR = 3

    //广播
    private var netBroadcastReceiver:NetBroadcastReceiver?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()//对控件的样式等操作
        initClick()//初始化点击事件
    }

    /*              线程运行                */

    /**
     * 实现登陆线程
     */
    private fun startLoginThread() = Thread(Runnable {
        kotlin.run {
            try {
                Log.e("LoginThread", "进入LoginThread",null)
                //用于实现Cookie的保存
                okHttpClientModel.mOkHttpClient = OkHttpClient.Builder()
                        .cookieJar(object:CookieJar{
                            val cookieStore = HashMap<String, List<Cookie>>()

                            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                                cookieStore[url.host()] = cookies
                            }

                            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                                return cookieStore[url.host()]?: ArrayList()
                            }
                        })
                        .build()
                //开始数据传输
                var map = HashMap<String, String>()
                map.put("loginParam", login_username.item_login.text.toString())
                map.put("password", login_password.item_login.text.toString())
                val body = RequestBody.create(JSON, toJSONString(map))

                var requestBody = Request.Builder()
                        .url(resources.getString(R.string.URL_Login))
                        .post(body)
                        .build();
                //接受回传消息
                val response = okHttpClientModel.mOkHttpClient?.newCall(requestBody)?.execute()
                val result = response?.body()?.string()
                Log.e(TAG, result, null)

                val loginResult = parseObject(result).get("message").toString()


                if (loginResult.equals("登陆成功!")){
                    handler.sendEmptyMessage(LOGIN_SUCCESS)
                }else if(true){
                    handler.sendEmptyMessage(LOGIN_FAIL)
                }else{

                }
            }catch (e:Exception){
                handler.sendEmptyMessage(LOGIN_ERROR)
            }
        }
    }).start()



    /*              初始化事件               */

    /**
     * 初始化点击事件
     */
    private fun initClick() {
        login_button.setOnClickListener(View.OnClickListener { startLoginThread() })
    }

    /**
     * 对控件的样式等进行操作
     */
    private fun initView() {
        supportActionBar?.hide()
        login_username.item_login_text.text = "帐号"
        login_username.item_login.hint = "请输入账号"
        login_password.item_login_text.text = "密码"
        login_password.item_login.hint = "请输入密码"
        login_password.item_login.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    /**
     * 初始化Handler
     */
    private val handler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                LOGIN_SUCCESS -> {Log.e(TAG, "登陆成功", null)
                    val intent = Intent()
                    intent.setClass(self, MainActivity::class.java)
                    intent.putExtra("username",login_username.item_login.text)
                    startActivity(intent)
                }
                LOGIN_FAIL -> Log.e(TAG, "登陆失败", null)
                LOGIN_ERROR -> Log.e(TAG, "出现异常", null)
                else -> Log.e(TAG, "未知信息", null)
            }
        }
    }

    /**
     * 初始化网络广播
     */
    private fun initNetBoardcastReceiver() {
        //Log.e(TAG, "广播监听中", null)
        if (netBroadcastReceiver == null) {
            netBroadcastReceiver = NetBroadcastReceiver()
            netBroadcastReceiver?.setNetChangeListern(object : NetBroadcastReceiver.NetChangeListener {
                override fun onChangeListener(status: Boolean) {
                    if (status) {
                        /*startCompanyThread();*/
                    } else {
                        Toast.makeText(self, "无可用的网络，请连接网络", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
        val filter = IntentFilter()
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netBroadcastReceiver, filter)
    }

    /*              生命周期中的操作                 */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(netBroadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        initNetBoardcastReceiver()
    }
}