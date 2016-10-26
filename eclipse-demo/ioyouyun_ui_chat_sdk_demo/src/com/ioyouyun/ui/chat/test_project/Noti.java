package com.ioyouyun.ui.chat.test_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.ioyouyun.ui.chat.api.Observers.HandleListener;
import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui.chat.api.bean.NotiTime;
import com.ioyouyun.ui_chat_sdk_test_project.R;

public class Noti extends Activity {
	
	private Button seeNotiTime;
	private EditText notiTimeEt1;
	private EditText notiTimeEt2;
	private Button setNotiTime;
	private Button closeNoti;
	private ToggleButton singleVibrateTbtn;
	private ToggleButton singleSoundTbtn;
	private ToggleButton groupVibrateTbtn;
	private ToggleButton groupSoundTbtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.noti);
		seeNotiTime = (Button)findViewById(R.id.seeNotiTime);
		seeNotiTime.setOnClickListener(onClick);
		notiTimeEt1 = (EditText)findViewById(R.id.notiTimeEt1);
		notiTimeEt2 = (EditText)findViewById(R.id.notiTimeEt2);
		setNotiTime = (Button)findViewById(R.id.setNotiTime);
		setNotiTime.setOnClickListener(onClick);
		closeNoti = (Button)findViewById(R.id.closeNoti);
		closeNoti.setOnClickListener(onClick);
		
		singleVibrateTbtn = (ToggleButton)findViewById(R.id.singleVibrateTbtn);
		singleVibrateTbtn.setChecked(WmOpenChatSdk.getInstance().isNotiVibrate(false));
		singleVibrateTbtn.setOnCheckedChangeListener(onCheckedChangeListener);
		
		singleSoundTbtn = (ToggleButton)findViewById(R.id.singleSoundTbtn);
		singleSoundTbtn.setChecked(WmOpenChatSdk.getInstance().isNotiSound(false));
		singleSoundTbtn.setOnCheckedChangeListener(onCheckedChangeListener);
		
		groupVibrateTbtn = (ToggleButton)findViewById(R.id.groupVibrateTbtn);
		groupVibrateTbtn.setChecked(WmOpenChatSdk.getInstance().isNotiVibrate(true));
		groupVibrateTbtn.setOnCheckedChangeListener(onCheckedChangeListener);
		
		groupSoundTbtn = (ToggleButton)findViewById(R.id.groupSoundTbtn);
		groupSoundTbtn.setChecked(WmOpenChatSdk.getInstance().isNotiSound(true));
		groupSoundTbtn.setOnCheckedChangeListener(onCheckedChangeListener);
	}
	
	OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if(buttonView == singleVibrateTbtn){
				WmOpenChatSdk.getInstance().setNotiVibrate(isChecked, false);
			} else if(buttonView == singleSoundTbtn){
				WmOpenChatSdk.getInstance().setNotiSound(isChecked, false);
			} else if(buttonView == groupVibrateTbtn){
				WmOpenChatSdk.getInstance().setNotiVibrate(isChecked, true);
			} else if(buttonView == groupSoundTbtn){
				WmOpenChatSdk.getInstance().setNotiSound(isChecked, true);
			}
		}
	};
	
	OnClickListener onClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(v == seeNotiTime){
				seeNotiTime();
			} else if(v == setNotiTime){
				setNotiTime();
			} else if(v == closeNoti){
				cancelNoti();
			}
		}
		
	};
	
	/**
	 * 取消提醒
	 */
	private void cancelNoti(){
		WmOpenChatSdk.getInstance().cancelNoti(new HandleListener() {
			
			@Override
			public void result(boolean success, String msg) {
				if(success){
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Noti.this)
								.setMessage(R.string.set_success)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
							
						}
					});
				} else{
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Noti.this)
							.setMessage(R.string.set_failed)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
							
						}
					});
				}
			}
		});
	}
	
	/**
	 * 设置提醒时段
	 */
	private void setNotiTime(){
		String str1 = notiTimeEt1.getText().toString().trim();
		String str2 = notiTimeEt2.getText().toString().trim();
		if(!str1.equals("") && !str2.equals("")){
			try {
				int start = Integer.parseInt(str1);
				int end = Integer.parseInt(str2);
				if(start >= 0 && start <= 24 && end >= 0 && end <= 24){
					WmOpenChatSdk.getInstance().setNotiTime(start, end, new HandleListener() {
						
						@Override
						public void result(boolean success, String msg) {
							if(success){
								runOnUiThread(new Runnable() {
									public void run() {
										new AlertDialog.Builder(Noti.this)
											.setMessage(R.string.set_success)
											.setPositiveButton(R.string.confirm, null)
											.create().show();
										
									}
								});
							} else{
								runOnUiThread(new Runnable() {
									public void run() {
										new AlertDialog.Builder(Noti.this)
										.setMessage(R.string.set_failed)
										.setPositiveButton(R.string.confirm, null)
										.create().show();
										
									}
								});
							}
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 查看提醒时段
	 */
	private void seeNotiTime(){
		NotiTime notiTime = WmOpenChatSdk.getInstance().getNotiTime();
		if(notiTime != null){
			String alertMsg = notiTime.getStart()+getString(R.string.hour)+" - "+notiTime.getEnd()+getString(R.string.hour);
			new AlertDialog.Builder(Noti.this)
				.setMessage(alertMsg)
				.setPositiveButton(R.string.confirm, null)
				.create().show();
		} else{
			new AlertDialog.Builder(Noti.this)
				.setMessage(R.string.no_info)
				.setPositiveButton(R.string.confirm, null)
				.create().show();
		}
	}
}
