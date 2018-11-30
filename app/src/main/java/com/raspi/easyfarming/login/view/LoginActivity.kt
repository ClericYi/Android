package com.raspi.easyfarming.login.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.alibaba.fastjson.JSON.parseObject
import com.alibaba.fastjson.JSON.toJSONString
import com.blankj.utilcode.util.ToastUtils
import com.raspi.easyfarming.R
import com.raspi.easyfarming.main.view.MainActivity
import com.raspi.easyfarming.utils.MD5Utils
import com.raspi.easyfarming.utils.SharePreferenceUtil
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import okhttp3.*
import java.util.ArrayList
import java.util.HashMap
import com.jia.jsfingerlib.JsFingerUtils

class LoginActivity : AppCompatActivity(), FingerDialog.FingerInputListener {

    override fun onCompleteFingerInput() {
            fingerLoginThread()
    }


    //常量
    private val TAG = "LoginActivity"
    private val self = this
    private val JSON = MediaType.parse("application/json; charset=utf-8")
    private val LOGIN_SUCCESS = 1
    private val LOGIN_FAIL = 2
    private val LOGIN_ERROR = 3

    //sharePreference
    private var spUtil:SharePreferenceUtil?=null

    //数据
    private var email: String? = null
    private var phone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()//对控件的样式等操作
        initClick()//初始化点击事件
        initSharePreference()
    }




    /*************************              线程         ************************************/

    /**
     * 实现登陆线程
     */
    private fun startLoginThread() = Thread(Runnable {
        kotlin.run {
            try {
//                Log.e("LoginThread", "进入LoginThread",null)
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
                val map = HashMap<String, String>()
                map.put("loginParam", login_username.login_username.text.toString())
                map.put("password", MD5Utils.stringToMD5(login_password.login_password.text.toString()))
                val body = RequestBody.create(JSON, toJSONString(map))

                val requestBody = Request.Builder()
                        .url(resources.getString(R.string.URL_Login))
                        .post(body)
                        .build()
                //接受回传消息
                val response = okHttpClientModel.mOkHttpClient?.newCall(requestBody)?.execute()
                val result = response?.body()?.string()
                Log.e(TAG, result, null)

                val loginResult = parseObject(result).get("state").toString()


                if (loginResult.equals("1")){
                    val data =  parseObject(parseObject(result).get("data").toString())
                    email = data.get("email").toString()
                    phone = data.get("phone").toString()
                    handler.sendEmptyMessage(LOGIN_SUCCESS)
                }else{
                    handler.sendEmptyMessage(LOGIN_FAIL)
                }
            }catch (e:Exception){
                handler.sendEmptyMessage(LOGIN_ERROR)
            }
        }
    }).start()

    /**
     * 实现指纹登陆线程
     */
    private fun fingerLoginThread() = Thread(Runnable {
        kotlin.run {
            try {
//                Log.e("LoginThread", "进入LoginThread",null)
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
                val map = HashMap<String, String>()
                map.put("loginParam", spUtil?.getString("username").toString())
                map.put("password", MD5Utils.stringToMD5(spUtil?.getString("password")))
                val body = RequestBody.create(JSON, toJSONString(map))

                val requestBody = Request.Builder()
                        .url(resources.getString(R.string.URL_Login))
                        .post(body)
                        .build()
                //接受回传消息
                val response = okHttpClientModel.mOkHttpClient?.newCall(requestBody)?.execute()
                val result = response?.body()?.string()
                Log.e(TAG, result, null)

                val loginResult = parseObject(result).get("state").toString()


                if (loginResult.equals("1")){
                    val data =  parseObject(parseObject(result).get("data").toString())
                    email = data.get("email").toString()
                    phone = data.get("phone").toString()
                    handler.sendEmptyMessage(LOGIN_SUCCESS)
                }else{
                    handler.sendEmptyMessage(LOGIN_FAIL)
                }
            }catch (e:Exception){
                handler.sendEmptyMessage(LOGIN_ERROR)
            }
        }
    }).start()



    /*************************              初始化         ************************************/

    /**
     * 初始化指纹识别
     */
    private fun initFinger() {
        val jsFingerUtils = JsFingerUtils(baseContext)
        if (jsFingerUtils.checkSDKVersion()) {
            val fingerDialog = FingerDialog()
            fingerDialog.show(supportFragmentManager, "FingerDialog")
        }
    }

    /**
     * 初始化信息
     */
    private fun initSharePreference() {
        spUtil = SharePreferenceUtil(baseContext,"login")
        val isChecked = spUtil?.getBoolean("isChecked")!!
        if(isChecked) {
            login_remember.isChecked = true
            login_username.text = Editable.Factory.getInstance().newEditable(spUtil?.getString("username"))
            initFinger()
//            login_password.text = Editable.Factory.getInstance().newEditable(spUtil?.getString("password"))
//            startLoginThread()
        }
    }

    /**
     * 初始化点击事件
     */
    private fun initClick() {
        login_button.setOnClickListener{
            if((login_username.text.toString().equals("")||login_password.text.toString().equals(""))
                ||(login_username.text.toString().equals(null)||login_password.text.toString().equals(null)))
                Toast.makeText(baseContext,"请输入账号或密码", Toast.LENGTH_SHORT).show()
            else {
                login_button.isEnabled = false
                login_button.background = resources.getDrawable(R.drawable.ic_login_button_false,null)
                login_button.text = "登录中"
                startLoginThread()
            }
        }
    }

    /**
     * 对控件的样式等进行操作
     */
    private fun initView() {
        supportActionBar?.hide()
    }

    /**
     * 初始化Handler
     */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                LOGIN_SUCCESS -> {Log.e(TAG, "登陆成功", null)
                    if(login_remember.isChecked){
                        val Username = SharePreferenceUtil.ContentValue("username", login_username.text.toString())
                        val Password = SharePreferenceUtil.ContentValue("password", login_password.text.toString())
                        val isCheck = SharePreferenceUtil.ContentValue("isChecked", login_remember.isChecked)
                        spUtil?.putValues(Username, Password, isCheck)
                    }else{
                        spUtil?.clear()
                    }
                    val intent = Intent()
                    intent.setClass(self, MainActivity::class.java)
                    intent.putExtra("username",login_username.text.toString())
                    intent.putExtra("email", email)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                    finish()
                }
                LOGIN_FAIL -> {
                    login_button.isEnabled = true
                    login_button.background = resources.getDrawable(R.drawable.ic_login_button,null)
                    login_button.text = "登 录"
                    Log.e(TAG, "登陆失败，请检查您的账号和密码", null)
                    Toast.makeText(self, "登陆失败，请检查您的账号和密码", Toast.LENGTH_SHORT).show()
                }
                LOGIN_ERROR -> {
                    login_button.isEnabled = true
                    login_button.background = resources.getDrawable(R.drawable.ic_login_button,null)
                    login_button.text = "登 录"
                    Log.e(TAG, "出现未知异常", null)
                    Toast.makeText(self, "出现未知异常", Toast.LENGTH_SHORT).show()
                }
                else -> Log.e(TAG, "未知信息", null)
            }
        }
    }

    /**
     * 初始化网络广播
     */
    private fun initNetBoardcastReceiver() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
        connectivityManager.requestNetwork(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {
            /**
             * 网络可用的回调
             */
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.e(TAG, "onAvailable")
            }

            /**
             * 网络丢失的回调
             */
            override fun onLost(network: Network) {
                super.onLost(network)
                ToastUtils.showShort("无可用的网络，请连接网络")
            }
        })
    }

    /***********************    生命周期中的操作    ****************************/

    override fun onResume() {
        super.onResume()
        initNetBoardcastReceiver()
    }

    /*********************** 物理键重写  ***************************************/
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true)
            return false
        }
        return super.onKeyDown(keyCode, event)
    }


}