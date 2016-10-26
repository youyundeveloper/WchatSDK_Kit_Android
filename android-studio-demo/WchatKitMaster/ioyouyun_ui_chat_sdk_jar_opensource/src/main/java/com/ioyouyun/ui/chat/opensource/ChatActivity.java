package com.ioyouyun.ui.chat.opensource;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hzmc.audioplugin.MediaManager;
import com.hzmc.audioplugin.MediaManager.RecordCallBack;
import com.ioyouyun.ui.chat.api.Observers.LoginStatusChangedListener;
import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui.chat.api.constant.LoginStatus;
import com.ioyouyun.ui.chat.bean.ChatAdditionOperateOption;
import com.ioyouyun.ui.chat.bean.DeviceInfo;
import com.ioyouyun.ui.chat.bean.Drafts;
import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.core.Consts;
import com.ioyouyun.ui.chat.core.RequestCode;
import com.ioyouyun.ui.chat.core.chat.Sender;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil;
import com.ioyouyun.ui.chat.core.login.LoginManager;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.ChatMsgReceiveObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgClearObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgContentChangedObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgSendServerInfoChangedObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgStatusObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverManager;
import com.ioyouyun.ui.chat.core.util.AppUtil;
import com.ioyouyun.ui.chat.core.util.ExpEmojiUtil;
import com.ioyouyun.ui.chat.core.util.FileUtil;
import com.ioyouyun.ui.chat.db.tables.DraftTbl;
import com.ioyouyun.ui.chat.db.tables.MsgInfoTbl;
import com.ioyouyun.ui.chat.lib.ResAnim;
import com.ioyouyun.ui.chat.lib.ResColor;
import com.ioyouyun.ui.chat.lib.ResDrawable;
import com.ioyouyun.ui.chat.lib.ResId;
import com.ioyouyun.ui.chat.lib.ResLayout;
import com.ioyouyun.ui.chat.lib.ResString;
import com.ioyouyun.ui.chat.ui.UseCameraActivity;
import com.ioyouyun.ui.chat.ui.adapter.ChatAdditionPagerAdapter;
import com.ioyouyun.ui.chat.ui.adapter.ExpEmojiViewPagerAdapter;
import com.ioyouyun.ui.chat.ui.chat.ChatActivityManager;
import com.ioyouyun.ui.chat.ui.choosemultipictures.ChooseMultiPicturesActivity;
import com.ioyouyun.ui.chat.util.DeviceUtil;
import com.ioyouyun.ui.chat.util.ImageUtil;
import com.ioyouyun.ui.chat.view.MyGestureRelativeLayout;
import com.ioyouyun.ui.chat.view.MyGestureRelativeLayout.MyGestureObserver;
import com.ioyouyun.ui.chat.view.MyTouchRelativeLayout;
import com.ioyouyun.ui.chat.view.NestedHorizontalScrollView;
import com.ioyouyun.ui.chat.view.NestedViewPager;
import com.ioyouyun.wchat.GroupIdConv;
import com.ioyouyun.wchat.message.ConvType;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class ChatActivity extends ChatActivityParent {

	private final static String TAG = ChatActivity.class.getSimpleName();

	private Button back;
	private TextView title;
	private ImageButton type;
	private ImageButton talk;
	private EditText inputText;
	private EditText pressTalk;
	private ImageButton add;
	private Button sendBtn;
	private View addition;
	private View chatAudioRecord;
	private View txtInputLayout;
	private View chatAudioRecordStat1;
	private View chatAudioRecordStat2;
	private View audioTooShort;
	private PullToRefreshListView msgListRefresh;
	private ListView messageList;
	private View expressionView;
	private ImageButton emojiBtn;
	private ImageButton weimiBtn;
	private TextView talkTimeText;
	private ImageView volImg;
	private MyGestureRelativeLayout gestureLayout;
	public MyTouchRelativeLayout myTouchRelativeLayout;
	private View blackView;
	public NestedViewPager expEmojiViewPager;
	
	private MessageAdapter messageAdapter;
	
	public MsgStatusObserverImpl msgStatusObserverImpl;
	public MsgClearObserverImpl msgClearObserverImpl;
	public ChatMsgReceiveObserverImpl chatMsgReceiveObserverImpl;
	public MsgSendServerInfoChangedObserverImpl msgSendServerInfoChangedObserverImpl;
	public MsgContentChangedObserverImpl msgContentChangedObserverImpl;
	private LoginStatusChangedListenerImpl loginStatusChangedListenerImpl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ChatActivityManager.getInstance().onCreate(this);
		setContentView(ResLayout.getLayout_wm_chat_page());
		init();
	}

	private void init() {
		initialUI();
		initialData();
		registObservers();
	}

	private void initialUI() {
		back = (Button) findViewById(ResId.getId_back());
		back.setOnClickListener(onClick);
		blackView = findViewById(ResId.getId_blackView());
		blackView.setOnClickListener(onClick);
		blackView.setVisibility(View.GONE);
		myTouchRelativeLayout = (MyTouchRelativeLayout) findViewById(ResId
				.getId_container_all());
		chatAudioRecord = findViewById(ResId.getId_chat_audio_record());
		int recordNotiMargin = DeviceInfo.mScreenHeigth / 2
				- DeviceUtil.dip2px(this, 100);
		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) chatAudioRecord
				.getLayoutParams();
		layoutParams.setMargins(0, 0, 0, recordNotiMargin);
		chatAudioRecord.setVisibility(View.GONE);
		volImg = (ImageView) chatAudioRecord.findViewById(ResId.getId_vol());
		talkTimeText = (TextView) chatAudioRecord.findViewById(ResId
				.getId_count());
		chatAudioRecordStat1 = chatAudioRecord
				.findViewById(ResId.getId_stat1());
		chatAudioRecordStat2 = chatAudioRecord
				.findViewById(ResId.getId_stat2());
		audioTooShort = findViewById(ResId.getId_audio_too_short());
		layoutParams = (RelativeLayout.LayoutParams) audioTooShort
				.getLayoutParams();
		layoutParams.setMargins(0, 0, 0, recordNotiMargin);
		audioTooShort.setVisibility(View.GONE);
		title = (TextView) findViewById(ResId.getId_member());
		title.setOnClickListener(onClick);
		txtInputLayout = findViewById(ResId.getId_txt_input_layout());
		sendBtn = (Button) findViewById(ResId.getId_sendBtn());
		sendBtn.setOnClickListener(onClick);
		sendBtn.setEnabled(false);
		type = (ImageButton) findViewById(ResId.getId_type());
		type.setOnClickListener(onClick);
		talk = (ImageButton) findViewById(ResId.getId_talk());
		talk.setOnClickListener(onClick);
		inputText = (EditText) findViewById(ResId.getId_text_input());
		inputText.setOnClickListener(onClick);
		setInputTextChangedListener();
		pressTalk = (EditText) findViewById(ResId.getId_press_talk());
		pressTalk.setOnTouchListener(onTouch);
		pressTalk.setBackgroundResource(ResDrawable
				.getDrawable_wm_s_n_chat_bottom_input_bg());

		pressTalk.setTextColor(getResources().getColor(
				ResColor.getColor_wm_press_talk_bg_nor()));
		pressTalk.setText(ResString.getString_wm_press_talk());
		add = (ImageButton) findViewById(ResId.getId_add());
		add.setOnClickListener(onClick);
		msgListRefresh = (PullToRefreshListView) findViewById(ResId
				.getId_msg_list_refresh());
		msgListRefresh.getLoadingLayoutProxy().setPullLabel(
				getString(ResString.getString_wm_pull_to_load_more()));
		messageList = msgListRefresh.getRefreshableView();
		messageList.setCacheColorHint(Color.TRANSPARENT);
		messageList.setDivider(null);
		messageList.setVerticalScrollBarEnabled(false);
		messageList.setSelector(android.R.color.transparent);
		messageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		messageList.setScrollingCacheEnabled(false);
		messageList.setAnimationCacheEnabled(false);
		setMessageListOnTouchListener();
		addition = findViewById(ResId.getId_addition_opreates());
		addition.setVisibility(View.GONE);
		expressionView = findViewById(ResId.getId_expression_view());
		weimiBtn = (ImageButton) findViewById(ResId.getId_weimi_exp_btn());
		weimiBtn.setOnClickListener(onClick);
		weimiBtn.setSelected(true);
		emojiBtn = (ImageButton) findViewById(ResId.getId_emoji_exp_btn());
		emojiBtn.setOnClickListener(onClick);
		expEmojiViewPager = (NestedViewPager) findViewById(ResId
				.getId_expEmojiViewPager());
		showExpEmojiExpression(ExpEmojiUtil.TYPE_WEIMI_EXP);
		setListViewRefreshListener();
		gestureLayout = (MyGestureRelativeLayout) findViewById(ResId
				.getId_gesture_layout());
		setGestureListener();
		setExceptFromGestureBack();
		setAdditionOptions();
	}

	private List<String> allExpKeys = null;

	private void showExpEmojiExpression(int expType) {
		if (allExpKeys == null) {
			allExpKeys = ExpEmojiUtil.getInstance(this).getAllExpKeys();
		}
		expEmojiViewPagerAdapter = new ExpEmojiViewPagerAdapter(this, expType,
				inputText, allExpKeys);
		expEmojiViewPager.setAdapter(expEmojiViewPagerAdapter);
		setExpEmojiPagerOnPageChangeListener();
	}

	int expEmojiCurrentPagePoint = 0;
	int expEmojiSelPagePoint = 0;

	private void setExpEmojiPagerOnPageChangeListener() {
		expEmojiCurrentPagePoint = 0;
		expEmojiSelPagePoint = 0;
		final LinearLayout pointParent = (LinearLayout) findViewById(ResId
				.getId_expEmojiViewPagerPoint());
		final int total = expEmojiViewPagerAdapter.getCount();
		setPagerPoint(pointParent, total, expEmojiCurrentPagePoint);
		expEmojiViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				expEmojiSelPagePoint = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
					if (expEmojiSelPagePoint != expEmojiCurrentPagePoint) {
						setPagerPoint(pointParent, total, expEmojiSelPagePoint);
						expEmojiCurrentPagePoint = expEmojiSelPagePoint;
					}
				}
			}
		});
	}

	private void setInputTextChangedListener() {
		inputText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String content = s.toString().trim();
				if (content.equals("")) {
					sendBtn.setEnabled(false);
				} else {
					sendBtn.setEnabled(true);
				}
				if (s != null && !s.toString().equals("")) {
					Matcher matcher = ExpEmojiUtil
							.getInstance(ChatActivity.this).mPattern.matcher(s);
					while (matcher.find()) {
						inputText.getText().setSpan(
								new ImageSpan(ChatActivity.this, ExpEmojiUtil
										.getInstance(ChatActivity.this)
										.getResId(matcher.group(), 1)),
								matcher.start(), matcher.end(),
								Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					}

				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	int additionCurrentPagePoint = 0;
	int additionSelPagePoint = 0;

	private void setAdditionOptions() {
		NestedViewPager additionOpreatesVp = (NestedViewPager) findViewById(ResId
				.getId_addition_opreates_vp());
		additionOpreatesVp.setNestedpParent(gestureLayout);
		List<ChatAdditionOperateOption> options = getAdditionOptions();
		ChatAdditionPagerAdapter adapter = new ChatAdditionPagerAdapter(this,
				options);
		additionOpreatesVp.setAdapter(adapter);

		int total = options.size() / ChatAdditionPagerAdapter.PAGE_OPTION_COUNT;
		if (options.size() % ChatAdditionPagerAdapter.PAGE_OPTION_COUNT > 0) {
			++total;
		}
		if (total > 1) {
			final int f_total = total;
			final LinearLayout pointParent = (LinearLayout) findViewById(ResId
					.getId_addition_opreates_point());
			additionCurrentPagePoint = 0;
			additionSelPagePoint = 0;
			setPagerPoint(pointParent, total, additionSelPagePoint);
			additionOpreatesVp
					.setOnPageChangeListener(new OnPageChangeListener() {

						@Override
						public void onPageSelected(int position) {
							additionSelPagePoint = position;
						}

						@Override
						public void onPageScrolled(int arg0, float arg1,
								int arg2) {

						}

						@Override
						public void onPageScrollStateChanged(int arg0) {
							if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
								if (additionSelPagePoint != additionCurrentPagePoint) {
									setPagerPoint(pointParent, f_total,
											additionSelPagePoint);
									additionCurrentPagePoint = additionSelPagePoint;
								}
							}
						}
					});
		}
	}

	private void setPagerPoint(LinearLayout parent, int total, int position) {
		parent.removeAllViews();
		for (int i = 0; i < total; i++) {
			ImageView imageView = new ImageView(this);
			if (i == position) {
				imageView.setImageResource(ResDrawable
						.getDrawable_wm_s_login_page_bg_hover());
			} else {
				imageView.setImageResource(ResDrawable
						.getDrawable_wm_s_login_page_bg());
			}
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.setMargins(3, 3, 3, 3);
			imageView.setLayoutParams(params);
			parent.addView(imageView);
		}
	}

	private List<ChatAdditionOperateOption> getAdditionOptions() {
		List<ChatAdditionOperateOption> options = new ArrayList<ChatAdditionOperateOption>();

		ChatAdditionOperateOption option = new ChatAdditionOperateOption();
		option.setIcon(ResDrawable.getDrawable_wm_s_n_chat_bottom_btn_face());
		option.setName(getString(ResString.getString_wm_expression()));
		option.setOnClickListener(additionExpressionOnClick);
		options.add(option);

		option = new ChatAdditionOperateOption();
		option.setIcon(ResDrawable.getDrawable_wm_s_n_chat_btn_photo());
		option.setName(getString(ResString.getString_wm_message_photo()));
		option.setOnClickListener(additionPhotoOnClick);
		options.add(option);

		option = new ChatAdditionOperateOption();
		option.setIcon(ResDrawable.getDrawable_wm_s_n_chat_btn_shoot());
		option.setName(getString(ResString.getString_wm_message_camera()));
		option.setOnClickListener(additionCamera);
		options.add(option);

		return options;
	}

	OnClickListener additionCamera = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ChatActivity.this,
					UseCameraActivity.class);

			startActivityForResult(intent, UseCameraActivity.GET_IMAGE_REQ);
		}
	};

	OnClickListener additionPhotoOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(ChatActivity.this,
					ChooseMultiPicturesActivity.class);
			intent.putExtra("maxCountLimit", ChatUtil.IMG_MSG_CHOOSE_MAX_COUNT);
			startActivityForResult(intent, RequestCode.CODE_REQUEST_IMG_ORI);
		}
	};

	OnClickListener additionExpressionOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			hideInputKeyBoard();
			talk.setVisibility(View.VISIBLE);
			txtInputLayout.setVisibility(View.VISIBLE);
			inputText.requestFocus();
			type.setVisibility(View.INVISIBLE);
			pressTalk.setVisibility(View.INVISIBLE);

			if (expressionView.getVisibility() == View.VISIBLE) {
				expressionView.setVisibility(View.GONE);
			} else {
				expressionView.setVisibility(View.VISIBLE);
				listScrollToBottom();
			}
			if (addition.getVisibility() == View.VISIBLE) {
				addition.setVisibility(View.GONE);
			}
		}
	};

	private void setExceptFromGestureBack() {
		expEmojiViewPager.setNestedpParent(gestureLayout);
		((NestedHorizontalScrollView) findViewById(ResId.getId_exp_btn_layout()))
				.setNestedpParent(gestureLayout);
	}

	private void initialData() {

		setInputText();
		setTitle();
		messageAdapter = new MessageAdapter(messageList, this);
		messageList.setAdapter(messageAdapter);
		loadMore(false);
	}
	
	private void setTitle() {
		
		String titleStr = null;
		if(isGroup){
			titleStr = gid;
		} else{
			titleStr = uid;
		}
		if (LoginManager.loginStatus != LoginStatus.LOGIN_STATUS_ONLINE) {
			titleStr += getNotOnlineHint();
		}
		title.setText(ExpEmojiUtil.getInstance(this).replaceSmall(titleStr));
	}

	private String getNotOnlineHint() {
		String hint = "";
		if (LoginManager.loginStatus == LoginStatus.LOGIN_STATUS_ING) {
			hint = "("
					+ getResources().getString(
							ResString.getString_wm_login_ing()) + ")";
		} else if (LoginManager.loginStatus == LoginStatus.LOGIN_STATUS_OFFLINE) {
			hint = "("
					+ getResources().getString(
							ResString.getString_wm_no_login()) + ")";
		}
		return hint;
	}

	private void hideBeforeScroll() {
		hideInputKeyBoard();
		hideChatExcessView();
	}

	private void setGestureListener() {
		gestureLayout.setGestureListener(new MyGestureObserver() {

			@Override
			public void handle(int direction) {
				if (direction == MyGestureObserver.DIRECTION_LEFT) {

				} else if (direction == MyGestureObserver.DIRECTION_RIGHT) {
					back();
				}
			}
		});
	}

	private void setInputText() {
		Drafts drafts = DraftTbl.getInstance().getDrafts(conversationId,
				LoginManager.loginUserInfo.getId());
		String inputContent = "";
		if (drafts != null) {
			inputContent = drafts.getContent();
		}
		inputText.setText(ExpEmojiUtil.getInstance(this).replaceSmall(
				inputContent));
		inputText.setSelection(inputText.getText().length());
	}

	private void saveInputText() {
		if (inputText != null) {
			String inputContent = inputText.getText().toString().trim();
			Drafts drafts = new Drafts();
			drafts.setTarget(conversationId);
			drafts.setSender(LoginManager.loginUserInfo.getId());
			drafts.setContent(inputContent);
			DraftTbl.getInstance().replaceDrafts(drafts);
			ObserverManager.getInstance().notifyDraftContentChangedObservers(
					drafts.getTarget(), drafts.getContent());
		}
	}

	private int pagesize = 20;

	public boolean needGotoLoadHistory(List<MsgInfo> msgInfos) {
		if (msgInfos.size() <= 0) {
			return true;
		}
		// 是否为新版消息
		if (msgInfos.get(0).getOrderNum() <= 0) {
			return false;
		}
		int conversationFlag = ChatUtil
				.getConversationFlagFromMapCache(conversationId);
		for (MsgInfo msgInfo : msgInfos) {
			if (msgInfo.getFlag() != conversationFlag) {
				return true;
			}
		}

		return false;
	}

	private void loadMore(final boolean loadFromServer) {
		if (!listRefreshing) {
			listRefreshing = true;
			try {
				Method handleAddLocalMsgInfosMethod = ChatActivity.class.getMethod("handleAddLocalMsgInfos", List.class);
				Method setRefreshingFinishMethod = ChatActivity.class.getMethod("setRefreshingFinish");
				Method needGotoLoadHistoryMethod = ChatActivity.class.getMethod("needGotoLoadHistory",List.class);
				startLoadMore(messageAdapter, conversationId, pagesize, loadFromServer, uid, this, 
						needGotoLoadHistoryMethod, handleAddLocalMsgInfosMethod, setRefreshingFinishMethod);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} else {
			setRefreshingFinish();
		}
	}

	public void handleAddLocalMsgInfos(final List<MsgInfo> msgInfos) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				final int beforeSize = messageAdapter.getCount();
				boolean checkRepeat = false;
				if (inTime > 0
						&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
					checkRepeat = true;
				}
				messageAdapter.addTopAll(msgInfos, checkRepeat);
				final int afterSize = messageAdapter.getCount();
				setRefreshingFinish();
				messageList.setSelection(afterSize - beforeSize);
			}
		});
	}

	private void listScrollToBottom() {
		messageList.post(new Runnable() {

			@Override
			public void run() {
				messageList.setSelection(messageAdapter.getCount());
			}
		});
	}

	private boolean firstLoad = true;

	public void setRefreshingFinish() {
		if (firstLoad) {
			firstLoad = false;
			messageList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
			messageList.setVerticalScrollBarEnabled(true);
		}
		listRefreshing = false;
		msgListRefresh.onRefreshComplete();
	}

	private void setListViewRefreshListener() {
		msgListRefresh
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						loadMore(true);
					}
				});
	}

	private void setMessageListOnTouchListener() {
		
		messageList.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					hideBeforeScroll();
				}
				return false;
			}
		});
	}

	OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v == back) {
				hideInputKeyBoard();
				back();
			} else if (v == talk) {
				hideInputKeyBoard();
				talk.setVisibility(View.INVISIBLE);
				txtInputLayout.setVisibility(View.INVISIBLE);
				type.setVisibility(View.VISIBLE);
				pressTalk.setVisibility(View.VISIBLE);
				hideChatExcessView();

			} else if (v == type) {
				showInputKeyBoard();
				talk.setVisibility(View.VISIBLE);
				txtInputLayout.setVisibility(View.VISIBLE);
				inputText.requestFocus();
				type.setVisibility(View.INVISIBLE);
				pressTalk.setVisibility(View.INVISIBLE);
				hideChatExcessView();
			} else if (v == add) {
				hideInputKeyBoard();
				if (addition.getVisibility() == View.VISIBLE) {
					addition.setVisibility(View.GONE);
				} else {
					addition.setVisibility(View.VISIBLE);
					listScrollToBottom();
				}
				if (expressionView.getVisibility() == View.VISIBLE) {
					expressionView.setVisibility(View.GONE);
				}
				talk.setVisibility(View.VISIBLE);
				txtInputLayout.setVisibility(View.VISIBLE);
				inputText.requestFocus();
				type.setVisibility(View.INVISIBLE);
				pressTalk.setVisibility(View.INVISIBLE);
			} else if (v == sendBtn) {
				String msg = inputText.getText().toString().trim();
				if (msg == null || msg.equals("")) {
					showToastMsgNull();
					return;
				}
				if (ChatUtil.checkSendTxtMsgExceed(msg)) {
					sendMsg(ChatUtil.MSGT_TEXT, null, msg,null);
					inputText.setText("");
				} else {
					String dialogMsg = getString(ResString
							.getString_wm_send_msg_txt_exceed_hint_p1())
							+ Consts.getConfigMaxChatTxtMsgLen()
							+ getString(ResString
									.getString_wm_send_msg_txt_exceed_hint_p2());
					new AlertDialog.Builder(ChatActivity.this)
							.setMessage(dialogMsg)
							.setPositiveButton(ResString.getString_confirm(),
									null).create().show();
				}
			} else if (v == inputText) {
				addition.setVisibility(View.GONE);
				expressionView.setVisibility(View.GONE);
				listScrollToBottom();
			} else if (v == weimiBtn || v == emojiBtn) {
				setExpressionBtnSelection(v);
			} else if (v == title) {
				messageList.setSelection(0);
			}
		}
	};

	private void setExpressionBtnSelection(View sel) {
		if (sel == weimiBtn) {
			if (!weimiBtn.isSelected()) {
				weimiBtn.setSelected(true);
				showExpEmojiExpression(ExpEmojiUtil.TYPE_WEIMI_EXP);
			}
		} else {
			weimiBtn.setSelected(false);
		}

		if (sel == emojiBtn) {
			if (!emojiBtn.isSelected()) {
				emojiBtn.setSelected(true);
				showExpEmojiExpression(ExpEmojiUtil.TYPE_EMOJI);
			}
		} else {
			emojiBtn.setSelected(false);
		}
	}

	private void showToastMsgNull() {
		Toast.makeText(
				this,
				getResources().getString(
						ResString.getString_wm_send_must_not_none()),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * 隐藏聊天其他元素
	 */
	public void hideChatExcessView() {
		hideExpressionView();
		hideAddView();
	}

	public void hideInputKeyBoard() {
		if (inputText != null) {
			AppUtil.hideInputMethod(inputText);
		}
	}

	public void showInputKeyBoard() {
		if (inputText != null) {
			AppUtil.showInputMethod(inputText);
		}
	}

	private void hideExpressionView() {
		if (expressionView.getVisibility() != View.GONE) {
			expressionView.setVisibility(View.GONE);
		}
	}

	private void hideAddView() {
		if (addition.getVisibility() != View.GONE) {
			addition.setVisibility(View.GONE);
		}
	}
	


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		finish();
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		msgReading = false;
		hideInputKeyBoard();
		messageAdapter.stopAudioPlaying();
		stopPressTalk();
		
		ChatActivityManager.getInstance().onPause(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ChatActivityManager.getInstance().onResume(this);
		new Thread() {
			public void run() {
				msgReading = true;
				AppUtil.SetActivityVolumeControlStream(ChatActivity.class,
						ChatActivity.this);
				MsgInfoTbl.getInstance().setMsgRead(conversationId,
						System.currentTimeMillis());
				ChatUtil.clearUnreadCount(conversationId);
			}
		}.start();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		ChatActivityManager.getInstance().onRestart(this);
	}

	private int recordStopAction = ChatUtil.AUDIO_RECORD_STOP_ACTION_SEND;
	private volatile float lasty, lastx;
	OnTouchListener onTouch = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getAction();
			// avoid gusture
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				pressTalk.setText(ResString.getString_wm_relax());
				pressTalk.setBackgroundResource(ResDrawable
						.getDrawable_wm_s_n_chat_bottom_input_bg_hover());
				pressTalk.setTextColor(getResources().getColor(
						ResColor.getColor_wm_press_talk_bg_hover()));
				break;
			case MotionEvent.ACTION_MOVE:
				myTouchRelativeLayout.requestDisallowInterceptTouchEvent(true);
				break;
			case MotionEvent.ACTION_CANCEL:
				myTouchRelativeLayout.requestDisallowInterceptTouchEvent(false);
				pressTalk.setText(ResString.getString_wm_press_talk());
				pressTalk.setBackgroundResource(ResDrawable
						.getDrawable_wm_s_n_chat_bottom_input_bg());
				pressTalk.setTextColor(getResources().getColor(
						ResColor.getColor_wm_press_talk_bg_nor()));
				break;
			case MotionEvent.ACTION_UP:
				myTouchRelativeLayout.requestDisallowInterceptTouchEvent(false);
				pressTalk.setText(ResString.getString_wm_press_talk());
				pressTalk.setBackgroundResource(ResDrawable
						.getDrawable_wm_s_n_chat_bottom_input_bg());
				pressTalk.setTextColor(getResources().getColor(
						ResColor.getColor_wm_press_talk_bg_nor()));
				break;
			}

			if (lasty == 0) {
				lasty = event.getY();
			}
			if (lastx == 0) {
				lastx = event.getX();
			}
			if (event.getY() >= 0) {
				if (chatAudioRecordStat1.getVisibility() != View.VISIBLE) {
					chatAudioRecordStat1.setVisibility(View.VISIBLE);
				}
				if (chatAudioRecordStat2.getVisibility() != View.GONE) {
					chatAudioRecordStat2.setVisibility(View.GONE);
				}
				recordStopAction = ChatUtil.AUDIO_RECORD_STOP_ACTION_SEND;
			}
			if (v == pressTalk && action == MotionEvent.ACTION_MOVE) {
				if (event.getY() < 0) {
					if (chatAudioRecordStat1.getVisibility() != View.GONE) {
						chatAudioRecordStat1.setVisibility(View.GONE);
					}
					if (chatAudioRecordStat2.getVisibility() != View.VISIBLE) {
						chatAudioRecordStat2.setVisibility(View.VISIBLE);
					}
					recordStopAction = ChatUtil.AUDIO_RECORD_STOP_ACTION_CANCEL;
				}
				lasty = event.getY();
				lastx = event.getX();
			} else if (v == pressTalk && action == MotionEvent.ACTION_DOWN) {
				synchronized (pressTalk) {
					if (!pressTalk.isSelected()) {
						pressTalk.setSelected(true);
						sendMsg(ChatUtil.MSGT_AUDIO, null, null,null);
					}
				}
			} else if (v == pressTalk && action == MotionEvent.ACTION_UP) {
				if (lastx != event.getX() || lasty != event.getY()) {
					return false;
				}
				stopPressTalk();
			}
			return false;
		}
	};

	private void stopPressTalk() {
		synchronized (pressTalk) {
			if (pressTalk.isSelected()) {
				pressTalk.setSelected(false);
				lasty = 0;
				lastx = 0;
				stopRealTimeRecord(true);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		if (data == null) {
			if (requestCode == RequestCode.CODE_REQUEST_IMG_ORI)
				Log.d(TAG,
						getString(ResString.getString_wm_no_image_select_info()));
			else if (requestCode == RequestCode.CODE_REQUEST_IMG_EDIT) {
				Log.d(TAG, getString(ResString.getString_wm_file_load_fail()));
			}
			return;
		}

		if (UseCameraActivity.GET_IMAGE_REQ == requestCode
				&& resultCode == Activity.RESULT_OK) {
			new AlertDialog.Builder(ChatActivity.this)
					.setMessage(ResString.getString_wm_sure_to_send_img())
					.setPositiveButton(ResString.getString_confirm(),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									final String path = data
											.getStringExtra(UseCameraActivity.IMAGE_PATH);
									sendImage(path);
								}
							})
					.setNegativeButton(ResString.getString_cancel(), null)
					.create().show();
		} else if (requestCode == RequestCode.CODE_REQUEST_IMG_ORI) {
			new AlertDialog.Builder(ChatActivity.this)
					.setMessage(ResString.getString_wm_sure_to_send_img())
					.setPositiveButton(ResString.getString_confirm(),
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									new Thread() {
										public void run() {
											try {
												ArrayList<String> chosen = data
														.getStringArrayListExtra("chosen");
												for (final String path : chosen) {
													try {
														runOnUiThread(new Runnable() {
															public void run() {
																sendImage(path);
															}
														});
														sleep(500);
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
											} catch (Exception e) {
												handler.post(new Runnable() {

													@Override
													public void run() {
														Toast.makeText(
																ChatActivity.this,
																ResString
																		.getString_wm_image_cant_read(),
																Toast.LENGTH_SHORT)
																.show();
													}
												});
											}
										}
									}.start();
								}
							})
					.setNegativeButton(ResString.getString_cancel(), null)
					.create().show();
		}
	}

	/**
	 * 发送图片消息
	 * @param filePath	路径
	 */
	private void sendImage(String filePath) {
		if (filePath != null) {
			sendMsg(ChatUtil.MSGT_PICTURE, null, filePath,null);
		}
	}

	public byte[] genSendImgThumbnail(String filePath) {
		Bitmap bmp = BitmapFactory.decodeFile(filePath);
		bmp = ImageUtil.zoomBitmap(bmp, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
		return ImageUtil.Bitmap2Bytes(bmp);
	}

	boolean cropImageUri(Uri uri) {
		final Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setData(uri);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 140);
		intent.putExtra("outputY", 140);

		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", true);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		try {
			startActivityForResult(intent, RequestCode.CODE_REQUEST_IMG_EDIT);
		} catch (ActivityNotFoundException e) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ChatActivity.this,
							ResString.getString_wm_file_load_fail(),
							Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		}
		return true;
	}

	boolean cropImagePath(String path) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(Uri.fromFile(new File(path)), "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 140);
		intent.putExtra("outputY", 140);
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", true);
		intent.putExtra("scale", true);
		intent.putExtra("scaleUpIfNeeded", true);
		try {
			startActivityForResult(intent, RequestCode.CODE_REQUEST_IMG_EDIT);
		} catch (ActivityNotFoundException e) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(ChatActivity.this,
							ResString.getString_wm_file_load_fail(),
							Toast.LENGTH_SHORT).show();
				}
			});
			return false;
		}
		return true;
	}

	public boolean saveImage(Bitmap bmp, String filePath) throws IOException {
		File myCropFile = new File(filePath);
		if (!myCropFile.exists()) {
			myCropFile.createNewFile();
		}
		bmp = Bitmap.createScaledBitmap(bmp, 140, 140, true);

		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(myCropFile));
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private class RecImpl implements RecordCallBack {
		String spanId;
		String filePath;
		String openSdkContent;

		RecImpl() {

		}

		@Override
		public void recordStartCallback(boolean bstarted) {
			if (bstarted) {
				if (pressTalk.isSelected()) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
						((AudioManager) ChatActivity.this
								.getSystemService(Context.AUDIO_SERVICE))
								.requestAudioFocus(null,
										AudioManager.STREAM_MUSIC,
										AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
					}
					talkSecond = 0;
					setTalkTime();
					handler.post(new Runnable() {

						@Override
						public void run() {
							messageAdapter.stopAudioPlaying();
							chatAudioRecord.setVisibility(View.VISIBLE);
						}
					});

					updateMicStatus(1);
				}
			} else {
				MediaManager.getMediaPlayManager().stopRealTimeRecord();
			}
		}

		@Override
		synchronized public void recordStopCallback(long totalsize, int seqcount) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				((AudioManager) ChatActivity.this
						.getSystemService(Context.AUDIO_SERVICE))
						.abandonAudioFocus(null);
			}
			String msgId = ChatUtil.getMsgId();

			if (tempAudioDatas.size() != 0 && sizes != 0) {
				int seq = 0;
				byte[] array = new byte[sizes];
				int start = 0;
				for (TempAudioData tempAudioDataInfo : tempAudioDatas) {
					System.arraycopy(tempAudioDataInfo.audioData, 0, array,
							start, tempAudioDataInfo.datasize);
					start += tempAudioDataInfo.datasize;
					seq = tempAudioDataInfo.seq;
				}
				
				if(isGroup){
					Sender.sendRealTimeAudioMsg(msgId, spanId, gid, seq, false,
							array, null, ConvType.group);
				} else{
					Sender.sendRealTimeAudioMsg(msgId, spanId, uid, seq, false,
							array, null, ConvType.single);
				}
				sizes = 0;
				tempAudioDatas.clear();
				msgId = ChatUtil.getMsgId();
			}
			if (recordStopAction == ChatUtil.AUDIO_RECORD_STOP_ACTION_CANCEL) {
				if(isGroup){
					Sender.sendRealTimeAudioMsg(msgId, spanId, gid,
							Integer.MAX_VALUE, false, new byte[] { 0 }, null,
							ConvType.group);
				} else{
					Sender.sendRealTimeAudioMsg(msgId, spanId, uid,
							Integer.MAX_VALUE, false, new byte[] { 0 }, null,
							ConvType.single);
				}
				return;
			}

			int audioLength = (int) (Math.ceil(seqcount / 2.0));
			if (seqcount <= 2) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						audioTooShort.setVisibility(View.VISIBLE);
						handler.postDelayed(new Runnable() {

							@Override
							public void run() {
								Animation animation = AnimationUtils
										.loadAnimation(
												ChatActivity.this,
												ResAnim.getAnim_wm_group_noti_hide());
								audioTooShort.startAnimation(animation);
								audioTooShort.setVisibility(View.GONE);
							}
						}, 1000);
					}
				});
				return;
			}

			sendAudioMsgAfterRecord(msgId,filePath,audioLength,
					messageAdapter,recordTime,
					seqcount,spanId,openSdkContent);
		}

		@Override
		public void recordVolumeCallback(long value) {
			int rangeFloor = 28;
			int rangeCeiling = 45;
			int v = (int) (((value - rangeFloor) * 12) / (rangeCeiling - rangeFloor));
			if (v > 12) {
				v = 12;
			} else if (v <= 0) {
				v = 1;
			}
			updateMicStatus(v);

		}

		private int preSeq = 0;
		private int sizes = 0;
		
		@Override
		synchronized public void recordAudioData(byte[] buffer, int size,
				int seq) {
			TempAudioData tempAudioData = new TempAudioData();
			tempAudioData.audioData = new byte[size];
			tempAudioData.datasize = size;
			System.arraycopy(buffer, 0, tempAudioData.audioData, 0, size);
			tempAudioData.seq = seq;
			sizes += size;
			tempAudioDatas.add(tempAudioData);
			if (preSeq == 0 || seq / 2.0 - preSeq / 2.0 == recordTime) {
				byte[] array = new byte[sizes];
				int start = 0;
				for (TempAudioData tempAudioDataInfo : tempAudioDatas) {
					System.arraycopy(tempAudioDataInfo.audioData, 0, array,
							start, tempAudioDataInfo.datasize);
					start += tempAudioDataInfo.datasize;
				}
				preSeq = seq;
				String msgId = ChatUtil.getMsgId();
				if(isGroup){
					Sender.sendRealTimeAudioMsg(msgId, spanId, gid, seq, false,
							array, null, ConvType.group);
				} else{
					Sender.sendRealTimeAudioMsg(msgId, spanId, uid, seq, false,
							array, null, ConvType.single);
				}
				sizes = 0;
				tempAudioDatas.clear();
			}
			if (seq % 2 == 0) {
				talkSecond = seq / 2;
				setTalkTime();
			}
		}

	}

	private double recordTime = 0;

	synchronized public void startRealTimeRecord(final String spanId,String filePath,String openSdkContent) {
		if (recordTime == 0) {
			recordTime = ChatUtil.getRecordTime();
		}

		RecImpl recordCallBack = new RecImpl();
		recordCallBack.filePath = filePath;
		recordCallBack.spanId = spanId;
		recordCallBack.openSdkContent = openSdkContent;
		MediaManager.getMediaPlayManager().setRecordCallBack(recordCallBack);
		MediaManager.getMediaPlayManager().startRealTimeRecord(null, filePath);
	}

	synchronized public void stopRealTimeRecord(boolean delay) {
		long delayTime = 0;
		if (delay) {
			delayTime = 500;
		}
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				MediaManager.getMediaPlayManager().stopRealTimeRecord();
			}
		}, delayTime);
		chatAudioRecord.setVisibility(View.GONE);
	}

	private void setTalkTime() {
		final int minute = talkSecond / AUDIO_RECORD_MAX_SEC;
		int second = talkSecond % AUDIO_RECORD_MAX_SEC;
		final String showTime;
		if (second < 10) {
			showTime = minute + ":0" + second;
		} else {
			showTime = minute + ":" + second;
		}
		handler.post(new Runnable() {
			@Override
			public void run() {
				talkTimeText.setText(showTime);
				if (talkSecond >= AUDIO_RECORD_MAX_SEC) {
					stopRealTimeRecord(false);
				}
			}
		});
	}

	private void updateMicStatus(final int value) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				volImg.getDrawable().setLevel(value);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		saveInputText();
		removeObservers();
		if (messageAdapter != null) {
			messageAdapter.release();
		}
		
		ChatActivityManager.getInstance().onDestroy(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		ChatActivityManager.getInstance().onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ChatActivityManager.getInstance().onStop(this);
	}

	protected void registObservers() {
		msgStatusObserverImpl = new MsgStatusObserverImpl();
		ObserverManager.getInstance().addMsgStatusObserver(
				msgStatusObserverImpl);
		msgClearObserverImpl = new MsgClearObserverImpl();
		ObserverManager.getInstance().addMsgClearObserver(msgClearObserverImpl);
		chatMsgReceiveObserverImpl = new ChatMsgReceiveObserverImpl();
		ObserverManager.getInstance().addChatMsgReceiveObserver(
				chatMsgReceiveObserverImpl);
		msgSendServerInfoChangedObserverImpl = new MsgSendServerInfoChangedObserverImpl();
		ObserverManager.getInstance().addMsgSendServerInfoChangedObserver(
				msgSendServerInfoChangedObserverImpl);
		msgContentChangedObserverImpl = new MsgContentChangedObserverImpl();
		ObserverManager.getInstance().addMsgContentChangedObserver(
				msgContentChangedObserverImpl);
		loginStatusChangedListenerImpl = new LoginStatusChangedListenerImpl();
		ObserverManager.getInstance().addLoginStatusChangedListener(
				loginStatusChangedListenerImpl);
	}

	protected void removeObservers() {
		ObserverManager.getInstance().removeMsgStatusObserver(
				msgStatusObserverImpl);
		ObserverManager.getInstance().removeMsgClearObserver(
				msgClearObserverImpl);
		ObserverManager.getInstance().removeChatMsgReceiveObserver(
				chatMsgReceiveObserverImpl);
		ObserverManager.getInstance().removeMsgSendServerInfoChangedObserver(
				msgSendServerInfoChangedObserverImpl);
		ObserverManager.getInstance().removeMsgContentChangedObserver(
				msgContentChangedObserverImpl);
		ObserverManager.getInstance().removeLoginStatusChangedListener(
				loginStatusChangedListenerImpl);
	}

	class MsgStatusObserverImpl implements MsgStatusObserver {

		@Override
		public void handle(final String msgId, final int status) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					messageAdapter.changeMsgStatus(msgId, status);
				}
			});
		}

	}

	class MsgClearObserverImpl implements MsgClearObserver {

		@Override
		public void handle(String id) {
			if (id.equals(conversationId)) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						messageAdapter.clearData();
					}
				});
			}
		}
	}

	/**
	 * 发送消息
	 * @param msgType
	 * @param subType
	 * @param data
	 * @param openSdkContent
	 */
	public void sendMsg(int msgType, String subType, String data,String openSdkContent) {
		String messageIden = MsgInfo.genNewMessageIden();	//重发时传被重发的messageIden
		hideAddView();
		if (msgType == ChatUtil.MSGT_AUDIO) {
			sendAudioMsg(openSdkContent);
		} else {
			String dest = null;
			if(isGroup){
				dest = gid;
			} else{
				dest = uid;
			}
			ChatUtil.sendMsg(msgType, subType, data, dest, isGroup,
					null, new ChatUtil.SendMsgListener() {

						@Override
						public void handle(MsgInfo msgInfo) {
							messageAdapter.addListData(msgInfo);
						}
					}, null, messageIden,openSdkContent);
		}
	}
	
	/**
	 * 发送自定义消息可以按照以下修改
	 */
	public void sendCustomMsgDemo(){
		WmOpenChatSdk.getInstance().sendCustomMsg(60000, "hello", "123", false, null, new ChatUtil.SendMsgListener() {

			@Override
			public void handle(MsgInfo msgInfo) {
				messageAdapter.addListData(msgInfo);
			}
		});
	}

	// 重发语音
	public void resendAudioMsg(MsgInfo msgInfo) {
		try {
			if (msgInfo.getMsg() != null) {
				File file = new File(msgInfo.getMsg());
				if (file.exists()) {
					byte[] buf = FileUtil.getBytes(msgInfo.getMsg());
					if (buf != null) {
						String spanId = ChatUtil.getMsgId();
						Sender.sendRealTimeAudioMsg(ChatUtil.getMsgId(),
								spanId, uid, 1, false, buf, null,
								ConvType.single);

						String msgId = ChatUtil.getMsgId();
						resendAudioMsg(msgId,msgInfo,messageAdapter,spanId,null);

					}
				} else {
					handler.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(ChatActivity.this,
									ResString.getString_wm_file_not_exist(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送语音消息
	 * @param openSdkContent
	 */
	private void sendAudioMsg(String openSdkContent) {
		String spanId = ChatUtil.getMsgId();
		startRealTimeRecord(spanId,FileUtil.getAudioPath(spanId),openSdkContent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (expressionView.getVisibility() == View.VISIBLE
					|| addition.getVisibility() == View.VISIBLE) {
				hideChatExcessView();
				return true;
			}
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	class ChatMsgReceiveObserverImpl implements ChatMsgReceiveObserver {
		
		@Override
		public void handleTypeGroupSystem(String gid, final MsgInfo msgInfo) {
			if(isGroup){//群聊
				gid = !gid.startsWith("G") ? gid : GroupIdConv.gidTouid(gid);
				if(gid.equals(ChatActivity.this.gid)){
					handler.post(new Runnable() {

						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);

						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleTypeCommonTextOrMixText(String fromId,
				final MsgInfo msgInfo) {
			if(!isGroup){//单聊
				if (fromId.equals(uid)
						|| fromId.equals(LoginManager.loginUserInfo.getId())) {
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);
							if (msgReading) {
								ChatUtil.clearUnreadCount(conversationId);
							}
						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleTypeGroupTextOrMixText(String gid, final MsgInfo msgInfo) {
			if(isGroup){//群聊
				gid = !gid.startsWith("G") ? gid : GroupIdConv.gidTouid(gid);
				if(gid.equals(ChatActivity.this.gid)){
					handler.post(new Runnable() {

						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);
							if (msgReading) {
								ChatUtil.clearUnreadCount(conversationId);
							}
						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleTypeCommonAudio(String fromId, final MsgInfo msgInfo) {
			if(!isGroup){//单聊
				if (fromId.equals(uid)
						|| fromId.equals(LoginManager.loginUserInfo.getId())) {
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);
							if (msgReading) {
								ChatUtil.clearUnreadCount(conversationId);
							}
						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleTypeGroupAudio(String gid, final MsgInfo msgInfo) {
			if(isGroup){//群聊
				gid = !gid.startsWith("G") ? gid : GroupIdConv.gidTouid(gid);
				if(gid.equals(ChatActivity.this.gid)){
					handler.post(new Runnable() {

						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);
							if (msgReading) {
								ChatUtil.clearUnreadCount(conversationId);
							}
						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleTypeCommonFile(String fromId, final MsgInfo msgInfo) {
			if(!isGroup){//单聊
				if (fromId.equals(uid)
						|| fromId.equals(LoginManager.loginUserInfo.getId())) {
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);
							if (msgReading) {
								ChatUtil.clearUnreadCount(conversationId);
							}
						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleTypeGroupFile(String gid, final MsgInfo msgInfo) {
			if(isGroup){//群聊
				gid = !gid.startsWith("G") ? gid : GroupIdConv.gidTouid(gid);
				if(gid.equals(ChatActivity.this.gid)){
					handler.post(new Runnable() {

						@Override
						public void run() {
							boolean checkRepeat = false;
							if (inTime > 0
									&& System.currentTimeMillis() - inTime < MessageAdapter.CHECK_REPEAT_TIME) {
								checkRepeat = true;
							}
							messageAdapter.addListData(msgInfo, checkRepeat);
							if (msgReading) {
								ChatUtil.clearUnreadCount(conversationId);
							}
						}
					});
					MsgInfoTbl.getInstance().setMsgRead(conversationId,
							msgInfo.getMsg_id(), System.currentTimeMillis());
				}
			}
		}

		@Override
		public void handleGroupMsgInfos(String gid, final List<MsgInfo> msgInfos) {
			if(isGroup){//群聊
				gid = !gid.startsWith("G") ? gid : GroupIdConv.gidTouid(gid);
				if(gid.equals(ChatActivity.this.gid)){
					runOnUiThread(new Runnable() {
						public void run() {
							setRefreshingFinish();
							String firstMsgId = messageAdapter.getEarliestMsgId();
							messageAdapter.addHistoryMsgInfos(msgInfos);
							if (firstMsgId != null) {
								int pos = messageAdapter.getPosition(firstMsgId);
								if (pos > 0) {
									MsgInfo msgInfo = (MsgInfo) messageAdapter
											.getItem(pos);
									if (msgInfo.getMsg_type() != ChatUtil.MSGT_TIME) {
										MsgInfo msgInfo2 = (MsgInfo) messageAdapter
												.getItem(pos - 1);
										if (msgInfo2.getMsg_type() == ChatUtil.MSGT_TIME) {
											--pos;
										}
									}
								}
								messageList.setSelection(pos);
							}
						}
					});
				}
			}
		}

		@Override
		public void handleSingleMsgInfos(String fromId,
				final List<MsgInfo> msgInfos) {
			if(!isGroup){//单聊
				if (fromId.equals(uid)
						|| fromId.equals(LoginManager.loginUserInfo.getId())) {
					runOnUiThread(new Runnable() {
						public void run() {
							setRefreshingFinish();
							String firstMsgId = messageAdapter.getEarliestMsgId();
							messageAdapter.addHistoryMsgInfos(msgInfos);
							if (firstMsgId != null) {
								int pos = messageAdapter.getPosition(firstMsgId);
								if (pos > 0) {
									MsgInfo msgInfo = (MsgInfo) messageAdapter
											.getItem(pos);
									if (msgInfo.getMsg_type() != ChatUtil.MSGT_TIME) {
										MsgInfo msgInfo2 = (MsgInfo) messageAdapter
												.getItem(pos - 1);
										if (msgInfo2.getMsg_type() == ChatUtil.MSGT_TIME) {
											--pos;
										}
									}
								}
								messageList.setSelection(pos);
							}
							
						}
					});
				}
			}
		}
	}

	class MsgSendServerInfoChangedObserverImpl implements
			MsgSendServerInfoChangedObserver {

		@Override
		public void handle(String msgId, long sendTime, String serverMsgId,
				long orderNum) {
			if (messageAdapter != null) {
				messageAdapter.changeMsgSendServerInfo(msgId, sendTime,
						serverMsgId, orderNum);
			}
		}

	}

	class MsgContentChangedObserverImpl implements MsgContentChangedObserver {

		@Override
		public void handle(final String msgId, final String msgContent) {
			handler.post(new Runnable() {

				@Override
				public void run() {
					messageAdapter.replaceMsgContent(msgId, msgContent);
				}
			});
		}

	}

	public void setBlackViewVisibility(int visibility) {
		blackView.setVisibility(visibility);
	}

	private void back() {
		hideInputKeyBoard();
		ChatActivity.this.finish();
	}

	public class LoginStatusChangedListenerImpl implements
			LoginStatusChangedListener {

		@Override
		public void changed(int loginStatus) {
			runOnUiThread(new Runnable() {
				public void run() {
					setTitle();
				}
			});
		}

	}
}
