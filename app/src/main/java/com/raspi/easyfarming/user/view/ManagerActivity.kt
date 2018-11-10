package com.raspi.easyfarming.user.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.othershe.baseadapter.interfaces.OnItemClickListener
import com.raspi.easyfarming.R
import com.raspi.easyfarming.login.view.LoginActivity
import com.raspi.easyfarming.user.adapter.ListAdapter
import com.raspi.easyfarming.utils.network.NetBroadcastReceiver
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel
import kotlinx.android.synthetic.main.activity_manager.*
import okhttp3.MediaType
import okhttp3.Request
import java.util.ArrayList
import java.util.HashMap

class ManagerActivity:AppCompatActivity(){
    //常量
    private val TAG = "ManagerActivity"
    private val self = this
    private val QUIT_SUCCESS = 1
    private val QUIT_FAIL = 2
    private val QUIT_ERROR = 3
    private val icons = arrayListOf<Int>(R.drawable.ic_password, R.drawable.ic_phone, R.drawable.ic_quit)
    private val texts = arrayListOf<Int>(R.string.user_password, R.string.user_phone, R.string.loginout)

    //广播
    private var netBroadcastReceiver: NetBroadcastReceiver?=null

    //适配器
    private var userListAdapter:ListAdapter?=null

    //数据
    private var email:String ?= null
    private var phone:String ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)
        initView()//初始化控件
        initList()//初始化列表
        initIntentObject()//初始化数据
    }

    private fun initIntentObject() {
        val intent = intent
        if (intent!=null&&intent.hasExtra("email"))
            email = intent.getStringExtra("email")
        if (intent!=null&&intent.hasExtra("phone"))
            phone = intent.getStringExtra("phone")
    }


    /**
     * 注销线程
     */
    private fun loginOutThread() = Thread(Runnable {
        kotlin.run {
            try {
                val url = resources.getString(R.string.URL_LoginOut)

                val request = Request.Builder()
                        .url(url)
                        .build()

                val response = okHttpClientModel.mOkHttpClient?.newCall(request)?.execute()

                val result = response?.body()?.string()
                Log.e(TAG, result, null)

                val message = com.alibaba.fastjson.JSON.parseObject(result).get("state")!!.toString()
                if (message.equals("1")) {
                    handler.sendEmptyMessage(QUIT_SUCCESS)
                } else {
                    handler.sendEmptyMessage(QUIT_FAIL)
                    return@Runnable
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.sendEmptyMessage(QUIT_ERROR)
            }
        }
    }).start()

    /************************************ 初始化 ****************************************/


    /**
     * 初始化控件
     */
    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.user_manager)
    }

    /**
     * 初始化列表
     */
    private fun initList() {
        val listMaps = ArrayList<Map<String, Int>>()
        for (i in texts.indices) {
            val map = HashMap<String, Int>()
            map["text"] = texts[i]
            map["icon"] = icons[i]
            listMaps.add(map)
        }
        userListAdapter = ListAdapter(baseContext, listMaps,true)
        userListAdapter?.setOnItemClickListener(OnItemClickListener { viewHolder, t, i ->
            when(i) {
                0 -> {
                    val intent = Intent()
                    intent.setClass(this, PasswordActivity::class.java)
                    startActivity(intent)
                }
                1 -> {
                    val intent = Intent()
                    intent.setClass(this, PhoneActivity::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("phone", phone)
                    startActivity(intent)
                }
                2 -> {
                    loginOutThread()
                }
                else -> {}
            } })
        manager_rv.adapter = userListAdapter
        manager_rv.layoutManager = LinearLayoutManager(baseContext)
        manager_rv.setHasFixedSize(true)
        manager_rv.itemAnimator = DefaultItemAnimator()
        manager_rv.addItemDecoration(DividerItemDecoration(baseContext, DividerItemDecoration.VERTICAL))
    }

    /**
     * 初始化Handler
     */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when(msg?.what){
                QUIT_SUCCESS -> {Log.e(TAG, "登陆成功", null)
                    val intent = Intent()
                    intent.setClass(self, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                QUIT_FAIL -> {
                    Log.e(TAG, "退出失败", null)
                }
                QUIT_ERROR -> {
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

    /***********************    生命周期中的操作    ****************************/

    override fun onPause() {
        super.onPause()
        unregisterReceiver(netBroadcastReceiver)
    }

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