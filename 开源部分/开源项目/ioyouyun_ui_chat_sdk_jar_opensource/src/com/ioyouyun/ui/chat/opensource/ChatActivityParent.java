package com.ioyouyun.ui.chat.opensource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.core.Consts;
import com.ioyouyun.ui.chat.core.chat.ChatMsgReceiveManager;
import com.ioyouyun.ui.chat.core.chat.MsgSendStatManager;
import com.ioyouyun.ui.chat.core.chat.Sender;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil;
import com.ioyouyun.ui.chat.core.login.LoginManager;
import com.ioyouyun.ui.chat.db.tables.MsgInfoTbl;
import com.ioyouyun.ui.chat.ui.abstractcommponts.ParentActivity;
import com.ioyouyun.ui.chat.ui.adapter.ExpEmojiViewPagerAdapter;
import com.ioyouyun.ui.chat.util.DeviceUtil;
import com.ioyouyun.wchat.GroupIdConv;
import com.ioyouyun.wchat.WeimiInstance;
import com.ioyouyun.wchat.message.AudioMessage;
import com.ioyouyun.wchat.message.ConvType;
import com.ioyouyun.wchat.message.FileMessage;
import com.ioyouyun.wchat.message.HistoryMessage;
import com.ioyouyun.wchat.message.NoticeType;
import com.ioyouyun.wchat.message.TextMessage;
import com.ioyouyun.wchat.util.HttpCallback;

public class ChatActivityParent extends ParentActivity {
	
	public final String TAG = ChatActivityParent.class.getSimpleName();
	
	// 发�?图片的缩略图大小
	public final static int THUMBNAIL_WIDTH = DeviceUtil.dip2px(
			LoginManager.applicationContext, 100);
	public final static int THUMBNAIL_HEIGHT = DeviceUtil.dip2px(
			LoginManager.applicationContext, 100);

	public final int AUDIO_RECORD_MAX_SEC = Consts
			.getConfigMaxAudioRecordSec();

	public Handler handler;

	public long inTime;
	public boolean listRefreshing = false;
	public ExpEmojiViewPagerAdapter expEmojiViewPagerAdapter;
	public int talkSecond;
	public boolean msgReading = false;
	public String currentWhisperShowPicMsgId;
	
	/** 单聊uid 不为null则为单聊 */
	public String uid = null;
	
	/** 群聊gid 不为null则为群聊 */
	public String gid = null;
	
	/** 会话id 如果是单聊 则等于uid 如果是群聊 则将gid改成G开头的字符串形式 */
	public String conversationId;
	
	/** 用于判断是否是单聊/群聊 */
	public boolean isGroup = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if(!LoginManager.isOnLine()){
//			finish();
//		}
		handler = new Handler();
		inTime = System.currentTimeMillis();
		Intent intent = getIntent();
		uid = intent.getStringExtra("uid");
		if(uid != null){							//单聊
			conversationId = uid;
			isGroup = false;
		} else{										//群聊
			gid = intent.getStringExtra("gid");
			if(gid == null){
				finish();
			}
			isGroup = true;
			conversationId = GroupIdConv.uidTogid(gid);
		}
	}
	
