package com.raspi.easyfarming.utils.mqtt;

/**
 * @author WangYh
 * @version V1.0
 * @Name: UpData
 * @Package wang.tinycoder.easyiottest
 * @Description: (用一句话描述该文件做什么)
 * @date 2018/8/29 0029
 */
public class UpData {

    private String data;

    public UpData() {
    }

    public UpData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
