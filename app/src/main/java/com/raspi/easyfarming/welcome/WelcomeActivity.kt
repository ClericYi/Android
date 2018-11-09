package com.raspi.easyfarming.welcome

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import com.raspi.easyfarming.R
import com.raspi.easyfarming.login.view.LoginActivity
import kotlinx.android.synthetic.main.activity_welcome.*

class WelcomeActivity:AppCompatActivity(){

    //常量
    private val GOTO = 1
    private val DELAY_TIME:Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        initView()//初始化控件
        initAnim()//初始化动画
        NowDo()//任务执行
    }

    /**
     * 任务执行
     */
    private fun NowDo() {
        handler.sendEmptyMessageDelayed(GOTO, DELAY_TIME)
    }


    /**
     * 初始化Handler
     */
    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            when (msg?.what) {
                GOTO -> {
                    startActivity(Intent(baseContext, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }

    /**
     * 动画初始化
     */
    private fun initAnim() {
        val alphaAnimation = AlphaAnimation(0f, 1f)//透明度从0~1
        alphaAnimation.duration = 1000//持续时间
        AnimationUtils.loadAnimation(baseContext, R.anim.welcome)
        //设置动画
        welcome_text.startAnimation(alphaAnimation)
        logo.startAnimation(alphaAnimation)
    }

    /**
     * 初始化控件
     */
    private fun initView() {
        supportActionBar?.hide()
    }
}