	public void startLoadMore(
			final Object messageAdapter,//final MessageAdapter messageAdapter,
			final String conversationId,
			final int pagesize,
			final boolean loadFromServer,
			final String uid,
			final Object chatActivity,//final ChatActivity chatActivity,
			final Method needGotoLoadHistoryMethod,
			final Method handleAddLocalMsgInfosMethod,
			final Method setRefreshingFinishMethod){
		new Thread() {
			@Override
			public void run() {
				final MsgInfo earliestMsgInfo = ((MessageParentAdapter)messageAdapter).getEarliestMsg();
				final List<MsgInfo> infos = MsgInfoTbl.getInstance()
						.getMsgInfos(
								conversationId,
								pagesize,
								earliestMsgInfo != null ? earliestMsgInfo
										.getMsg_id() : null,
								earliestMsgInfo != null ? earliestMsgInfo
										.getTimestamp() : null,
								earliestMsgInfo != null ? earliestMsgInfo.getOrderNum():MsgInfo.ORDER_NUM_DEFAULT,	
								true);
				boolean gotoLoadHistroy = false;
				if (loadFromServer
						&& ChatUtil.OPEN_PULL_HISTORY_MSG
						&& DeviceUtil
								.isConnectingToInternet(LoginManager.applicationContext)) {
					try {
						gotoLoadHistroy = (Boolean)needGotoLoadHistoryMethod.invoke(chatActivity, infos);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
				if (!gotoLoadHistroy) {
					if (infos.size() > 0) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									handleAddLocalMsgInfosMethod.invoke(chatActivity, infos);
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								}
							}
						});
					} else {
						handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									setRefreshingFinishMethod.invoke(chatActivity);
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								}
							}
						});
					}
				} else {

					long t = System.currentTimeMillis() / 1000;

					if (earliestMsgInfo != null) {
						String timestamp = "";
						try {
							timestamp = earliestMsgInfo.getTimestamp()
									.substring(
											0,
											earliestMsgInfo.getTimestamp()
													.length() - 3);// 秒级
						} catch (Exception e) {
						}
						if (!timestamp.equals("")) {
							t = Long.parseLong(timestamp);
						}
					}
					ConvType convType = isGroup?ConvType.group:ConvType.single;
					final String id = isGroup?gid:uid;
					WeimiInstance.getInstance().shortGetHistoryByTime(id,
							t, pagesize,convType, new HttpCallback() {

								@Override
								public void onResponseHistory(List arg0) {
									List<HistoryMessage> messages = (ArrayList<HistoryMessage>) arg0;
									boolean needHandleHistoryMessages = true;
//									Log.e(TAG, "arg0.size():"+arg0.size());
									if (infos.size() == 0) {// 1、如果本地临时取的消息数量为0
															// 则服务器的消息merge进来
										needHandleHistoryMessages = true;
									} else {
										long lastOldMsg = -1;
										for (int i = infos.size() - 1; i >= 0; i--) {
											if (infos.get(i).getOrderNum() <= 0) {
												try {
													lastOldMsg = Long
															.parseLong(infos
																	.get(i)
																	.getTimestamp());
													break;
												} catch (Exception e) {

												}
											}
										}
										if (lastOldMsg == -1) {// 2、如果本地临时取的全是新版消�?
																// 那么服务器获取的消息执行merge
											needHandleHistoryMessages = true;
										} else {// 3、如果本地区的临时消息中�?���?��旧消息的时间在从服务器获取的新消息的�?��时间还要�?
												// 那么merge否则丢弃服务器消息采用本地旧消息
											long serverOldestTime = -1;
											for (int i = 0; i < messages
													.size(); i++) {
												HistoryMessage historyMessage = messages
														.get(i);
												long time = -1;
												if (historyMessage.type == NoticeType.textmessage
														|| historyMessage.type == NoticeType.mixedtextmessage) {
													time = ((TextMessage) historyMessage.message).time;
												} else if (historyMessage.type == NoticeType.audiomessage) {
													time = ((AudioMessage) historyMessage.message).time;
												} else if (historyMessage.type == NoticeType.filemessage) {
													time = ((FileMessage) historyMessage.message).time;
												}
												if (serverOldestTime == -1) {
													serverOldestTime = time;
												} else {
													if (time < serverOldestTime) {
														serverOldestTime = time;
													}
												}
											}

											if (serverOldestTime > lastOldMsg) {
												needHandleHistoryMessages = true;
											} else {
												needHandleHistoryMessages = false;
											}
										}
									}
									if (needHandleHistoryMessages) {
										ChatMsgReceiveManager
												.handleHistoryMessages(
														messages, isGroup,
														id);
									} else {
										try {
											handleAddLocalMsgInfosMethod.invoke(chatActivity, infos);
										} catch (IllegalArgumentException e) {
											e.printStackTrace();
										} catch (IllegalAccessException e) {
											e.printStackTrace();
										} catch (InvocationTargetException e) {
											e.printStackTrace();
										}
									}
									handler.post(new Runnable() {

										@Override
										public void run() {
											try {
												setRefreshingFinishMethod.invoke(chatActivity);
											} catch (IllegalArgumentException e) {
												e.printStackTrace();
											} catch (IllegalAccessException e) {
												e.printStackTrace();
											} catch (InvocationTargetException e) {
												e.printStackTrace();
											}
										}
									});
								}

								@Override
								public void onResponse(String arg0) {
								}

								@Override
								public void onError(Exception arg0) {
								}
							}, 10);
				}
			}
		}.start();
	}
	


	public class TempAudioData {
		public byte[] audioData;
		public int datasize;
		public int seq;
	}
	
	public List<TempAudioData> tempAudioDatas = new ArrayList<TempAudioData>();
	
	public void sendAudioMsgAfterRecord(String msgId,String filePath,int audioLength,final Object messageAdapter,double recordTime,
			int seqcount,String spanId,String openSdkContent){
		String audioLengthStr = String.valueOf(audioLength);
		String messageIden = MsgInfo.genNewMessageIden();
		String dest = null;
		if(isGroup){
			dest = GroupIdConv.uidTogid(gid);
		} else{
			dest = uid;
		}
		int temp = (int) (recordTime / 0.5);
		String extendMsg = ChatUtil.getAudioMsgExtendInfo(
				messageIden, null, "amr", audioLength,
				((int) Math.ceil((seqcount - 1) * 1.0 / temp)) + 2,
				seqcount,openSdkContent);

		handleBeforeSendAudioMsg(dest, msgId, filePath, audioLengthStr, conversationId, messageAdapter,messageIden,extendMsg);
		
		if(isGroup){
			Sender.sendRealTimeAudioMsg(msgId, spanId, gid, seqcount + 1, true,
					new byte[] { 0 }, extendMsg != null ? extendMsg.getBytes()
							: null, ConvType.group);
		} else{
			Sender.sendRealTimeAudioMsg(msgId, spanId, uid, seqcount + 1, true,
					new byte[] { 0 }, extendMsg != null ? extendMsg.getBytes()
							: null, ConvType.single);
		}
	}
	
	public void resendAudioMsg(String msgId,MsgInfo oldMsgInfo,Object messageAdapter,String spanId,String openSdkContent){
		String dest = null;
		if(isGroup){
			dest = GroupIdConv.uidTogid(gid);
		} else{
			dest = uid;
		}
		String extendMsg = ChatUtil.getAudioMsgExtendInfo(
				oldMsgInfo.getMessageIden(), null, "amr",
				Integer.parseInt(oldMsgInfo.getExtra()), 2, 1,openSdkContent);

		handleBeforeSendAudioMsg(dest, msgId, oldMsgInfo.getMsg(), oldMsgInfo.getExtra(), conversationId, messageAdapter, oldMsgInfo.getMessageIden(),extendMsg);
		
		if(isGroup){
			Sender.sendRealTimeAudioMsg(msgId, spanId, gid, 2, true,
					new byte[] { 0 }, extendMsg != null ? extendMsg.getBytes()
							: null, ConvType.group);
		} else{
			Sender.sendRealTimeAudioMsg(msgId, spanId, uid, 2, true,
					new byte[] { 0 }, extendMsg != null ? extendMsg.getBytes()
							: null, ConvType.single);
		}
	}
	
	/**
	 * 
	 * @param dest				接收人id/接收G开头群id
	 * @param msgId
	 * @param filePath
	 * @param audioLengthStr
	 * @param conversationId
	 * @param messageAdapter
	 * @param messageIden
	 * @param extendMsg
	 */
	public void handleBeforeSendAudioMsg(String dest,String msgId,String filePath,
			String audioLengthStr,String conversationId,final Object messageAdapter,
			String messageIden,String extendMsg){
		long sendTime = System.currentTimeMillis();
		final MsgInfo info = new MsgInfo();
		info.setFrom_id(LoginManager.loginUserInfo.getId());
		info.setTo_id(dest);
		info.setMsg_type(ChatUtil.MSGT_AUDIO);
		info.setMsg_id(msgId);
		info.setMsg(filePath);
		info.setExtra(audioLengthStr);// 语音长度
		info.setTimestamp(sendTime + "");
		info.setSend_status(MsgInfo.SEND_STATUS_UNKNOW);
		info.setProcessed(MsgInfo.PROCESSED_UNDONE);
		info.setAudio_readtime(sendTime + "");
		info.setPicPraised(MsgInfo.PIC_PRAISED_DEFAULT);
		info.setReadTime(sendTime);
		info.setMessageIden(messageIden);
		info.setFlag(ChatUtil
				.getConversationFlagFromMapCache(conversationId));
		info.setExtendMsg(extendMsg);
		MsgInfoTbl.getInstance().addMsg(info, conversationId);
		ChatUtil.checkAndUpdateCommonConversation(conversationId, info,
				false, false);

		MsgSendStatManager.getInstance().putMsgId(msgId);
		handler.post(new Runnable() {

			@Override
			public void run() {
				((MessageParentAdapter)messageAdapter).addListData(info);
			}
		});
	}
	
}
