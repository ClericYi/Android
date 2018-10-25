package com.raspi.easyfarming.onestackconfig.base;

import android.app.Application;

import com.raspi.easyfarming.onestackconfig.utils.PreferenceUtil;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		PreferenceUtil.init(this);
	}

}
