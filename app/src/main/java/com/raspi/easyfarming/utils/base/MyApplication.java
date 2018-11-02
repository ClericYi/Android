package com.raspi.easyfarming.utils.base;

import android.app.Application;

import com.raspi.easyfarming.utils.PreferenceUtil;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceUtil.init(this);
	}

}
