package com.raspi.easyfarming.user.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.blankj.utilcode.util.ToastUtils
import com.raspi.easyfarming.R
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel
import kotlinx.android.synthetic.main.activity_password.*
import kotlinx.android.synthetic.main.item_phone.view.*
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

class PasswordActivity: AppCompatActivity(){
    //常量
    private val TAG = "PhoneActivity"
    private val JSON = MediaType.parse("application/json; charset=utf-8")
    private val CHANGE_SUCCESS = 0
    private val CHANGE_FAILED = 1
    private val CHANGE_ERROR = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        initView()
        initClick()
    }



    /**
     * 修改密码线程
     */
    private fun changePasswordThread() = Thread(Runnable {
        kotlin.run {
            try {
                if(!password_input.phone_edit.text.toString().equals(password_retry_input.phone_edit.text.toString())){
                    handler.sendEmptyMessage(CHANGE_FAILED)
                }

                val map = hashMapOf<String, String>()
                map.put("newPassword", password_input.phone_edit.text.toString())
                map.put("newPasswordRetry", password_retry_input.phone_edit.text.toString())

                val requestBody = RequestBody.create(JSON, com.alibaba.fastjson.JSON.toJSONString(map))

                val request = Request.Builder()
                        .post(requestBody)
                        .url(resources.getString(R.string.URL_User_ChangePassword))
                        .build()

                val response = okHttpClientModel.mOkHttpClient?.newCall(request)?.execute()

                val result = response?.body()?.string()
                Log.e(TAG, result, null)

                val message = com.alibaba.fastjson.JSON.parseObject(result).get("state")!!.toString()
                if (message.equals("1")) {
                    handler.sendEmptyMessage(CHANGE_SUCCESS)
                } else {
                    handler.sendEmptyMessage(CHANGE_FAILED)
                    return@Runnable
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.sendEmptyMessage(CHANGE_ERROR)
            }
        }
    }).start()


    /**************************************** 初始化 ***************************************/

    /**
     * 初始化Handler
     */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                CHANGE_SUCCESS -> {
                    Log.e(TAG, "修改成功", null)
                    finish()
                }
                CHANGE_FAILED -> {
                    Log.e(TAG, "登陆失败，请检查您的账号和密码", null)
                    Toast.makeText(baseContext, "登陆失败，请检查您的账号和密码", Toast.LENGTH_SHORT).show()
                }
                CHANGE_ERROR -> {
                    Log.e(TAG, "出现未知异常", null)
                    Toast.makeText(baseContext, "出现未知异常", Toast.LENGTH_SHORT).show()
                }
                else -> Log.e(TAG, "未知信息", null)
            }
        }
    }


    /**
     * 初始化控件
     */
    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.user_password)
        password_input.phone_text.text = resources.getString(R.string.password)
        password_retry_input.phone_text.text = resources.getString(R.string.retry_password)
        password_retry_input.phone_edit.transformationMethod = PasswordTransformationMethod.getInstance()
        password_input.phone_edit.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    /**
     * 初始化点击事件
     */
    private fun initClick() {
        password_change.setOnClickListener(View.OnClickListener { changePasswordThread() })
    }

    /**
     * 初始化网络广播
     */
    private fun initNetBoardcastReceiver() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 请注意这里会有一个版本适配bug，所以请在这里添加非空判断
        connectivityManager?.requestNetwork(NetworkRequest.Builder().build(), object : ConnectivityManager.NetworkCallback() {
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

    /**
     * Home键的事件监听
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return false
    }

}