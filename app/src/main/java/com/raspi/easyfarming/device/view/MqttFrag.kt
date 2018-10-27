package com.raspi.easyfarming.device.view

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import com.raspi.easyfarming.R
import com.raspi.easyfarming.device.adapter.MqttAdapter
import kotlinx.android.synthetic.main.frag_mqtt.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MqttFrag: Fragment(){

    //常量
    private val TAG = "MqttFrag"

    private val listMap = listOf<Map<Any, Any>>()

    //mqtt
    private val RECENVETOPICFORMAT = arrayOf("IN/DEVICE/1/1532428497255/1536823959779/TEMP/MIN", "IN/DEVICE/1/1532428497255/1537323247888/HUMID/MIN", "IN/DEVICE/1/1532428497255/1537665793526/PRESSURE/MIN", "IN/DEVICE/1/1532428497255/1537672450525/ILLUMINANCE/MIN", "IN/DEVICE/1/1532428497255/1537676466798/RAIN/MIN")
    private var mqttAndroidClient: MqttAndroidClient? = null
    internal val serverUri = "tcp://39.108.153.134:1883"
    internal val deviceId = "websocket_client"
    private var connectSuccess: Boolean = false
    private var point: Array<String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.frag_mqtt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()//初始化RecyclerView
        //初始化
        initSDK(context, RECENVETOPICFORMAT)
        connectServer()
    }



    /******************************************  MQTT数据处理*********************************************************/

    /**
     * 初始化SDK
     * @param context context
     */
    fun initSDK(context: Context?, topics: Array<String>) {
        mqttAndroidClient = MqttAndroidClient(context?.applicationContext, serverUri, deviceId)
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
                Log.e(TAG, "The Connection was lost." + cause.localizedMessage)
            }

            // THIS DOES NOT WORK!
            @Throws(Exception::class)
            override fun messageArrived(topic: String, message: MqttMessage) {
                Log.d(TAG, "Incoming message: " + topic + String(message.payload))
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
            mqttAndroidClient?.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    connectSuccess = true
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 100
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
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
        mOptions.isAutomaticReconnect = true//断开后，是否自动连接
        mOptions.isCleanSession = true//是否清空客户端的连接记录。若为true，则断开后，broker将自动清除该客户端连接信息
        mOptions.connectionTimeout = 100//设置超时时间，单位为秒
        mOptions.userName = userName//设置用户名。跟Client ID不同。用户名可以看做权限等级
        mOptions.password = password.toCharArray()//设置登录密码
        mOptions.keepAliveInterval = 200//心跳时间，单位为秒。即多长时间确认一次Client端是否在线
        mOptions.maxInflight = 10//允许同时发送几条消息（未收到broker确认信息）
        mOptions.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1_1//选择MQTT版本
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
                val mqttMessageListener = IMqttMessageListener { topic, message ->
                    // message Arrived!消息送达后做出的处理
                    Log.d(TAG, topic + " : " + String(message.payload))
                    handleReceivedMessage(String(message.payload), topic)
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
        Thread(Runnable {
            kotlin.run {
            for (i in RECENVETOPICFORMAT.indices) {
//                if (gatewayId.indexOf("TEMP") != -1 && i == 0) {
//                    point?.set(i, message)
//                    handler.sendEmptyMessage(TEMP)
//                } else if (gatewayId.indexOf("HUMID") != -1 && i == 1) {
//                    point?.set(i, message)
//                    handler.sendEmptyMessage(HUMID)
//                } else if (gatewayId.indexOf("PRESSURE") != -1 && i == 2) {
//                    point?.set(i, message)
//                    handler.sendEmptyMessage(PRESSURE)
//                } else if (gatewayId.indexOf("ILLUMINANCE") != -1 && i == 3) {
//                    point?.set(i, message)
//                    handler.sendEmptyMessage(ILLUMINANCE)
//                } else if (gatewayId.indexOf("RAIN") != -1 && i == 4) {
//                    point?.set(i, message)
//                    handler.sendEmptyMessage(RAIN)
//                }
            }

        } }).start()
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

    /**************************** 生命周期 ********************************/
    override fun onDestroy() {
        close()
        super.onDestroy()
    }

    /**************************** 初始化 **********************************/

    /**
     * 初始化RecyclerView
     */
    private fun initList() {
        val MqttAdapter = MqttAdapter(context,listMap,true)


        frag_mqtt_rv.adapter = MqttAdapter
        val gridLayoutManager = GridLayoutManager(context,2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL
        frag_mqtt_rv.layoutManager = gridLayoutManager
    }

}