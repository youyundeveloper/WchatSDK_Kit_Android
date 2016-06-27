package com.ioyouyun.ui.chat.opensource;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ioyouyun.ui.chat.api.constant.LoginStatus;
import com.ioyouyun.ui.chat.bean.Conversation;
import com.ioyouyun.ui.chat.core.chat.util.ConversationDelayUpdateUtil;
import com.ioyouyun.ui.chat.core.login.LoginManager;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.LoginObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverManager;
import com.ioyouyun.ui.chat.db.tables.ConversationTbl;
import com.ioyouyun.ui.chat.lib.ResId;
import com.ioyouyun.ui.chat.lib.ResLayout;
import com.ioyouyun.ui.chat.lib.ResString;
import com.ioyouyun.ui.chat.ui.abstractcommponts.ParentActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class ConversationActivity extends ParentActivity {

	private Button back;
	private TextView conversationTitle;
	private View receiving;
	private ListView conversationList;
	private Handler mHandler;
	private ConversationAdapter conversationAdapter;
	private LoginObserverImpl loginObserverImpl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResLayout.getLayout_wm_conversation());//R.layout.wm_conversation
		mHandler = new Handler();
		loginObserverImpl = new LoginObserverImpl();
		ObserverManager.getInstance().addLoginObserver(loginObserverImpl);
		initUI();
		initialData();
	}

	private void initUI() {
		back = (Button) findViewById(ResId.getId_back());
		back.setOnClickListener(onClick);
		conversationList = (ListView) findViewById(ResId
				.getId_conversationList());
		conversationList.setScrollingCacheEnabled(false);
		conversationList.setAnimationCacheEnabled(false);
		conversationList.setOnScrollListener(new PauseOnScrollListener(
				ImageLoader.getInstance(), true, true));
		conversationTitle = (TextView) findViewById(ResId.getId_title());
		if (LoginManager.loginStatus == LoginStatus.LOGIN_STATUS_ONLINE) {
			conversationTitle.setText(ResString.getString_conversation_title());
		} else if(LoginManager.loginStatus == LoginStatus.LOGIN_STATUS_OFFLINE){
			conversationTitle.setText(ResString
					.getString_wm_conversatin_title_offline());
		} else {
			conversationTitle.setText(ResString
					.getString_wm_conversation_title_connecting());
		}
		receiving = findViewById(ResId.getId_receiving());
		if (ConversationDelayUpdateUtil.getInstance().isBatchReceiving) {
			receiving.setVisibility(View.VISIBLE);
		} else {
			receiving.setVisibility(View.GONE);
		}
	}

	private void initialData() {
		conversationAdapter = new ConversationAdapter(this,
				ConversationAdapter.USE_TYPE_2);
		conversationList.setAdapter(conversationAdapter);
		conversationList.setOnItemClickListener(conversationAdapter);
		conversationList.setOnItemLongClickListener(conversationAdapter);
		if (LoginManager.isOnLine()) {
			initConversation();
		}
	}

	private List<Conversation> conversations;

	synchronized private void initConversation() {
		if (conversations == null) {
			conversations = ConversationTbl.getInstance()
					.getSortedConversation();
			if (conversations != null) {
				conversationAdapter.setAllConversationData(conversations);
				conversationAdapter.calculateAndRefreshAllUnreadCount();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		conversationAdapter.onDestroy();
		removeObservers();
	}

	private void removeObservers() {
		ObserverManager.getInstance().removeLoginObserver(loginObserverImpl);
	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			back();
		}
	};

	private void back() {
		finish();
	}

	class LoginObserverImpl implements LoginObserver {
		@Override
		public void loginResult(boolean result) {

			ConversationDelayUpdateUtil.getInstance()
					.beginDelayUpdate(mHandler);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					conversationTitle.setText(ResString
							.getString_conversation_title());
					initDataAfterLoginSuccess();
				}
			});
		}
	}

	private void initDataAfterLoginSuccess() {
		initConversation();
	}

}
