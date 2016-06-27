package com.ioyouyun.ui.chat.test_project;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui_chat_sdk_test_project.R;
import com.ioyouyun.wchat.message.HistoryMessage;
import com.ioyouyun.wchat.util.HttpCallback;

public class Black extends Activity {
	
	private Button getBlackListBtn;
	private EditText uidEt;
	private Button addBlackBtn;
	private Button delBlackBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.black);
		getBlackListBtn = (Button)findViewById(R.id.getBlackListBtn);
		getBlackListBtn.setOnClickListener(onClick);
		uidEt = (EditText)findViewById(R.id.uidEt);
		addBlackBtn = (Button)findViewById(R.id.addBlackBtn);
		addBlackBtn.setOnClickListener(onClick);
		delBlackBtn = (Button)findViewById(R.id.delBlackBtn);
		delBlackBtn.setOnClickListener(onClick);
	}
	
	OnClickListener onClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(v == getBlackListBtn){
				getBlackList();
			} else if(v == addBlackBtn){
				addBlack();
			} else if(v == delBlackBtn){
				delBlack();
			}
		}
	};
	
	/**
	 * 将用户移出黑名单
	 */
	private void delBlack(){
		String uid = uidEt.getText().toString().trim();
		WmOpenChatSdk.getInstance().delBlack(uid, new HttpCallback() {
			
			@Override
			public void onResponse(String response) {
				boolean success = false;
				try {
					JSONObject jsonObject = new JSONObject(response);
					int apistatus = jsonObject.optInt("apistatus", 0);
					if (apistatus == 1) {
						boolean result = jsonObject.optBoolean("result", false);
						if (result) {
							success = true;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(success){	//删除成功
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Black.this)
							.setMessage(R.string.del_success)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
						}
					});
				} else{			//删除失败
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Black.this)
							.setMessage(R.string.del_failed)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
						}
					});
				}
			}
			
			@Override
			public void onResponseHistory(List<HistoryMessage> arg0) {
				
			}
			
			@Override
			public void onError(Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Black.this)
						.setMessage(R.string.del_failed)
						.setPositiveButton(R.string.confirm, null)
						.create().show();
					}
				});
			}
		});
	}
	
	/**
	 * 将用户加入黑名单
	 */
	private void addBlack(){
		String uid = uidEt.getText().toString().trim();
		WmOpenChatSdk.getInstance().addBlack(uid, new HttpCallback() {
			
			@Override
			public void onResponse(String arg0) {
				boolean success = false;
				try {
					JSONObject jsonObject = new JSONObject(arg0);
					int apistatus = jsonObject.optInt("apistatus", 0);
					if (apistatus == 1) {
						boolean result = jsonObject.optBoolean("result", false);
						if (result) {
							success = true;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(success){	//添加成功
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Black.this)
							.setMessage(R.string.add_success)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
						}
					});
				} else{			//添加失败
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Black.this)
							.setMessage(R.string.add_failed)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
						}
					});
				}
			}
			
			@Override
			public void onResponseHistory(List<HistoryMessage> arg0) {
				
			}
			
			@Override
			public void onError(Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Black.this)
						.setMessage(R.string.add_failed)
						.setPositiveButton(R.string.confirm, null)
						.create().show();
					}
				});
			}
		});

	}
	
	/**
	 * 获取黑名单列表
	 */
	private void getBlackList(){
		WmOpenChatSdk.getInstance().getBlackList(new HttpCallback() {
			
			@Override
			public void onResponse(String response) {
				boolean flag = false;
				String msg = null;
				try {
					JSONObject jsonObject = new JSONObject(response);
					int apistatus = jsonObject.optInt("apistatus", 0);
					if (apistatus == 1) {
						JSONObject resultJson = jsonObject.optJSONObject("result");
						if (resultJson != null) {
							JSONArray usersJsonArray = resultJson
									.optJSONArray("users");
							if (usersJsonArray != null) {
								flag = true;
								msg = usersJsonArray.toString();
							}
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				final String fMsg = msg;
				if(flag){	//获取成功
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Black.this)
							.setMessage(fMsg)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
						}
					});
				} else{		//获取失败
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Black.this)
							.setMessage(R.string.get_failed)
							.setPositiveButton(R.string.confirm, null)
							.create().show();
						}
					});
				}
			}
			
			@Override
			public void onResponseHistory(List<HistoryMessage> arg0) {
				
			}
			
			@Override
			public void onError(Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Black.this)
						.setMessage(R.string.get_failed)
						.setPositiveButton(R.string.confirm, null)
						.create().show();
					}
				});
			}
		});
	}

}
