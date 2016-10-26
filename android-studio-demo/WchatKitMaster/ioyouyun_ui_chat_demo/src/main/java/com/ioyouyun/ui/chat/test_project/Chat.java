package com.ioyouyun.ui.chat.test_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ioyouyun.ui.chat.api.Observers.HandleListener;
import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui_chat_sdk_test_project.R;

public class Chat extends Activity {
	
	private Button conversationBtn;
	private EditText uidEt;
	private Button singleChatBtn;
	private Button clearSingleChatRecord;
	private EditText gidEt;
	private Button groupChatBtn;
	private Button clearGroupChatRecord;
	private Button clearAllChatRecord;
	private EditText pushNickNameEt;
	private Button setPushNickNameBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat);
		conversationBtn = (Button)findViewById(R.id.conversationBtn);
		conversationBtn.setOnClickListener(onClick);
		uidEt = (EditText)findViewById(R.id.uidEt);
		singleChatBtn = (Button)findViewById(R.id.singleChatBtn);
		singleChatBtn.setOnClickListener(onClick);
		clearSingleChatRecord = (Button)findViewById(R.id.clearSingleChatRecord);
		clearSingleChatRecord.setOnClickListener(onClick);
		gidEt = (EditText)findViewById(R.id.gidEt);
		groupChatBtn = (Button)findViewById(R.id.groupChatBtn);
		groupChatBtn.setOnClickListener(onClick);
		clearGroupChatRecord = (Button)findViewById(R.id.clearGroupChatRecord);
		clearGroupChatRecord.setOnClickListener(onClick);
		clearAllChatRecord = (Button)findViewById(R.id.clearAllChatRecord);
		clearAllChatRecord.setOnClickListener(onClick);
		pushNickNameEt = (EditText)findViewById(R.id.pushNickNameEt);
		setPushNickNameBtn = (Button)findViewById(R.id.setPushNickNameBtn);
		setPushNickNameBtn.setOnClickListener(onClick);
	}
	
	OnClickListener onClick = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(v == conversationBtn){
				WmOpenChatSdk.getInstance().gotoConversationPage(Chat.this);
			} else if(v == singleChatBtn){
				openSingleChat();
			} else if(v == clearSingleChatRecord){
				clearSingleChatRecord();
			} else if(v == groupChatBtn){
				openGroupChat();
			} else if(v == clearGroupChatRecord){
				clearGroupChatRecord();
			} else if(v == clearAllChatRecord){
				clearAllChatRecord();
			} else if(v == setPushNickNameBtn){
				setPushNickName();
			}
		}
		
	};
	
	/**
	 * 设置推送昵称
	 */
	private void setPushNickName(){
		WmOpenChatSdk.getInstance().setPushNickName(
				pushNickNameEt.getText().toString().trim(), 
				new HandleListener() {
					
					@Override
					public void result(boolean success, String msg) {
						if(success){
							runOnUiThread(new Runnable() {
								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
									builder.setMessage(R.string.set_success);
									builder.setPositiveButton(R.string.confirm, null);
									builder.create().show();
								}
							});
						} else{
							runOnUiThread(new Runnable() {
								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
									builder.setMessage(R.string.set_failed);
									builder.setPositiveButton(R.string.confirm, null);
									builder.create().show();
								}
							});
						}
					}
				});
	}
	
	/**
	 * 清空我的所有聊天记录
	 */
	private void clearAllChatRecord() {
		WmOpenChatSdk.getInstance().clearChatRecord(new HandleListener() {

			@Override
			public void result(boolean success, String msg) {
				if(success){
					runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
							builder.setMessage(R.string.handle_success);
							builder.setPositiveButton(R.string.confirm, null);
							builder.create().show();
						}
					});
				}
			}
		});
	}
	
	/**
	 * 清空群聊记录
	 */
	private void clearGroupChatRecord() {
		String gid = gidEt.getText().toString().trim();
		WmOpenChatSdk.getInstance().clearGroupChatRecord(gid,
				new HandleListener() {

					@Override
					public void result(boolean success, String msg) {
						if(success){
							runOnUiThread(new Runnable() {
								public void run() {
									AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
									builder.setMessage(R.string.handle_success);
									builder.setPositiveButton(R.string.confirm, null);
									builder.create().show();
								}
							});
						}
					}
				});
	}
	
	/**
	 * 打开群聊
	 */
	private void openGroupChat(){
		String gid = gidEt.getText().toString().trim();
		try {
			WmOpenChatSdk.getInstance().gotoGroupChatPage(Long.parseLong(gid), this);			
		} catch (Exception e) {
		}
	}
	
	/**
	 * 清空单聊聊天记录
	 */
	private void clearSingleChatRecord(){
		String uid = uidEt.getText().toString().trim();
		WmOpenChatSdk.getInstance().clearSingleChatRecord(uid, new HandleListener() {
			
			@Override
			public void result(boolean success, String msg) {
				if(success){
					runOnUiThread(new Runnable() {
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(Chat.this);
							builder.setMessage(R.string.handle_success);
							builder.setPositiveButton(R.string.confirm, null);
							builder.create().show();
						}
					});
				}
			}
		});
	}
	
	/**
	 * 进入单聊
	 */
	private void openSingleChat(){
		String uid = uidEt.getText().toString().trim();
		WmOpenChatSdk.getInstance().gotoChatPage(uid, this);
	}

}
