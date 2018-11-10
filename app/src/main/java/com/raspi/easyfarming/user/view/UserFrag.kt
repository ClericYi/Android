package com.raspi.easyfarming.user.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alibaba.fastjson.JSON.parseObject
import com.othershe.baseadapter.interfaces.OnItemClickListener
import com.raspi.easyfarming.R
import com.raspi.easyfarming.login.view.LoginActivity
import com.raspi.easyfarming.main.view.MainActivity
import com.raspi.easyfarming.user.adapter.ListAdapter
import com.raspi.easyfarming.utils.okhttp.okHttpClientModel
import kotlinx.android.synthetic.main.frag_user.*
import kotlinx.android.synthetic.main.item_user.view.*
import okhttp3.Request
import java.util.ArrayList
import java.util.HashMap

class UserFrag : Fragment() {

    //常量
    private val TAG = "UserFrag"
    private val icons = arrayListOf<Int>(R.drawable.ic_user_texts, R.drawable.ic_user_triggers, R.drawable.ic_user_netconfig)
    private val texts = arrayListOf<Int>(R.string.user_logs, R.string.trigger, R.string.netconfig)

    //适配器
    private var userListAdapter: ListAdapter? = null

    //数据
    private var username_text: String? = null
    private var phone:String ?= null
    private var email:String ?= null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUserList()//初始化功能列表
        intUser()
        initClick()
    }

    private fun initClick() {
        frag_user.setOnClickListener(View.OnClickListener {
            val intent = Intent()
            intent.setClass(context, ManagerActivity::class.java)
            intent.putExtra("email", email)
            intent.putExtra("phone", phone)
            context?.startActivity(intent)
        })
    }

    private fun intUser() {
        frag_user.item_user_ic.background = context?.resources?.getDrawable(R.drawable.ic_user_head)
        frag_user.item_user_tv.text = "用户管理"
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        username_text = (context as MainActivity).username
        email = (context as MainActivity).email
        phone = (context as MainActivity).phone
    }


    /**
     * 初始化功能列表
     */
    private fun initUserList() {
        val listMaps = ArrayList<Map<String, Int>>()
        for (i in texts.indices) {
            val map = HashMap<String, Int>()
            map["text"] = texts[i]
            map["icon"] = icons[i]
            listMaps.add(map)
        }
        userListAdapter = ListAdapter(context, listMaps,true)
        userListAdapter?.setOnItemClickListener(OnItemClickListener { viewHolder, t, i ->
            when(i) {
                0 -> {
                    val intent = Intent(context, LogsActivity::class.java)
                    context?.startActivity(intent)
                }
                1 -> {
                    val intent = Intent(context, TriggersActivity::class.java)
                    context?.startActivity(intent)
                }
                2 -> {
                    val intent = Intent(context, WifiConnectActivity::class.java)
                    context?.startActivity(intent)
                }
                else -> {}
            } })
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

        }
        false
    })
}