package com.ioyouyun.ui.chat.test_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ioyouyun.ui.chat.api.Observers.ExitListener;
import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui_chat_sdk_test_project.R;

public class Main extends Activity {
	
	private Button chatBtn;
	private Button notiSettingBtn;
	private Button blackBtn;
	private Button groupBtn;
	private Button logoutBtn;
	private TextView myInfoTv;
	private Button sendCustomMsgBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		chatBtn = (Button)findViewById(R.id.chatBtn);
		chatBtn.setOnClickListener(onClickListener);
		notiSettingBtn = (Button)findViewById(R.id.notiSettingBtn);
		notiSettingBtn.setOnClickListener(onClickListener);
		blackBtn = (Button)findViewById(R.id.blackBtn);
		blackBtn.setOnClickListener(onClickListener);
		groupBtn = (Button)findViewById(R.id.groupBtn);
		groupBtn.setOnClickListener(onClickListener);
		logoutBtn = (Button)findViewById(R.id.logoutBtn);
		logoutBtn.setOnClickListener(onClickListener);
		myInfoTv = (TextView)findViewById(R.id.myInfoTv);
		sendCustomMsgBtn = (Button)findViewById(R.id.sendCustomMsgBtn);
		sendCustomMsgBtn.setOnClickListener(onClickListener);
		setMyInfo();
	}
	
	/**
	 * 设置我的个人信息
	 */
	private void setMyInfo(){
		String myUid = WmOpenChatSdk.getInstance().getLoginUserId();
		if(myUid != null){
			myInfoTv.setText("my uid:"+myUid);
		}
	}
	
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v == chatBtn){
				Intent intent = new Intent(Main.this, Chat.class);
				startActivity(intent);
			} else if(v == notiSettingBtn){
				Intent intent = new Intent(Main.this, Noti.class);
				startActivity(intent);
			} else if(v == blackBtn){
				Intent intent = new Intent(Main.this, Black.class);
				startActivity(intent);
			} else if(v == groupBtn){
				Intent intent = new Intent(Main.this, Group.class);
				startActivity(intent);
			} else if(v == logoutBtn){
				logout();
			} else if(v == sendCustomMsgBtn){
				Intent intent = new Intent(Main.this, CustomMsg.class);
				startActivity(intent);
			}
		}
	};
	
	/**
	 * 退出登录
	 */
	private void logout(){
		WmOpenChatSdk.getInstance().exit(Main.this,new ExitListener() {
			
			@Override
			public void result(final boolean success, String msg) {
				runOnUiThread(new Runnable() {
					public void run() {
						if(success){
							finish();							
						} else{
							Toast.makeText(Main.this, R.string.logout_failed, Toast.LENGTH_SHORT).show();
						}
					}
				});
			}
		});
	}

}
