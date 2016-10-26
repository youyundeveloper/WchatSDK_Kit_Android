package com.ioyouyun.ui.chat.test_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil.SendMsgListener;
import com.ioyouyun.ui_chat_sdk_test_project.R;

public class CustomMsg extends Activity {
	
	private EditText msgTypeEt;
	private EditText contentEt;
	private EditText idEt;
	private EditText openSdkContentEt;
	private RadioButton groupChatRb;
	private Button send;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_msg);
		msgTypeEt = (EditText)findViewById(R.id.msgType);
		contentEt = (EditText)findViewById(R.id.content);
		idEt = (EditText)findViewById(R.id.id);
		openSdkContentEt = (EditText)findViewById(R.id.openSdkContent);
		groupChatRb = (RadioButton)findViewById(R.id.groupChatRb);
		send = (Button)findViewById(R.id.send);
		send.setOnClickListener(onClickListener);
	}
	
	OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v == send){
				send();
			}
		}
	};
	
	private void send(){
		int msgType = 0;
		try {
			msgType = Integer.parseInt(msgTypeEt.getText().toString().trim());
		} catch (Exception e) {
		}
		
		if(msgType < 60000){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.msg_type_error);
			builder.setPositiveButton(R.string.confirm, null);
			builder.create().show();
			return;
		}
		
		String id = idEt.getText().toString().trim();
		if(id.equals("")){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.id_error);
			builder.setPositiveButton(R.string.confirm, null);
			builder.create().show();
			return;
		}
		
		String content = contentEt.getText().toString().trim();
		String openSdkContent = openSdkContentEt.getText().toString().trim();
		boolean isGroup = groupChatRb.isChecked();
		
		//调用发送接口
		WmOpenChatSdk.getInstance().sendCustomMsg(msgType, content, id, isGroup, openSdkContent, new SendMsgListener() {
			
			@Override
			public void handle(MsgInfo msgInfo) {
				AlertDialog.Builder builder = new AlertDialog.Builder(CustomMsg.this);
				builder.setMessage(R.string.send_success);
				builder.setPositiveButton(R.string.confirm, null);
				builder.create().show();
				return;
			}
		});
	}

}






