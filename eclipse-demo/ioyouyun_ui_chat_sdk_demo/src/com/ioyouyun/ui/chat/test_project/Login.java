package com.ioyouyun.ui.chat.test_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.ioyouyun.ui.chat.api.Observers.LoginListener;
import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui.chat.api.constant.LoginStatus;
import com.ioyouyun.ui_chat_sdk_test_project.R;

public class Login extends Activity {
	
	private EditText uniqueIdEt;
	private EditText clientIdEt;
	private EditText clientSecretEt;
	private RadioButton onlinePlatform;
	private Button loginBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		uniqueIdEt = (EditText) findViewById(R.id.uniqueIdEt);
		clientIdEt = (EditText) findViewById(R.id.clientIdEt);
		clientIdEt.setText("1-20046-8c351702e261c7f607ea8020dcb80f41-android");
		clientSecretEt = (EditText) findViewById(R.id.clientSecretEt);
		clientSecretEt.setText("db25eb5508619a34c0ce38b63b4b4c0a");
		onlinePlatform = (RadioButton) findViewById(R.id.onlinePlatform);
		loginBtn = (Button) findViewById(R.id.loginBtn);
		loginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				login();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(WmOpenChatSdk.getInstance().getLoginStatus() == LoginStatus.LOGIN_STATUS_ONLINE){
			loginBtn.setText(R.string.already_login);
		} else if(WmOpenChatSdk.getInstance().getLoginStatus() == LoginStatus.LOGIN_STATUS_ING){
			loginBtn.setText(R.string.login_ing);
		} else if(WmOpenChatSdk.getInstance().getLoginStatus() == LoginStatus.LOGIN_STATUS_OFFLINE){
			loginBtn.setText(R.string.login);
		}
	}

	/**
	 * 登录
	 */
	private void login() {
		if(WmOpenChatSdk.getInstance().getLoginStatus() == LoginStatus.LOGIN_STATUS_ONLINE){			//已经登录
			redirectToMain();
		} else if(WmOpenChatSdk.getInstance().getLoginStatus() == LoginStatus.LOGIN_STATUS_ING){		//正在登录
			
		} else if(WmOpenChatSdk.getInstance().getLoginStatus() == LoginStatus.LOGIN_STATUS_OFFLINE){	//未登录
			String uniqueId = uniqueIdEt.getText().toString().trim();
			String clientId = clientIdEt.getText().toString().trim();
			String clientSecret = clientSecretEt.getText().toString().trim();

			boolean isTestPlatFrom = false;
			if (onlinePlatform.isChecked()) {
				isTestPlatFrom = false;
			} else {
				isTestPlatFrom = true;
			}
			loginBtn.setText(R.string.login_ing);
			
			WmOpenChatSdk.getInstance().login(getApplicationContext(),
					uniqueId, clientId, clientSecret, isTestPlatFrom,
					new LoginListener() {

						@Override
						public void result(final boolean success,
								final String msg) {
							runOnUiThread(new Runnable() {
								public void run() {
									if (success) {			//登录成功
										loginBtn.setText(R.string.already_login);
										redirectToMain();
										setNotiTime();		//登录成功之后去设置提醒时段
										setPushNickName();	//登录成功之后去设置push昵称 
									} else{					//登录失败
										loginBtn.setText(R.string.login);
										Toast.makeText(Login.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
									}
								}
							});
						}
					}, 
					null);
			
		}
	}
	
	/**
	 * 跳转到主界面
	 */
	private void redirectToMain(){
		Intent intent = new Intent(Login.this, Main.class);
		startActivity(intent);
	}
	
	/**
	 * 设置提醒时段 这里设置为全部时段提醒
	 */
	private void setNotiTime(){
		WmOpenChatSdk.getInstance().setNotiTime(0, 24, null);
	}
	
	/**
	 * 设置push昵称 否则默认会采用id作为昵称
	 */
	private void setPushNickName(){
		WmOpenChatSdk.getInstance().setPushNickName(getString(R.string.test_user)+"("+WmOpenChatSdk.getInstance().getLoginUserId()+")", null);
	}

}
