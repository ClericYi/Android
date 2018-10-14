package com.raspi.easyfarming.main.view

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.raspi.easyfarming.R
import com.raspi.easyfarming.device.view.DeviceFragment
import com.raspi.easyfarming.utils.network.NetBroadcastReceiver
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MainActivity:AppCompatActivity(){

    //常量
    private val self = this
    private val TAG = "MainActivity"

    //数据
    var name:String ?= null

    //视图
    private val fragments = arrayListOf<Fragment>()

    //广播
    private var netBroadcastReceiver:NetBroadcastReceiver ?= null

    //mqtt
    private val point: Array<String>? = null
    private val RECENVETOPICFORMAT = arrayOf("WARNING/DEVICE/1/#")
    private var mqttAndroidClient: MqttAndroidClient? = null
    internal var serverUri = "tcp://39.108.153.134:1883"
    internal var deviceId = "websocket_client"
    private var connectSuccess: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()//初始化部分样式
        initPager()//初始化分页
        initBottomNav()//初始化底部按钮
        initObject()//初始化数据
        //初始化MQTT
//        initSDK(this, RECENVETOPICFORMAT)
//        connectServer()
    }

    /**
     * 初始化数据
     */
    private fun initObject() {
//        if(intent != null)
//            name = intent.getStringExtra("username")
    }

    /**
     * 初始化底部按钮
     */
    private fun initBottomNav() {
        activity_main_bnv.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            var position = 0
            Log.e(TAG, "BottomNavigation被点击了", null)
            when (menuItem.itemId) {
                R.id.main_bnv_device -> position = 0
                R.id.main_bnv_user -> position = 1
            }
            activity_main_vp.setCurrentItem(position)
            menuItem.isChecked = true
            false
        })
    }

    /**
     * 初始化分页
     */
    private fun initPager() {
        activity_main_vp.offscreenPageLimit = 3

        val deviceFragment = DeviceFragment()

        fragments.add(deviceFragment)
        //适配器
        activity_main_vp.setAdapter(object : FragmentStatePagerAdapter(supportFragmentManager) {

            override fun getItem(position: Int): Fragment {
                return fragments[position]
            }

            override fun getCount(): Int {
                return fragments.size
            }
        })

        //页面切换事件
        activity_main_vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            //图标Id
            var itemIds = intArrayOf(R.id.main_bnv_device, R.id.main_bnv_user)

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                activity_main_bnv.setSelectedItemId(itemIds[position])
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    /**
     * 初始化部分样式
     */
    private fun initView() {
        supportActionBar?.hide()
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

                        val deviceFragment = DeviceFragment(baseContext)
                        deviceFragment.startgetAllDevicesThread()

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

    override fun onDestroy() {
        close()
        super.onDestroy()
    }

    /******************************************  MQTT数据处理*********************************************************/

    /**
     * 初始化SDK
     *
     * @param context context
     */
    fun initSDK(context: Context, topics: Array<String>) {
        mqttAndroidClient = MqttAndroidClient(context.applicationContext, serverUri, deviceId)
        mqttAndroidClient?.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {

                if (reconnect) {
                    Log.d(TAG, "Reconnected to : $serverURI")
                } else {
                    Log.d(TAG, "Connected to: $serverURI")

                }
                connectSuccess = true
                // 订阅
                subscribeToTopic(topics)
            }

            override fun connectionLost(cause: Throwable) {
                connectSuccess = false
                Log.e(TAG, "The Connection was lost.")
            }

            // THIS DOES NOT WORK!
            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.d(TAG, "Incoming message: " + topic + String(message.getPayload()))
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {

            }
        })
    }


    /**
     * 连接远程服务
     */
    fun connectServer() {
        val mqttConnectOptions = initMqttConnectionOptions(deviceId, deviceId)
        try {
            mqttAndroidClient?.connect(mqttConnectOptions, null, object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    connectSuccess = true
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.setBufferEnabled(true)
                    disconnectedBufferOptions.setBufferSize(100)
                    disconnectedBufferOptions.setPersistBuffer(false)
                    disconnectedBufferOptions.setDeleteOldestMessages(false)
                    mqttAndroidClient?.setBufferOpts(disconnectedBufferOptions)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.e(TAG, "Failed to connect to: $serverUri")
                    exception.printStackTrace()
                    Log.d(TAG, "onFailure: " + exception.cause)
                    connectSuccess = false
                }
            })

        } catch (ex: MqttException) {
            ex.printStackTrace()
        }

    }

    // 初始化Mqtt连接选项
    private fun initMqttConnectionOptions(userName: String, password: String): MqttConnectOptions {
        val mOptions = MqttConnectOptions()
        mOptions.setAutomaticReconnect(true)//断开后，是否自动连接
        mOptions.setCleanSession(true)//是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mOptions.setConnectionTimeout(100)//设置超时时间，单位为秒
        mOptions.setUserName(userName)//设置用户名。跟Client ID不同。用户名可以看做权限等级
        mOptions.setPassword(password.toCharArray())//设置登录密码
        mOptions.setKeepAliveInterval(200)//心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        mOptions.setMaxInflight(10)//允许同时发送几条消息（未收到broker确认信息）
        mOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1)//选择MQTT版本
        return mOptions
    }

    /**
     * 订阅mqtt消息
     */
    private fun subscribeToTopic(topics: Array<String>?) {
        try {
            if (topics == null || topics.size == 0) {
                return
            }
            val qoc = IntArray(topics.size)
            val mqttMessageListeners = arrayOfNulls<IMqttMessageListener>(topics.size)
            for (i in topics.indices) {
                val mqttMessageListener = object : IMqttMessageListener {
                    @Throws(Exception::class)
                    override fun messageArrived(topic: String, message: MqttMessage) {
                        // message Arrived!消息送达后做出的处理
                        Log.d(TAG, topic + " : " + String(message.getPayload()))
                        handleReceivedMessage(String(message.getPayload()), topic)
                    }
                }
                mqttMessageListeners[i] = mqttMessageListener
                Log.d(TAG, "subscribeToTopic: qoc= " + qoc[i])
            }
            mqttAndroidClient?.subscribe(topics, qoc, null, object : IMqttActionListener {
                override fun onSuccess(iMqttToken: IMqttToken) {
                    Log.d(TAG, "Subscribed!")
                }

                override fun onFailure(iMqttToken: IMqttToken, throwable: Throwable) {
                    Log.d(TAG, "Failed to subscribe")
                }
            }, mqttMessageListeners)

        } catch (ex: MqttException) {
            System.err.println("Exception whilst subscribing")
            ex.printStackTrace()
        }
    }

    // 处理消息
    private fun handleReceivedMessage(message: String, gatewayId: String) {
        //可以发送一条广播通知程序
        Log.i(TAG, "topic$gatewayId")
        Log.i(TAG, " receive : $message")
        runOnUiThread {  }
    }

    private fun close() {
        if (mqttAndroidClient != null) {
            try {
                mqttAndroidClient?.disconnect()
                mqttAndroidClient?.unregisterResources()
            } catch (e: MqttException) {
                e.printStackTrace()
            }
        }
    }
}