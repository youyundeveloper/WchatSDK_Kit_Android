package com.ioyouyun.ui.chat.test_project;

import com.ioyouyun.ui.chat.api.WmOpenChatSdk;


public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// SDK初始化
		WmOpenChatSdk.getInstance().init(this);
	}

}
