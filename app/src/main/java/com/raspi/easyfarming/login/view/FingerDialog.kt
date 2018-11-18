package com.raspi.easyfarming.login.view

import android.graphics.Color
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.jia.jsfingerlib.FingerListener
import com.jia.jsfingerlib.JsFingerUtils
import com.raspi.easyfarming.R

class FingerDialog: DialogFragment(), FingerListener{

    //控件
    private var img: ImageView ?= null
    private var text: TextView ?= null

    //finger
    var jsFingerUtils: JsFingerUtils ?= null

    interface FingerInputListener {
        fun onCompleteFingerInput()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view= inflater.inflate(R.layout.frag_dialog_finger, container, false)
        initView(view)//初始化控件
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        initFinger()//初始化指纹识别
        return view
    }


    /**
     * 初始化指纹识别
     */
    private fun initFinger() {
        jsFingerUtils = JsFingerUtils(context)
        jsFingerUtils?.startListening(this)
    }

    /**
     * 初始化控件
     */
    private fun initView(view: View?) {
        img = view?.findViewById(R.id.frag_dialog_img)
        text = view?.findViewById(R.id.frag_dialog_text)
    }

    /************************ Finger ***************************************/
    override fun onSuccess(p0: FingerprintManager.AuthenticationResult?) {
        jsFingerUtils?.cancelListening()
        img?.background = context?.resources?.getDrawable(R.drawable.finger_true,null)
        text?.text = context?.resources?.getString(R.string.finger_true)
        text?.setTextColor(Color.rgb(0,210,0))
        (activity as FingerInputListener).onCompleteFingerInput();
    }

    override fun onStartListening() {

    }

    override fun onStopListening() {

    }

    override fun onFail(p0: Boolean, p1: String?) {
        img?.background = context?.resources?.getDrawable(R.drawable.finger_false,null)
        text?.text = context?.resources?.getString(R.string.finger_false)
        text?.setTextColor(Color.RED)
    }

    override fun onAuthenticationError(p0: Int, p1: CharSequence?) {
    }

    override fun onAuthenticationHelp(p0: Int, p1: CharSequence?) {
    }
}