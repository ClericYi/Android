package com.raspi.easyfarming.user.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alibaba.fastjson.JSON.parseObject
import com.raspi.easyfarming.R
import com.raspi.easyfarming.login.view.LoginActivity
import com.raspi.easyfarming.main.view.MainActivity
import com.raspi.easyfarming.user.adapter.ListAdapter
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel
import kotlinx.android.synthetic.main.frag_user.*
import okhttp3.Request
import java.util.ArrayList
import java.util.HashMap

class UserFrag : Fragment() {

    //常量
    private val TAG = "UserFrag"
    private val QUIT_SUCCESS = 1
    private val QUIT_FAIL = 2
    private val QUIT_ERROR = 3
    private val icons = arrayListOf<Int>(R.drawable.ic_user_info, R.drawable.ic_user_texts, R.drawable.ic_user_triggers, R.drawable.ic_user_netconfig)
    private val texts = arrayListOf<Int>(R.string.personinfo, R.string.user_logs, R.string.trigger, R.string.netconfig)

    //适配器
    private var userListAdapter: ListAdapter? = null

    //数据
    private var username_text: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOnClick()//初始化点击事件
        initUserList()//初始化功能列表
        frag_user_username.text = username_text
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        username_text = (context as MainActivity).username
    }

    /**
     * 注销线程
     */
    private fun loginOutThread() = Thread(Runnable {
        kotlin.run {
            try {
                val url = context!!.resources.getString(R.string.URL_LoginOut)

                val request = Request.Builder()
                        .url(url)
                        .build()

                val response = okHttpClientModel.mOkHttpClient?.newCall(request)?.execute()

                val result = response?.body()?.string()
                Log.e(TAG, result, null)

                val message = parseObject(result).get("state")!!.toString()
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

    /**
     * 初始化点击事件
     */
    private fun initOnClick() {
        frag_user_quit.setOnClickListener(View.OnClickListener { loginOutThread() })
    }

    /**
     * 初始化功能列表
     */
    private fun initUserList() {
        val listMaps = ArrayList<Map<String, Any>>()
        for (i in texts.indices) {
            val map = HashMap<String, Any>()
            map["text"] = texts[i]
            map["icon"] = icons[i]
            listMaps.add(map)
        }
        userListAdapter = ListAdapter(listMaps, context)
        frag_user_rv.adapter = userListAdapter
        frag_user_rv.layoutManager = LinearLayoutManager(context)
        frag_user_rv.setHasFixedSize(true)
        frag_user_rv.itemAnimator = DefaultItemAnimator()
        frag_user_rv.addItemDecoration(DividerItemDecoration(context!!, DividerItemDecoration.VERTICAL))
    }

    /**
     * 初始化Handler
     */
      private val handler = Handler(Handler.Callback { msg ->
            when (msg.what) {
                QUIT_SUCCESS -> {
                    val intent = Intent(context, LoginActivity::class.java)
                    startActivity(intent)
                }
                QUIT_FAIL -> Toast.makeText(context, "注销失败，请稍后再试", Toast.LENGTH_SHORT).show()
                QUIT_ERROR -> Toast.makeText(context, "注销异常，请稍后再试", Toast.LENGTH_SHORT).show()
            }
            false
        })
}