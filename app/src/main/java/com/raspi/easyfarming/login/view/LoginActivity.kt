package com.raspi.easyfarming.login.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import com.raspi.easyfarming.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.item_login.view.*
import java.net.PasswordAuthentication

class LoginActivity : AppCompatActivity() {

    //常量
    private val  TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()//对控件的样式等操作
        initClick()//初始化点击事件
    }





    /**
     * 初始化点击事件
     */
    private fun initClick() {
        login_button.setOnClickListener(View.OnClickListener { Log.e(TAG, "点击事件触发",null) })
    }

    /**
     * 对控件的样式等进行操作
     */
    private fun initView() {
        supportActionBar?.hide()
        login_username.item_login_text.setText("帐号")
        login_username.item_login.setHint("请输入账号")
        login_password.item_login_text.setText("密码")
        login_password.item_login.setHint("请输入密码")
        login_password.item_login.transformationMethod = PasswordTransformationMethod.getInstance()
    }
}