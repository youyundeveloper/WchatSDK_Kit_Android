package com.ioyouyun.ui.chat.opensource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.ioyouyun.ui.chat.api.WmOpenChatSdk;
import com.ioyouyun.ui.chat.bean.Conversation;
import com.ioyouyun.ui.chat.bean.GroupInfo;
import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.bean.Suser;
import com.ioyouyun.ui.chat.bean.TargetSetting;
import com.ioyouyun.ui.chat.bean.UserInfo;
import com.ioyouyun.ui.chat.core.CacheManager;
import com.ioyouyun.ui.chat.core.Consts;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil;
import com.ioyouyun.ui.chat.core.login.LoginManager;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.AddOrUpdateConversationObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.ClearConversationObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.ConversationMsgChangeObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.ConversationRefreshObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.ConversationUnreadCountRefreshObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.ConversationUpdateObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.DeleteConversationObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.DraftContentChangedObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.FirstReadAudioMsgObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.GroupCat1UpdateObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.GroupNameUpdateObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgSendServerInfoChangedObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgStatusObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.RemindChangeObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.UnreadCountClearObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.UnreadMsgSticktimeChangeObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.UpdateMarkSuccessObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverManager;
import com.ioyouyun.ui.chat.db.tables.ConversationTbl;
import com.ioyouyun.ui.chat.db.tables.GroupInfoTbl;
import com.ioyouyun.ui.chat.db.tables.MsgInfoTbl;
import com.ioyouyun.ui.chat.db.tables.SuserTbl;
import com.ioyouyun.ui.chat.db.tables.TargetSettingTbl;
import com.ioyouyun.ui.chat.db.tables.UserInfoTbl;
import com.ioyouyun.ui.chat.lib.ResString;
import com.ioyouyun.ui.chat.ui.adapter.ParentAdapter;
import com.ioyouyun.wchat.GroupIdConv;
import com.ioyouyun.wchat.message.WeimiNotice;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class ConversationParentAdapter extends ParentAdapter implements
		OnItemClickListener, OnItemLongClickListener {

	public static int USE_TYPE_1 = 1; // 显示全部
	public static int USE_TYPE_2 = 2; // 折叠部分消息进入消息�?
	public static int USE_TYPE_3 = 3; // 只显示屏蔽群消息

	protected List<Conversation> mConversations = new ArrayList<Conversation>();
	protected DisplayImageOptions userAvatarLoadOptions;
	protected DisplayImageOptions groupAvatarLoadOptions;
	protected LayoutInflater mLayoutInflater;
	protected int useType;
	protected Handler mHandler;

	protected FirstReadAudioMsgObserverImpl firstReadAudioMsgObserverImpl;
	protected UnreadMsgSticktimeChangeObserverImpl unreadMsgSticktimeChangeObserverImpl;
	protected ClearConversationObserverImpl clearConversationObserverImpl;
	protected GroupNameUpdateObserverImpl groupNameUpdateObserverImpl;
	protected GroupCat1UpdateObserverImpl groupCat1UpdateObserverImpl;
	protected UpdateMarkSuccessObserverImpl updateMarkSuccessObserverImpl;
	protected RemindChangeObserverImpl remindChangeObserverImpl;
	protected ConversationUpdateObserverImpl conversationUpdateObserverImpl;
	protected MsgSendServerInfoChangedObserverImpl msgSendTimeChangedObserverImpl;
	protected AddOrUpdateConversationObserverImpl addOrUpdateConversationObserverImpl;
	protected DeleteConversationObserverImpl deleteConversationObserverImpl;
	protected DraftContentChangedObserverImpl draftContentChangedObserverImpl;
	protected MsgStatusObserverImpl msgStatusObserverImpl;
	protected ConversationUnreadCountRefreshObserverImpl conversationUnreadCountRefreshObserverImpl;
	protected ConversationRefreshObserverImpl conversationRefreshObserverImpl;
	protected ConversationMsgChangeObserverImpl conversationMsgChangeObserverImpl;
	protected UnreadCountClearObserverImpl unreadCountClearObserverImpl;

	public ConversationParentAdapter(Activity activity) {
		super(activity);
		mLayoutInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHandler = new Handler();
		initImgLoadOptions();
		addObservers();
	}

	protected void initImgLoadOptions() {
		userAvatarLoadOptions = new DisplayImageOptions.Builder().cacheOnDisc()
				.cacheInMemory()
				.showImageForEmptyUri(Consts.USER_AVATAR_RES_DEFAULT)
				.showStubImage(Consts.USER_AVATAR_RES_DEFAULT)
				.showImageOnFail(Consts.USER_AVATAR_RES_DEFAULT)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
		groupAvatarLoadOptions = new DisplayImageOptions.Builder()
				.cacheOnDisc().cacheInMemory()
				.showImageForEmptyUri(Consts.GROUP_AVATAR_RES_DEFAULT)
				.showStubImage(Consts.GROUP_AVATAR_RES_DEFAULT)
				.showImageOnFail(Consts.GROUP_AVATAR_RES_DEFAULT)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	protected void addObservers() {
		firstReadAudioMsgObserverImpl = new FirstReadAudioMsgObserverImpl();
		ObserverManager.getInstance().addFirstReadAudioMsgObserver(
				firstReadAudioMsgObserverImpl);
		unreadMsgSticktimeChangeObserverImpl = new UnreadMsgSticktimeChangeObserverImpl();
		ObserverManager.getInstance().addUnreadMsgSticktimeChangeObserver(
				unreadMsgSticktimeChangeObserverImpl);
		clearConversationObserverImpl = new ClearConversationObserverImpl();
		ObserverManager.getInstance().addClearConversationObserver(
				clearConversationObserverImpl);
		groupNameUpdateObserverImpl = new GroupNameUpdateObserverImpl();
		ObserverManager.getInstance().addGroupNameUpdateObserver(
				groupNameUpdateObserverImpl);
		groupCat1UpdateObserverImpl = new GroupCat1UpdateObserverImpl();
		ObserverManager.getInstance().addGroupCat1UpdateObserver(
				groupCat1UpdateObserverImpl);
		updateMarkSuccessObserverImpl = new UpdateMarkSuccessObserverImpl();
		ObserverManager.getInstance().addUpdateMarkSuccessObserver(
				updateMarkSuccessObserverImpl);
		remindChangeObserverImpl = new RemindChangeObserverImpl();
		ObserverManager.getInstance().addRemindChangeObserver(
				remindChangeObserverImpl);
		conversationUpdateObserverImpl = new ConversationUpdateObserverImpl();
		ObserverManager.getInstance().addConversationUpdateObserver(
				conversationUpdateObserverImpl);
		msgSendTimeChangedObserverImpl = new MsgSendServerInfoChangedObserverImpl();
		ObserverManager.getInstance().addMsgSendServerInfoChangedObserver(
				msgSendTimeChangedObserverImpl);
		addOrUpdateConversationObserverImpl = new AddOrUpdateConversationObserverImpl();
		ObserverManager.getInstance().addAddOrUpdateConversationObserver(
				addOrUpdateConversationObserverImpl);
		deleteConversationObserverImpl = new DeleteConversationObserverImpl();
		ObserverManager.getInstance().addDeleteConversationObserver(
				deleteConversationObserverImpl);
		draftContentChangedObserverImpl = new DraftContentChangedObserverImpl();
		ObserverManager.getInstance().addDraftContentChangedObserver(
				draftContentChangedObserverImpl);
		msgStatusObserverImpl = new MsgStatusObserverImpl();
		ObserverManager.getInstance().addMsgStatusObserver(
				msgStatusObserverImpl);
		conversationUnreadCountRefreshObserverImpl = new ConversationUnreadCountRefreshObserverImpl();
		ObserverManager.getInstance()
				.addConversationUnreadCountRefreshObserver(
						conversationUnreadCountRefreshObserverImpl);
		conversationRefreshObserverImpl = new ConversationRefreshObserverImpl();
		ObserverManager.getInstance().addConversationRefreshObserver(
				conversationRefreshObserverImpl);
		conversationMsgChangeObserverImpl = new ConversationMsgChangeObserverImpl();
		ObserverManager.getInstance().addConversationMsgChangeObserver(
				conversationMsgChangeObserverImpl);
		unreadCountClearObserverImpl = new UnreadCountClearObserverImpl();
		ObserverManager.getInstance().addUnreadCountClearObserver(
				unreadCountClearObserverImpl);
	}

	public void onDestroy() {
		removeObservers();
	}

	protected void removeObservers() {
		ObserverManager.getInstance().removeFirstReadAudioMsgObserver(
				firstReadAudioMsgObserverImpl);
		ObserverManager.getInstance().removeUnreadMsgSticktimeChangeObserver(
				unreadMsgSticktimeChangeObserverImpl);
		ObserverManager.getInstance().removeClearConversationObserver(
				clearConversationObserverImpl);
		ObserverManager.getInstance().removeGroupNameUpdateObserver(
				groupNameUpdateObserverImpl);
		ObserverManager.getInstance().removeGroupCat1UpdateObserver(
				groupCat1UpdateObserverImpl);
		ObserverManager.getInstance().removeUpdateMarkSuccessObserver(
				updateMarkSuccessObserverImpl);
		ObserverManager.getInstance().removeRemindChangeObserver(
				remindChangeObserverImpl);
		ObserverManager.getInstance().removeConversationUpdateObserver(
				conversationUpdateObserverImpl);
		ObserverManager.getInstance().removeMsgSendServerInfoChangedObserver(
				msgSendTimeChangedObserverImpl);
		ObserverManager.getInstance().removeAddOrUpdateConversationObserver(
				addOrUpdateConversationObserverImpl);
		ObserverManager.getInstance().removeDeleteConversationObserver(
				deleteConversationObserverImpl);
		ObserverManager.getInstance().removeDraftContentChangedObserver(
				draftContentChangedObserverImpl);
		ObserverManager.getInstance().removeMsgStatusObserver(
				msgStatusObserverImpl);
		ObserverManager.getInstance()
				.removeConversationUnreadCountRefreshObserver(
						conversationUnreadCountRefreshObserverImpl);
		ObserverManager.getInstance().removeConversationRefreshObserver(
				conversationRefreshObserverImpl);
		ObserverManager.getInstance().removeConversationMsgChangeObserver(
				conversationMsgChangeObserverImpl);
		ObserverManager.getInstance().removeUnreadCountClearObserver(
				unreadCountClearObserverImpl);
	}

	@Override
	public int getCount() {
		return mConversations.size();
	}

	@Override
	public Object getItem(int position) {
		return mConversations.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	protected void clearData() {
		mConversations.clear();
	}

	synchronized public void setAllConversationData(
			List<Conversation> conversations) {
		clearData();
		for (Conversation conversation : conversations) {
			boolean audioIsRead = true;
			if (conversation.getMsg_type() == 4
					&& conversation.getMsgId() != null) {// 语音�?���?��下已读未�?

				MsgInfo msgInfo = MsgInfoTbl.getInstance().getMsgInfo(
						conversation.getMsgId(), conversation.getId());
				if (msgInfo != null) {
					String readtime = msgInfo.getAudio_readtime();
					if (readtime == null || readtime.equals("")) {
						audioIsRead = false;
					}
				}
			}
			conversation.setAudioIsRead(audioIsRead);
		}
		for (int i = conversations.size() - 1; i >= 0; i--) {
			if (ChatUtil.isConversationHide(conversations.get(i).getId())) {
				conversations.remove(i);
				continue;
			}
			if (useType == USE_TYPE_2) {
				if (!conversations.get(i).getParent()
						.equals(Conversation.PARENT_TOP)) {
					conversations.remove(i);
				}
			} else if (useType == USE_TYPE_3) {
				if (!conversations.get(i).getParent()
						.equals(ChatUtil.BOX_SHIELD_GROUPS_ID)) {
					conversations.remove(i);
				}
			}
		}
		mConversations.addAll(conversations);
		notifyDataSetChanged();
	}

	synchronized public void calculateAndRefreshAllUnreadCount() {
		if (useType == USE_TYPE_2) {
			int count = 0;
			for (int i = 0; i < mConversations.size(); i++) {
				if (mConversations.get(i).getId().startsWith("B")) {
					continue;
				}
				if (mConversations.get(i).getCount() > 0) {
					count += mConversations.get(i).getCount();
				}
			}
			ObserverManager.getInstance().notifyMsgUnreadCountUpdateObservers(
					count);
		}
	}

	private void handleGetUserInfo(WeimiNotice notice) {
		try {
			JSONObject object = new JSONObject(notice.getObject().toString());
			if ("1".equals(object.getString(Consts.APISTATUS))) {
				JSONObject result = object.getJSONObject(Consts.RESULT);

				UserInfo userInfo = new UserInfo();
				userInfo = UserInfo.getUserInfo(result, UserInfo.class);

				if (userInfo != null) {
					UserInfo dbUserInfo = UserInfoTbl.getInstance().getUser(
							userInfo.getId());
					if (dbUserInfo == null) {
						UserInfoTbl.getInstance().replaceUser(userInfo);
						CacheManager.userCache.put(userInfo.getId(), userInfo);
					} else {
						dbUserInfo.setId(userInfo.getId());
						dbUserInfo.setGender(userInfo.getGender());
						dbUserInfo.setAvatar(userInfo.getAvatar());
						dbUserInfo.setNickname(userInfo.getNickname());
						dbUserInfo.setDescription(userInfo.getDescription());
						UserInfoTbl.getInstance().replaceUser(dbUserInfo);
						CacheManager.userCache.put(dbUserInfo.getId(),
								dbUserInfo);
					}
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleGetGroupInfo(WeimiNotice notice) {
		try {
			JSONObject object = new JSONObject(notice.getObject().toString());
			if ("1".equals(object.getString(Consts.APISTATUS))) {
				JSONObject result = object.getJSONObject(Consts.RESULT);
				GroupInfo groupInfo = GroupInfo.getGroupInfo(result,
						GroupInfo.class);
				if (groupInfo != null) {
					CacheManager.groupCache.put(groupInfo.getGid(), groupInfo);

					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();
						}
					});
					GroupInfo dbGroupInfo = GroupInfoTbl.getInstance()
							.getGroup(groupInfo.getGid());
					if (dbGroupInfo == null) {
						GroupInfoTbl.getInstance().replaceGroup(groupInfo);
					} else {
						dbGroupInfo.setGid(groupInfo.getGid());
						dbGroupInfo.setGeo(groupInfo.getGeo());
						dbGroupInfo.setLevel(groupInfo.getLevel());
						dbGroupInfo.setCat2(groupInfo.getCat2());
						dbGroupInfo.setMaxMembers(groupInfo.getMaxMembers());
						dbGroupInfo.setName(groupInfo.getName());
						dbGroupInfo.setAvatar(groupInfo.getAvatar());
						dbGroupInfo.setMembers(groupInfo.getMembers());
						dbGroupInfo.setIntra(groupInfo.getIntra());
						dbGroupInfo.setCat1(groupInfo.getCat1());
						dbGroupInfo.setSettings(groupInfo.getSettings());
						GroupInfoTbl.getInstance().replaceGroup(dbGroupInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleGetSysUserInfo(WeimiNotice notice) {
		try {
			JSONObject object = new JSONObject(notice.getObject().toString());
			if ("1".equals(object.getString(Consts.APISTATUS))) {
				JSONObject result = object.getJSONObject(Consts.RESULT);
				final Suser suser = Suser.getSuserFromResultJson(result);
				if (suser != null) {
					CacheManager.suserCache.put(suser.id, suser);
					SuserTbl.getInstance().addSuser(suser);
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							notifyDataSetChanged();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class FirstReadAudioMsgObserverImpl implements FirstReadAudioMsgObserver {

		@Override
		public void handle(final String msgId, final String readTime) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					setAudioRead(msgId);
				}
			});
		}

	}

	synchronized public void setAudioRead(String msgId) {
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getMsgId() != null
					&& mConversations.get(i).getMsgId().equals(msgId)) {
				if (mConversations.get(i).isAudioIsRead() == false) {
					mConversations.get(i).setAudioIsRead(true);
					notifyDataSetChanged();
				}
				break;
			}
		}
	}

	class UnreadMsgSticktimeChangeObserverImpl implements
			UnreadMsgSticktimeChangeObserver {

		@Override
		public void handle(final String id, final String time) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					changeSticktime(id, time);
				}
			});
		}

	}

	synchronized public void changeSticktime(String conversationId,
			String sticktime) {
		Conversation conversation = null;
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				mConversations.get(i).setSticktime(sticktime);
				conversation = mConversations.get(i);
				mConversations.remove(i);
				break;
			}
		}
		if (conversation != null) {
			addOrUpdataConversation(conversation);
		}
	}

	synchronized public void addOrUpdataConversation(Conversation conversation) {
		if (conversation != null) {
			if (ChatUtil.isConversationHide(conversation.getId())) {// 排除不显示的会话
				return;
			}

			if (useType == USE_TYPE_2) {
				if (!conversation.getParent().equals(Conversation.PARENT_TOP)) {
					return;
				}
			} else if (useType == USE_TYPE_3) {
				if (!conversation.getParent().equals(
						ChatUtil.BOX_SHIELD_GROUPS_ID)) {
					return;
				}
			}
				conversation = setAudioIsReadFlag(conversation);
				for (int i = 0; i < mConversations.size(); i++) {
					if (mConversations.get(i).getId()
							.equals(conversation.getId())) {
						mConversations.remove(i);
						break;
					}
				}
				boolean flag2 = false;
				if (conversation.getSticktime().equals(
						TargetSetting.STICKTIME_CLOSE)) {
					for (int i = 0; i < mConversations.size(); i++) {
						if (mConversations.get(i).getSticktime()
								.equals(TargetSetting.STICKTIME_CLOSE)
								&& Long.valueOf(conversation.getTimestamp()) > Long
										.valueOf(mConversations.get(i)
												.getTimestamp())) {
							mConversations.add(i, conversation);
							flag2 = true;
							break;
						}
					}

				} else {
					for (int i = 0; i < mConversations.size(); i++) {
						if (Long.valueOf(conversation.getSticktime()) > Long
								.valueOf(mConversations.get(i).getSticktime())) {
							mConversations.add(i, conversation);
							flag2 = true;
							break;
						} else if (Long.valueOf(conversation.getSticktime()) == Long
								.valueOf(mConversations.get(i).getSticktime())
								&& Long.valueOf(conversation.getTimestamp()) > Long
										.valueOf(mConversations.get(i)
												.getTimestamp())) {
							mConversations.add(i, conversation);
							flag2 = true;
							break;
						}
					}
				}
				if (!flag2) {
					mConversations.add(conversation);
				}
				notifyDataSetChanged();
		}
	}

	synchronized public Conversation setAudioIsReadFlag(
			Conversation conversation) {
		boolean audioIsRead = true;
		if (conversation.getMsg_type() == 4 && conversation.getMsgId() != null) {// 语音�?���?��下已读未�?
			MsgInfo msgInfo = MsgInfoTbl.getInstance().getMsgInfo(
					conversation.getMsgId(), conversation.getId());
			if (msgInfo == null) {
				audioIsRead = false;
			} else {
				String readtime = msgInfo.getAudio_readtime();
				if (readtime == null || readtime.equals("")) {
					audioIsRead = false;
				}
			}
		}
		conversation.setAudioIsRead(audioIsRead);
		return conversation;
	}

	class ClearConversationObserverImpl implements ClearConversationObserver {

		@Override
		public void handle() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					clearConversationData();
					calculateAndRefreshAllUnreadCount();
				}
			});
		}

	}

	synchronized public void clearConversationData() {
		clearData();
		notifyDataSetChanged();
	}

	class GroupNameUpdateObserverImpl implements GroupNameUpdateObserver {

		@Override
		public void handle(final String groupName, final String gid) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					String gid2 = !gid.startsWith("G") ? GroupIdConv
							.uidTogid(gid) : gid;
					notifyDataSetChangedByConversationId(gid2);
				}
			});
		}

	}

	synchronized public void notifyDataSetChangedByConversationId(
			String conversationId) {
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				notifyDataSetChanged();
				break;
			}
		}
	}

	class GroupCat1UpdateObserverImpl implements GroupCat1UpdateObserver {

		@Override
		public void handle(final String gid, final int cat1) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					GroupInfo g = CacheManager.groupCache.get(gid
							.startsWith("G") ? GroupIdConv.gidTouid(gid) : gid);
					if (g != null) {
						g.setCat1(cat1);
						notifyDataSetChanged();
					}
				}
			});
		}

	}

	class UpdateMarkSuccessObserverImpl implements UpdateMarkSuccessObserver {

		@Override
		public void handle(final String uid, String mark) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					notifyDataSetChangedByConversationId(uid);
				}
			});
		}
	}

	class RemindChangeObserverImpl implements RemindChangeObserver {

		@Override
		public void handle(String id, final int remind) {
			if (id.startsWith("G")) {
				final String conversationId = id;
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						changeRemind(conversationId, remind);
					}
				});
			}
		}

		@Override
		public void handle(final HashMap<String, Integer> batch) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					changeRemind(batch);
				}
			});
		}

	}

	synchronized public void changeRemind(final HashMap<String, Integer> batch) {
		new Thread() {
			public void run() {
				synchronized (ConversationParentAdapter.this) {
					boolean flag = false;
					HashSet<String> needCheckAddConversationIds = new HashSet<String>();
					needCheckAddConversationIds.addAll(batch.keySet());
					for (int i = 0; i < mConversations.size(); i++) {
						needCheckAddConversationIds.remove(mConversations
								.get(i).getId());
						if (batch.containsKey(mConversations.get(i).getId())) {
							mConversations.get(i).setRemind(
									batch.get(mConversations.get(i).getId()));
							if (mConversations.get(i).getId().startsWith("G")) {
								if (mConversations.get(i).getRemind() == TargetSetting.REMIND_CLOSE) {
									mConversations.get(i).setParent(
											ChatUtil.BOX_SHIELD_GROUPS_ID);
								} else {
									mConversations.get(i).setParent(
											Conversation.PARENT_TOP);
								}
							}
							flag = true;
						}
					}

					boolean needCheckAndUpdateShieldGroupsConversation = false;
					List<String> cids = ConversationTbl.getInstance()
							.getAllConversationIds();
					for (String cid : cids) {
						if (needCheckAddConversationIds.contains(cid)) {
							final Conversation conversation = ConversationTbl
									.getInstance().getConversation(cid);
							if (conversation != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										addOrUpdataConversation(conversation);

									}
								});
								if (useType == USE_TYPE_2) {
									needCheckAndUpdateShieldGroupsConversation = true;
								}
							}
						}
					}

					if (flag) {
						final List<String> needDeleteConversationIds = new ArrayList<String>();
						for (Conversation conversation : mConversations) {
							if (useType == USE_TYPE_2) {
								if (!conversation.getParent().equals(
										Conversation.PARENT_TOP)) {
									needDeleteConversationIds.add(conversation
											.getId());
								}
							} else if (useType == USE_TYPE_3) {
								if (!conversation.getParent().equals(
										ChatUtil.BOX_SHIELD_GROUPS_ID)) {
									needDeleteConversationIds.add(conversation
											.getId());
								}
							}
						}

						if (needDeleteConversationIds.size() > 0) {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									deleteData(needDeleteConversationIds);
									calculateAndRefreshAllUnreadCount();
									notifyDataSetChanged();
								}
							});
							if (useType == USE_TYPE_2) {
								needCheckAndUpdateShieldGroupsConversation = true;
							}
						} else {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									calculateAndRefreshAllUnreadCount();
									notifyDataSetChanged();
								}
							});
						}
					}
					if (needCheckAndUpdateShieldGroupsConversation) {
						ChatUtil.checkAndUpdateShieldGroupsConversation(false);
					}
				}

			}
		}.start();
	}

	synchronized public void changeRemind(String conversationId, int remind) {
			boolean exist = false;
			boolean needCheckAndUpdateShieldGroupsConversation = false;
			for (int i = 0; i < mConversations.size(); i++) {
				if (mConversations.get(i).getId().equals(conversationId)) {
					exist = true;
					mConversations.get(i).setRemind(remind);
					if (mConversations.get(i).getId().startsWith("G")) {
						if (mConversations.get(i).getRemind() == TargetSetting.REMIND_CLOSE) {
							mConversations.get(i).setParent(
									ChatUtil.BOX_SHIELD_GROUPS_ID);
						} else {
							mConversations.get(i).setParent(
									Conversation.PARENT_TOP);
						}
					}
					boolean flag = false;
					if (useType == USE_TYPE_2) {
						if (!mConversations.get(i).getParent()
								.equals(Conversation.PARENT_TOP)) {
							deleteData(mConversations.get(i).getId());
							needCheckAndUpdateShieldGroupsConversation = true;
							calculateAndRefreshAllUnreadCount();
							flag = true;
						}
					} else if (useType == USE_TYPE_3) {
						if (!mConversations.get(i).getParent()
								.equals(ChatUtil.BOX_SHIELD_GROUPS_ID)) {
							deleteData(mConversations.get(i).getId());
							calculateAndRefreshAllUnreadCount();
							flag = true;
						}
					}

					if (!flag) {
						calculateAndRefreshAllUnreadCount();
						notifyDataSetChanged();
					}
					break;
				}
			}
			if (!exist) {
				Conversation conversation = ConversationTbl.getInstance()
						.getConversation(conversationId);
				if (conversation != null) {
					addOrUpdataConversation(conversation);
					if (useType == USE_TYPE_2) {
						needCheckAndUpdateShieldGroupsConversation = true;
					}
				}
			}
			if (needCheckAndUpdateShieldGroupsConversation) {
				ChatUtil.checkAndUpdateShieldGroupsConversation(false);
			}
	}

	synchronized public void deleteData(String conversationId) {
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				String parent = mConversations.get(i).getParent();
				mConversations.remove(i);
				notifyDataSetChanged();
				if (parent.startsWith("B")) {
					// 判断剩余的conversation数量
					if (mConversations.size() == 0) {
						ChatUtil.checkAndUpdateBoxConversation(parent, false);
					} else {
						if (i == 0) {
							ChatUtil.checkAndUpdateBoxConversation(
									mConversations.get(0), false);
						}
					}
				}
				break;
			}
		}
	}

	synchronized void deleteData(List<String> conversationIds) {
		boolean flag = false;
		String parent = "";
		for (int i = mConversations.size() - 1; i >= 0; i--) {
			if (conversationIds.contains(mConversations.get(i).getId())) {
				parent = mConversations.get(i).getParent();
				mConversations.remove(i);
				flag = true;
			}
		}
		if (flag) {
			notifyDataSetChanged();
			if (useType == USE_TYPE_3) {
				if (mConversations.size() == 0) {
					ChatUtil.checkAndUpdateBoxConversation(parent, false);
				} else {
					ChatUtil.checkAndUpdateBoxConversation(
							mConversations.get(0), false);
				}
			}
			if (parent.startsWith("B")) {
				// 判断剩余的conversation数量
				if (mConversations.size() == 0) {
					ChatUtil.checkAndUpdateBoxConversation(parent, false);
				} else {
					ChatUtil.checkAndUpdateBoxConversation(mConversations
							.get(0).getId(), false);
				}
			}
		}
	}

	class ConversationUpdateObserverImpl implements ConversationUpdateObserver {

		@Override
		public void handle(String conversationId) {
			if (haveConversation(conversationId)) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}

	}

	public boolean haveConversation(String conversationId) {
		boolean flag = false;
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	class MsgSendServerInfoChangedObserverImpl implements
			MsgSendServerInfoChangedObserver {

		@Override
		public void handle(final String msgId, final long sendTime,
				final String serverMsgId, final long orderNum) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					changeMsgSendServerInfo(msgId, sendTime, serverMsgId,
							orderNum);
				}
			});
		}

	}

	public synchronized void changeMsgSendServerInfo(String msgId,
			long sendTime, String serverMsgId, long orderNum) {
		if (msgId != null) {
			for (int i = 0; i < mConversations.size(); i++) {
				if (mConversations.get(i).getMsgId() != null
						&& mConversations.get(i).getMsgId().equals(msgId)) {
					mConversations.get(i).setTimestamp(sendTime);
					addOrUpdataConversation(mConversations.get(i));
					break;
				}
			}
		}
	}

	class AddOrUpdateConversationObserverImpl implements
			AddOrUpdateConversationObserver {

		@Override
		public void handle(final Conversation conversation) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					addOrUpdataConversation(conversation);
					calculateAndRefreshAllUnreadCount();
				}
			});
		}

		@Override
		public void handle(final String conversationId) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					addOrUpdataConversation(conversationId);
					calculateAndRefreshAllUnreadCount();
				}
			});
		}

	}

	synchronized public void addOrUpdataConversation(String conversationId) {
		final Conversation conversation = ConversationTbl.getInstance()
				.getConversation(conversationId);
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				addOrUpdataConversation(conversation);
			}
		});
	}

	class DeleteConversationObserverImpl implements DeleteConversationObserver {

		@Override
		public void handle(final String conversationId) {
			if (conversationId != null) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						deleteData(conversationId);
						calculateAndRefreshAllUnreadCount();
					}
				});
			}
		}
	}

	class DraftContentChangedObserverImpl implements
			DraftContentChangedObserver {

		@Override
		public void handle(final String target, final String content) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					changeDraft(target, content);
				}
			});
		}

	}

	public synchronized void changeDraft(String conversationId, String draft) {
		if (conversationId != null) {
			for (int i = 0; i < mConversations.size(); i++) {
				if (mConversations.get(i).getId() != null
						&& mConversations.get(i).getId().equals(conversationId)) {
					mConversations.get(i).setDraftContet(draft);
					notifyDataSetChanged();
					break;
				}
			}
		}
	}

	class MsgStatusObserverImpl implements MsgStatusObserver {

		@Override
		public void handle(final String msgId, final int status) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					changeSendStatus(msgId, status);
				}
			});
		}

	}

	public synchronized void changeSendStatus(String msgId, int status) {
		if (msgId != null) {
			for (int i = 0; i < mConversations.size(); i++) {
				if (mConversations.get(i).getMsgId() != null
						&& mConversations.get(i).getMsgId().equals(msgId)) {
					mConversations.get(i).setSendStatus(status);
					notifyDataSetChanged();
					break;
				}
			}
		}
	}

	class ConversationUnreadCountRefreshObserverImpl implements
			ConversationUnreadCountRefreshObserver {

		@Override
		public void handle(final String conversationId) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					refreshConversationUnreadCount(conversationId);
				}
			});
		}

	}

	synchronized public void refreshConversationUnreadCount(
			String conversationId) {
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				if (mConversations.get(i).getId().startsWith("B")) {
					mConversations.get(i).setCount(
							ConversationTbl.getInstance()
									.getConversationBoxUnreadCount(
											conversationId));
				} else {
					mConversations.get(i)
							.setCount(
									ConversationTbl.getInstance()
											.getConversationUnreadCount(
													conversationId));
				}
				notifyDataSetChanged();
				calculateAndRefreshAllUnreadCount();
				break;
			}
		}
	}

	class ConversationRefreshObserverImpl implements
			ConversationRefreshObserver {

		@Override
		public void handle() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}

	}

	class ConversationMsgChangeObserverImpl implements
			ConversationMsgChangeObserver {

		@Override
		public void handle(final String id, final String msg) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					changeConversationMsg(id, msg);
				}
			});
		}
	}

	synchronized public void changeConversationMsg(String conversationId,
			String msg) {
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				mConversations.get(i).setMsg(msg);
				notifyDataSetChanged();
				if (i == 0
						&& !mConversations.get(i).getParent()
								.equals(Conversation.PARENT_TOP)) {
					if (ConversationTbl.getInstance().clearConversationRecord(
							mConversations.get(i).getParent())) {
						ObserverManager.getInstance()
								.notifyConversationMsgChangeObservers(
										mConversations.get(i).getParent(), msg);
					}
				}
				return;
			}
		}
	}

	class UnreadCountClearObserverImpl implements UnreadCountClearObserver {

		@Override
		public void handle(String unreadMsgId) {

			if (ChatUtil.isConversationHide(unreadMsgId)) {// 排除不显示的会话
				return;
			}
			final String f_unreadMsgId = unreadMsgId;
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// 会话未读数量设置
					clearUnreadInfoCount(f_unreadMsgId);
					// 会话未读总数量设�?
					calculateAndRefreshAllUnreadCount();
				}
			});

		}

	}

	synchronized public void clearUnreadInfoCount(String conversationId) {
		Conversation conversation = null;
		for (int i = 0; i < mConversations.size(); i++) {
			if (mConversations.get(i).getId().equals(conversationId)) {
				mConversations.get(i).setCount(0);
				conversation = mConversations.get(i);
				mConversations.remove(i);
				break;
			}
		}

		if (conversation != null) {
			addOrUpdataConversation(conversation);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		final Conversation conversation = (Conversation) parent.getAdapter()
				.getItem(position);
		showConversationDialog(conversation);
		return true;
	}

	public void showConversationDialog(final Conversation conversation) {
		if (conversation.getId().startsWith("G")) {
			final int remind = TargetSettingTbl.getInstance().getRemind(
					conversation.getId(), LoginManager.loginUserInfo.getId());
			// final String remindItem = remind == TargetSetting.REMIND_OPEN ?
			// mActivity
			// .getString(ResString.getString_group_msg_noti_open2())
			// : (remind == TargetSetting.REMIND_UNDIS ? mActivity
			// .getString(ResString
			// .getString_group_msg_noti_undis2())
			// : LoginManager.applicationContext
			// .getString(ResString
			// .getString_group_msg_noti_close2()));
			new AlertDialog.Builder(mActivity)
					.setItems(
							new String[] { mActivity.getString(ResString
									.getString_delete_msg()) },
							// ,remindItem },
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										showDeleteConversationDialog(conversation);
										break;
									// case 1:
									// showRemindSelDialog(remind,
									// conversation.getId());
									// break;
									}
								}
							}).create().show();
		} else if (conversation.getId().startsWith("B")) {
			new AlertDialog.Builder(mActivity)
					.setItems(
							new String[] { mActivity.getString(ResString
									.getString_wm_delete()) },
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										ChatUtil.deleteChatRecord(conversation.getId());
										break;
									}
								}
							}).create().show();
		} else {
			new AlertDialog.Builder(mActivity)
					.setItems(
							new String[] { mActivity.getString(ResString
									.getString_delete_msg()) },
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									switch (which) {
									case 0:
										showDeleteConversationDialog(conversation);
										break;
									}
								}
							}).create().show();
		}
	}

	// private void showRemindSelDialog(final int currentRemind, final String
	// gid) {
	// String[] items = new String[3];
	// items[0] = mActivity.getString(ResString
	// .getString_group_notify_open_brief());
	// items[1] = mActivity.getString(ResString
	// .getString_group_notify_undis_brief());
	// items[2] = mActivity.getString(ResString
	// .getString_group_notify_close_brief());
	// if (currentRemind == TargetSetting.REMIND_OPEN)
	// items[0] = items[0] + "                          �?;
	// else if (currentRemind == TargetSetting.REMIND_UNDIS)
	// items[1] = items[1] + "                          �?;
	// else if (currentRemind == TargetSetting.REMIND_CLOSE)
	// items[2] = items[2] + "                            �?;
	//
	// new AlertDialog.Builder(mActivity)
	// .setItems(items, new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// String id;
	// id = (!gid.startsWith("G")) ? GroupIdConv.uidTogid(gid)
	// : gid;
	// String groupId = gid.startsWith("G") ? GroupIdConv
	// .gidTouid(gid) : gid;
	// final String f_id = id;
	// switch (which) {
	// case 0:
	// ApiUtil.shortConnectRequest(
	// "/groupuser/update_extend",
	// "gid="
	// + groupId
	// + "&uid="
	// + LoginManager.loginUserInfo
	// .getId() + "&remind=Remind",
	// RequestType.POST, 120,
	// new ShortConnectCallback() {
	//
	// @Override
	// public void handleException(
	// WeimiNotice weimiNotice) {
	//
	// }
	//
	// @Override
	// public void handle(
	// WeimiNotice weimiNotice) {
	// try {
	// JSONObject objectJson = new JSONObject(
	// weimiNotice.getObject()
	// .toString());
	// int apistatus = objectJson
	// .getInt("apistatus");
	// if (apistatus == 1) {
	// if (TargetSettingTbl
	// .getInstance()
	// .setRemind(
	// f_id,
	// LoginManager.loginUserInfo
	// .getId(),
	// TargetSetting.REMIND_OPEN)) {
	// ObserverManager
	// .getInstance()
	// .notifyRemindChangeObservers(
	// gid.startsWith("G") ? gid
	// : GroupIdConv
	// .uidTogid(gid),
	// TargetSetting.REMIND_OPEN);
	// }
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// break;
	// case 1:
	// ApiUtil.shortConnectRequest(
	// "/groupuser/update_extend",
	// "gid="
	// + groupId
	// + "&uid="
	// + LoginManager.loginUserInfo
	// .getId()
	// + "&remind=UnDisturb",
	// RequestType.POST, 120,
	// new ShortConnectCallback() {
	//
	// @Override
	// public void handleException(
	// WeimiNotice weimiNotice) {
	//
	// }
	//
	// @Override
	// public void handle(
	// WeimiNotice weimiNotice) {
	// try {
	// JSONObject objectJson = new JSONObject(
	// weimiNotice.getObject()
	// .toString());
	// int apistatus = objectJson
	// .getInt("apistatus");
	// if (apistatus == 1) {
	// if (TargetSettingTbl
	// .getInstance()
	// .setRemind(
	// f_id,
	// LoginManager.loginUserInfo
	// .getId(),
	// TargetSetting.REMIND_UNDIS)) {
	// ObserverManager
	// .getInstance()
	// .notifyRemindChangeObservers(
	// gid.startsWith("G") ? gid
	// : GroupIdConv
	// .uidTogid(gid),
	// TargetSetting.REMIND_UNDIS);
	// }
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// break;
	// case 2:
	// ApiUtil.shortConnectRequest(
	// "/groupuser/update_extend",
	// "gid="
	// + groupId
	// + "&uid="
	// + LoginManager.loginUserInfo
	// .getId()
	// + "&remind=Shielded",
	// RequestType.POST, 120,
	// new ShortConnectCallback() {
	//
	// @Override
	// public void handleException(
	// WeimiNotice weimiNotice) {
	//
	// }
	//
	// @Override
	// public void handle(
	// WeimiNotice weimiNotice) {
	// try {
	// JSONObject objectJson = new JSONObject(
	// weimiNotice.getObject()
	// .toString());
	// int apistatus = objectJson
	// .getInt("apistatus");
	// if (apistatus == 1) {
	// if (TargetSettingTbl
	// .getInstance()
	// .setRemind(
	// f_id,
	// LoginManager.loginUserInfo
	// .getId(),
	// TargetSetting.REMIND_CLOSE)) {
	// ObserverManager
	// .getInstance()
	// .notifyRemindChangeObservers(
	// gid.startsWith("G") ? gid
	// : GroupIdConv
	// .uidTogid(gid),
	// TargetSetting.REMIND_CLOSE);
	// }
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// });
	// break;
	// }
	// }
	// }).create().show();
	// }

	public void showDeleteConversationDialog(final Conversation conversation) {
		new AlertDialog.Builder(mActivity)
				.setMessage(ResString.getString_sure_to_del_conversation())
				.setNegativeButton(ResString.getString_cancel(), null)
				.setPositiveButton(ResString.getString_confirm(),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								final ProgressDialog progressDialog = new ProgressDialog(
										mActivity);
								new Thread() {
									public void run() {
										mHandler.post(new Runnable() {

											@Override
											public void run() {
												progressDialog.setMessage(mActivity.getString(ResString
														.getString_handling()));
												progressDialog.show();
											}
										});
										ChatUtil.deleteChatRecord(conversation.getId());
										mHandler.post(new Runnable() {

											@Override
											public void run() {
												progressDialog.cancel();
											}
										});
									}
								}.start();
							}
						}).show();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Conversation conversation = (Conversation) arg0.getAdapter().getItem(
				arg2);
		if (conversation.getId().startsWith("G")) {// 群聊
			long gid = 0;
			try {
				gid = Long.parseLong(GroupIdConv.gidTouid(conversation.getId()));
				
			} catch (Exception e) {
			}
			if(gid > 0){
				WmOpenChatSdk.getInstance().gotoGroupChatPage(gid, mActivity);				
			}
		}  else {// 单聊
			WmOpenChatSdk.getInstance().gotoChatPage(conversation.getId(), mActivity);
		}

	}

}
