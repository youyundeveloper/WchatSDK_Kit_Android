package com.ioyouyun.ui.chat.test_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui_chat_sdk_test_project.R;
import com.ioyouyun.wchat.util.HttpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Group extends Activity {

	private Button createGroupBtn;
	private EditText gidEt;
	private EditText uidsEt;
	private Button addGroupMemberBtn;
	private Button delGroupMemberBtn;
	private EditText gid2Et;
	private Button getGroupMembersBtn;
	private Button getMyGroupsBtn;
	private Button exitGroupBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.group);
		createGroupBtn = (Button) findViewById(R.id.createGroupBtn);
		createGroupBtn.setOnClickListener(onClick);
		gidEt = (EditText) findViewById(R.id.gidEt);
		uidsEt = (EditText) findViewById(R.id.uidsEt);
		addGroupMemberBtn = (Button) findViewById(R.id.addGroupMemberBtn);
		addGroupMemberBtn.setOnClickListener(onClick);
		delGroupMemberBtn = (Button) findViewById(R.id.delGroupMemberBtn);
		delGroupMemberBtn.setOnClickListener(onClick);
		gid2Et = (EditText) findViewById(R.id.gid2Et);
		getGroupMembersBtn = (Button) findViewById(R.id.getGroupMembersBtn);
		getGroupMembersBtn.setOnClickListener(onClick);
		getMyGroupsBtn = (Button) findViewById(R.id.getMyGroupsBtn);
		getMyGroupsBtn.setOnClickListener(onClick);
		exitGroupBtn = (Button)findViewById(R.id.exitGroupBtn);
		exitGroupBtn.setOnClickListener(onClick);
	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == createGroupBtn) {
				createGroup();
			} else if (v == addGroupMemberBtn) {
				addGroupMembers();
			} else if (v == delGroupMemberBtn) {
				delGroupMembers();
			} else if (v == getGroupMembersBtn) {
				getGroupMembers();
			} else if (v == getMyGroupsBtn) {
				getMyGroups();
			} else if(v == exitGroupBtn){
				exitGroup();
			}
		}
	};
	
	/**
	 * 退出群
	 */
	private void exitGroup(){
		long gid = 0;
		try {
			gid = Long.parseLong(gid2Et.getText().toString().trim());
		} catch (Exception e) {
		}
		if (gid <= 0) {
			return;
		}
		WmOpenChatSdk.getInstance().exitGroup(gid, new HttpCallback() {
			
			@Override
			public void onResponse(final String arg0) {
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
				
				if(success){	//退出成功
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Group.this)
									.setMessage(R.string.exit_success)
									.setPositiveButton(R.string.confirm, null)
									.create().show();
						}
					});
				} else{			//退出失败
					runOnUiThread(new Runnable() {
						public void run() {
							new AlertDialog.Builder(Group.this)
									.setMessage(R.string.exit_failed)
									.setPositiveButton(R.string.confirm, null)
									.create().show();
						}
					});
				}
			}
			
			@Override
			public void onResponseHistory(List arg0) {
				
			}
			
			@Override
			public void onError(Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this)
								.setMessage(R.string.exit_failed)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
		});
	}

	/**
	 * 获取我的群列表
	 */
	private void getMyGroups() {
		
		WmOpenChatSdk.getInstance().getGroupList(new HttpCallback() {
			
			@Override
			public void onResponse(final String arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this)
								.setMessage(arg0)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
			
			@Override
			public void onResponseHistory(List paramList) {
				
			}
			
			@Override
			public void onError(Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this)
								.setMessage(R.string.get_failed)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
		});
	}

	/**
	 * 获取群成员列表
	 */
	private void getGroupMembers() {
		long gid = 0;
		try {
			gid = Long.parseLong(gid2Et.getText().toString().trim());
		} catch (Exception e) {
		}
		if (gid <= 0) {
			return;
		}
		WmOpenChatSdk.getInstance().getGroupUsers(gid, new HttpCallback() {

			@Override
			public void onResponse(final String arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this).setMessage(arg0)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
			
			@Override
			public void onResponseHistory(List arg0) {
				
			}

			@Override
			public void onError(Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this)
								.setMessage(R.string.get_failed)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
		});
	}

	/**
	 * 踢人/退出群
	 */
	private void delGroupMembers() {
		long gid = 0;
		try {
			gid = Long.parseLong(gidEt.getText().toString().trim());
		} catch (Exception e) {
		}
		if (gid <= 0) {
			return;
		}
		WmOpenChatSdk.getInstance().delGroupUser(gid,
				uidsEt.getText().toString().trim(), new HttpCallback() {

					@Override
					public void onResponse(String response) {
						boolean success = false;
						try {
							JSONObject jsonObject = new JSONObject(response);
							int apistatus = jsonObject.optInt("apistatus", 0);
							if (apistatus == 1) {
								int result = jsonObject.optInt("result", 0);
								if (result == 1) {
									success = true;
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (success) { // 移除成功
							runOnUiThread(new Runnable() {
								public void run() {
									new AlertDialog.Builder(Group.this)
											.setMessage(R.string.remove_success)
											.setPositiveButton(
													R.string.confirm, null)
											.create().show();
								}
							});
						} else { // 移除失败
							runOnUiThread(new Runnable() {
								public void run() {
									new AlertDialog.Builder(Group.this)
											.setMessage(R.string.remove_failed)
											.setPositiveButton(
													R.string.confirm, null)
											.create().show();
								}
							});
						}
					}
					
					@Override
					public void onResponseHistory(List arg0) {
						
					}

					@Override
					public void onError(Exception arg0) {
						runOnUiThread(new Runnable() {
							public void run() {
								new AlertDialog.Builder(Group.this)
										.setMessage(R.string.remove_failed)
										.setPositiveButton(R.string.confirm,
												null).create().show();
							}
						});
					}
				});
	}

	/**
	 * 添加群成员
	 */
	private void addGroupMembers() {
		long gid = 0;
		try {
			gid = Long.parseLong(gidEt.getText().toString().trim());
		} catch (Exception e) {
		}
		if (gid <= 0) {
			return;
		}
		WmOpenChatSdk.getInstance().addGroupUser(gid,
				uidsEt.getText().toString().trim(), new HttpCallback() {

					@Override
					public void onResponse(final String response) {
						boolean success = false;
						try {
							JSONObject jsonObject = new JSONObject(response);
							int apistatus = jsonObject.optInt("apistatus", 0);
							if (apistatus == 1) {
								int result = jsonObject.optInt("result", 0);
								if (result == 1) {
									success = true;
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (success) { // 添加成功
							runOnUiThread(new Runnable() {
								public void run() {
									new AlertDialog.Builder(Group.this)
											.setMessage(R.string.add_success)
											.setPositiveButton(
													R.string.confirm, null)
											.create().show();
								}
							});
						} else { // 添加失败
							runOnUiThread(new Runnable() {
								public void run() {
									new AlertDialog.Builder(Group.this)
											.setMessage(R.string.add_failed)
											.setPositiveButton(
													R.string.confirm, null)
											.create().show();
								}
							});
						}
					}
					
					@Override
					public void onResponseHistory(List arg0) {
						
					}

					@Override
					public void onError(final Exception arg0) {
						runOnUiThread(new Runnable() {
							public void run() {
								new AlertDialog.Builder(Group.this)
										.setMessage(R.string.add_failed)
										.setPositiveButton(R.string.confirm,
												null).create().show();
							}
						});
					}
				});
	}

	/**
	 * 创建群
	 */
	private void createGroup() {
		WmOpenChatSdk.getInstance().createGroup(new HttpCallback() {

			@Override
			public void onResponse(final String arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this).setMessage(arg0)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
			
			@Override
			public void onResponseHistory(List arg0) {
				
			}

			@Override
			public void onError(final Exception arg0) {
				runOnUiThread(new Runnable() {
					public void run() {
						new AlertDialog.Builder(Group.this)
								.setMessage(R.string.create_failed)
								.setPositiveButton(R.string.confirm, null)
								.create().show();
					}
				});
			}
		});
	}
}
