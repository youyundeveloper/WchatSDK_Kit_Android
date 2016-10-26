package com.ioyouyun.ui.chat.opensource;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ioyouyun.ui.chat.bean.DeviceInfo;
import com.ioyouyun.ui.chat.bean.GroupInfo;
import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.core.Consts;
import com.ioyouyun.ui.chat.core.chat.util.AudioPlayHelp;
import com.ioyouyun.ui.chat.core.chat.util.AudioPlayHelp.AudioStopListener;
import com.ioyouyun.ui.chat.core.chat.util.AudioPlayHelp.DistanceChangeListener;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil;
import com.ioyouyun.ui.chat.core.login.LoginManager;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.DeleteMsgObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.FileUploadObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverIface.MsgFlagChangedObserver;
import com.ioyouyun.ui.chat.core.observer.ObserverManager;
import com.ioyouyun.ui.chat.core.util.CommonImageUtil;
import com.ioyouyun.ui.chat.core.util.FileUtil;
import com.ioyouyun.ui.chat.core.util.MySettingHelper;
import com.ioyouyun.ui.chat.core.util.PlayUtil;
import com.ioyouyun.ui.chat.db.tables.MsgInfoTbl;
import com.ioyouyun.ui.chat.lib.ResRaw;
import com.ioyouyun.ui.chat.lib.ResString;
import com.ioyouyun.ui.chat.ui.ShowChatImgActivity;
import com.ioyouyun.ui.chat.ui.adapter.ParentAdapter;
import com.ioyouyun.ui.chat.util.ApplicationUtil;
import com.ioyouyun.ui.chat.util.DeviceUtil;
import com.ioyouyun.wchat.GroupIdConv;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class MessageParentAdapter extends ParentAdapter implements
		DistanceChangeListener {

	public static long CHECK_REPEAT_TIME = 3000;
	public HashMap<String, Integer> fileUploadProgress = new HashMap<String, Integer>();
	/** adapter数据源 */
	public List<MsgInfo> mMsgInfos = new ArrayList<MsgInfo>();
	public LayoutInflater mLayoutInflater;
	public DisplayImageOptions userAvatarLoadOptions;
	public String conversationId;
	public AudioPlayHelp mAudioPlayHelp;
	public ListView mListView;

	public FileUploadObserverImpl fileUploadObserverImpl;
	public DeleteMsgObserverImpl deleteMsgObserverImpl;
	public MsgFlagChangedObserverImpl msgFlagChangedObserverImpl;

	public final int screenWidth7p10 = DeviceInfo.mScreenWidth * 7 / 10;
	public final int screenWidth1p3 = DeviceInfo.mScreenWidth / 3;
	public final int screenWidth1p4 = DeviceInfo.mScreenWidth / 4;
	public static final int screenWidth2p7 = DeviceInfo.mScreenWidth * 2 / 7;

	public MessageParentAdapter(Activity activity) {
		super(activity);

	}

	public void init(ListView lv) {
		mListView = lv;
		mLayoutInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		conversationId = ((ChatActivityParent) mActivity).conversationId;
		int audioManagerMode = AudioManager.MODE_NORMAL;
		if (MySettingHelper.getInstance().getSoundModel() == Consts.SOUND_TEL) {
			audioManagerMode = AudioManager.MODE_IN_CALL;
		} else {
			audioManagerMode = AudioManager.MODE_NORMAL;
		}
		mAudioPlayHelp = AudioPlayHelp.createAudioPlayHelp(mActivity,
				audioManagerMode);
		mAudioPlayHelp.setDistanceChangeListener(this);
		fileUploadObserverImpl = new FileUploadObserverImpl();
		ObserverManager.getInstance().addFileUploadObserver(
				fileUploadObserverImpl);
		deleteMsgObserverImpl = new DeleteMsgObserverImpl();
		ObserverManager.getInstance().addDeleteMsgObserver(
				deleteMsgObserverImpl);
		msgFlagChangedObserverImpl = new MsgFlagChangedObserverImpl();
		ObserverManager.getInstance().addMsgFlagChangedObserver(
				msgFlagChangedObserverImpl);
		userAvatarLoadOptions = new DisplayImageOptions.Builder().cacheOnDisc(true)
				.cacheInMemory(true)
				.showImageForEmptyUri(Consts.USER_AVATAR_RES_DEFAULT)
				.showImageOnFail(Consts.USER_AVATAR_RES_DEFAULT)
				.bitmapConfig(Bitmap.Config.RGB_565).build();
	}

	synchronized public void replaceMsgContent(String msgId, String msgContent) {
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.get(i).setMsg(msgContent);
				notifyDataSetChanged();
				break;
			}
		}
	}

	synchronized public void changeMsgStatus(String msgId, int status) {
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.get(i).setSend_status(status);
				notifyDataSetChanged();
				break;
			}
		}
	}

	public void changeMsgSendServerInfo(String msgId, long sendTime,
			String serverMsgId, long orderNum) {
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.get(i).setTimestamp(sendTime + "");
				mMsgInfos.get(i).setServerMsgId(serverMsgId);
				mMsgInfos.get(i).setOrderNum(orderNum);
				break;
			}
		}
	}

	public MsgInfo getEarliestMsg() {
		MsgInfo msgInfo = null;
		if (mMsgInfos.size() > 0) {
			for (int i = 0; i < mMsgInfos.size(); i++) {
				if (mMsgInfos.get(i).getMsg_type() != ChatUtil.MSGT_TIME) {
					msgInfo = mMsgInfos.get(i);
					break;
				}
			}
		}
		return msgInfo;
	}

	public String getEarliestMsgId() {
		String msgId = null;
		if (mMsgInfos.size() > 0) {
			for (int i = 0; i < mMsgInfos.size(); i++) {
				if (mMsgInfos.get(i).getMsg_type() != ChatUtil.MSGT_TIME) {
					msgId = mMsgInfos.get(i).getMsg_id();
					break;
				}
			}
		}
		return msgId;
	}

	public String getEarliestMsgTime() {
		String msgTime = null;
		if (mMsgInfos.size() > 0) {
			for (int i = 0; i < mMsgInfos.size(); i++) {
				if (mMsgInfos.get(i).getMsg_type() != ChatUtil.MSGT_TIME) {
					msgTime = mMsgInfos.get(i).getTimestamp();
					break;
				}
			}
		}
		return msgTime;
	}

	@Override
	public int getCount() {
		return mMsgInfos.size();
	}

	@Override
	public Object getItem(int position) {
		MsgInfo msgInfo = null;
		msgInfo = mMsgInfos.get(position);
		return msgInfo;
	}

	synchronized public void deleteGroupCardMsg(String cardGid) {
		cardGid = cardGid.startsWith("G") ? GroupIdConv.gidTouid(cardGid)
				: cardGid;
		boolean flag = false;
		for (int i = mMsgInfos.size() - 1; i >= 0; i--) {
			if (mMsgInfos.get(i).getMsg_type() == ChatUtil.MSGT_GCAR) {
				GroupInfo groupInfo = ChatUtil.getGcardMsgGroupInfo(mMsgInfos
						.get(i).getMsg());
				if (groupInfo != null && groupInfo.getGid().equals(cardGid)) {
					MsgInfoTbl.getInstance().deleteMsg(
							mMsgInfos.get(i).getMsg_id(), conversationId);
					mMsgInfos.remove(i);
					flag = true;
				}
			}
		}
		if (flag) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	}

	synchronized public void addListData(Object data) {
		addListData(data, false);
	}

	synchronized public void addHistoryMsgInfos(List<MsgInfo> msgInfos) {
		if (msgInfos != null && msgInfos.size() > 0) {
			for (MsgInfo msgInfo : msgInfos) {
				if (!mMsgInfos.contains(msgInfo)) {
					handleAddMsgInfo(msgInfo);
				}
			}
			notifyDataSetChanged();
		}
	}

	public int getPosition(String msgId) {
		int pos = -1;
		if (msgId != null) {
			for (int i = 0; i < mMsgInfos.size(); i++) {
				if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
					pos = i;
					break;
				}
			}
		}
		return pos;
	}

	synchronized public void addListData(Object data, boolean checkRepeat) {

		MsgInfo msgInfo = (MsgInfo) data;
		if (checkRepeat) {
			if (mMsgInfos.contains(msgInfo)) {
				return;
			}
		}
		handleAddMsgInfo(msgInfo);
		notifyDataSetChanged();
		if (!msgInfo.isHistory()) {
			if (mListView.getLastVisiblePosition() == mMsgInfos.size() - 1
					|| msgInfo.getFrom_id().equals(
							LoginManager.loginUserInfo.getId())) {
				setSelection(mMsgInfos.size());
			}
		}
	}

	public void handleAddMsgInfo(MsgInfo msgInfo) {
		int index = 0;
		if (msgInfo.isHistory()) {
			for (int i = mMsgInfos.size() - 1; i >= 0; i--) {
				if (Long.parseLong(mMsgInfos.get(i).getTimestamp()) < Long
						.parseLong(msgInfo.getTimestamp())) {
					index = i + 1;
					break;
				}
			}
		} else {
			index = mMsgInfos.size();
		}
		mMsgInfos.add(index, msgInfo);
		int nextIndex = index + 1;
		if (nextIndex <= mMsgInfos.size() - 1
				&& mMsgInfos.get(nextIndex).getMsg_type() != ChatUtil.MSGT_TIME
				&& Long.parseLong(mMsgInfos.get(nextIndex).getTimestamp())
						- Long.parseLong(mMsgInfos.get(index).getTimestamp()) >= ChatUtil.MSG_TIME_SEPARATE) {
			addTimeMsg(nextIndex, mMsgInfos.get(nextIndex).getTimestamp());
		}
		int preIndex = index - 1;
		if (preIndex >= 0) {
			if (mMsgInfos.get(preIndex).getMsg_type() != ChatUtil.MSGT_TIME
					&& Long.parseLong(mMsgInfos.get(index).getTimestamp())
							- Long.parseLong(mMsgInfos.get(preIndex)
									.getTimestamp()) >= ChatUtil.MSG_TIME_SEPARATE) {
				addTimeMsg(index, mMsgInfos.get(index).getTimestamp());
			}
		} else {
			if (nextIndex <= mMsgInfos.size() - 1
					&& mMsgInfos.get(nextIndex).getMsg_type() == ChatUtil.MSGT_TIME) {
				if (nextIndex == mMsgInfos.size() - 1) {
					mMsgInfos.remove(nextIndex);
				} else {
					if (Long.parseLong(mMsgInfos.get(nextIndex + 1)
							.getTimestamp())
							- Long.parseLong(mMsgInfos.get(index)
									.getTimestamp()) < ChatUtil.MSG_TIME_SEPARATE) {
						mMsgInfos.remove(nextIndex);
					}
				}
			}
			addTimeMsg(index, mMsgInfos.get(index).getTimestamp());
		}
	}

	public void addTimeMsg(int index, String timestamp) {
		MsgInfo msgInfo = new MsgInfo();
		msgInfo.setFrom_id("0");
		msgInfo.setMsg_id("0");
		msgInfo.setMsg_type(ChatUtil.MSGT_TIME);
		msgInfo.setTimestamp(timestamp);
		mMsgInfos.add(index, msgInfo);

	}

	public void setSelection(int location) {
		mListView.setSelection(location);
	}

	synchronized public void deleteData(String msgId) {
		MsgInfoTbl.getInstance().deleteMsg(msgId, conversationId);
		deleteData2(msgId);
	}

	synchronized public void deleteData2(String msgId) {
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.remove(i);
				if (mMsgInfos.size() > 0) {
					int preIndex = i - 1;
					if (preIndex >= 0) {
						if (mMsgInfos.get(preIndex).getMsg_type() == ChatUtil.MSGT_TIME) {
							boolean flag = false;
							if (preIndex - 1 >= 0
									&& mMsgInfos.get(preIndex - 1)
											.getMsg_type() == ChatUtil.MSGT_TIME) {
								flag = true;
							}

							if (!flag) {
								if (preIndex + 1 <= mMsgInfos.size() - 1
										&& mMsgInfos.get(preIndex + 1)
												.getMsg_type() == ChatUtil.MSGT_TIME) {
									flag = true;
								}
							}
							if (flag) {
								mMsgInfos.remove(preIndex);
							}
						}
					}
				}
				if (mMsgInfos.size() > 0) {
					if (mMsgInfos.get(mMsgInfos.size() - 1).getMsg_type() == ChatUtil.MSGT_TIME) {
						mMsgInfos.remove(mMsgInfos.size() - 1);
					}
				}
				notifyDataSetChanged();
				break;
			}
		}
	}

	// 删除
	synchronized public void deleteData(MsgInfo msgInfo) {
		if (msgInfo.getMsg_type() == ChatUtil.MSGT_AUDIO
				|| msgInfo.getMsg_type() == -ChatUtil.MSGT_AUDIO) {
			if (mAudioPlayHelp.isPlaying()
					&& getCurrentPlayingAudioMsg() != null
					&& getCurrentPlayingAudioMsg().getMsg_id().equals(
							msgInfo.getMsg_id())) {
				mAudioPlayHelp.stop();
			}
		}
		FileUtil.deleteWeimiChatFile(msgInfo.getMsg_type(),
				msgInfo.getMsg_id(), conversationId);
		deleteData(msgInfo.getMsg_id());
	}

	// 复制
	public void copyData(MsgInfo info) {
		ApplicationUtil.copy(info.getMsg(), mActivity);
	}

	synchronized public void addTopAll(Object data) {
		addTopAll(data, false);
	}

	synchronized public void addTopAll(Object data, boolean checkRepeat) {
		List<MsgInfo> temp = (List<MsgInfo>) data;
		if (checkRepeat) {
			for (int i = mMsgInfos.size() - 1; i >= 0; i--) {
				if (temp.contains(mMsgInfos.get(i))) {
					mMsgInfos.remove(i);
				}
			}
			if (mMsgInfos.size() > 0) {
				for (int i = mMsgInfos.size() - 1; i >= 0; i--) {
					if (mMsgInfos.get(i).getMsg_type() == ChatUtil.MSGT_TIME) {
						mMsgInfos.remove(i);
					} else {
						break;
					}
				}
			}
		}
		List<MsgInfo> msgInfos = addTimeMsgInfos(temp);
		mMsgInfos.addAll(0, msgInfos);
		notifyDataSetChanged();
	}

	public int getItemInfoType(int position) {
		int infoType;
		MsgInfo msgInfo = (MsgInfo) getItem(position);
		if (msgInfo.getMsg_type() == ChatUtil.MSGT_CUSTOM
				|| msgInfo.getMsg_type() == ChatUtil.MSGT_SYS_EVENT
				|| (msgInfo.getMsg_type() == ChatUtil.MSGT_CONTENT && msgInfo
						.getSubType().equals(ChatUtil.MC_TYPE_RICH_CONTENT))) {
			infoType = msgInfo.getMsg_type();
		} else {
			if (msgInfo.getFrom_id().equals(LoginManager.loginUserInfo.getId())) {
				infoType = -msgInfo.getMsg_type();
			} else {
				infoType = msgInfo.getMsg_type();
			}
		}
		return infoType;
	}

	public String getItemInfoSubType(int position) {
		return ((MsgInfo) getItem(position)).getSubType();
	}

	@Override
	public int getItemViewType(int position) {
		int viewType = IGNORE_ITEM_VIEW_TYPE;
		int infoType = getItemInfoType(position);
		if (infoType == ChatUtil.MSGT_TEXT) {
			viewType = 0;
		} else if (infoType == -ChatUtil.MSGT_TEXT) {
			viewType = 1;
		} else if (infoType == ChatUtil.MSGT_AUDIO) {
			viewType = 2;
		} else if (infoType == -ChatUtil.MSGT_AUDIO) {
			viewType = 3;
		} else if (infoType == ChatUtil.MSGT_PICTURE) {
			viewType = 4;
		} else if (infoType == -ChatUtil.MSGT_PICTURE) {
			viewType = 5;
		} else if (infoType == ChatUtil.MSGT_TIME) {
			viewType = 6;
		} else if (infoType == ChatUtil.MSGT_SYS_EVENT) {
			viewType = 7;
		} else if(ChatUtil.isCustomMsgType(Math.abs(infoType))){	//自定义消息-左
			viewType = 8;
		} else if(infoType < 0 && ChatUtil.isCustomMsgType(Math.abs(infoType))){					//自定义消息-右
			viewType = 9;
		}
		return viewType;
	}

	public View getItemView(int position) {
		View view = null;
		position++;
		int visiblePosition = mListView.getFirstVisiblePosition();
		if (position >= visiblePosition) {
			view = mListView.getChildAt(position - visiblePosition);
		}
		return view;
	}

	@Override
	public int getViewTypeCount() {
		return 10;
	}

	synchronized public void clearData() {
		mMsgInfos.clear();
		notifyDataSetChanged();
	}

	synchronized public void setAudioMsgRead(String msgId) {
		long currentTime = System.currentTimeMillis();
		MsgInfoTbl.getInstance().updateAudioReadTime(msgId, currentTime + "",
				conversationId);
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.get(i).setAudio_readtime(currentTime + "");
				notifyDataSetChanged();
				break;
			}
		}
		ObserverManager.getInstance().notifyFirstReadAudioMsgObservers(msgId,
				currentTime + "");
	}

	public boolean showPoint(int position) {
		boolean showPoint = false;
		if (position != 0
				&& mMsgInfos.get(position - 1).getFrom_id()
						.equals(mMsgInfos.get(position).getFrom_id())
				&& !(mMsgInfos.get(position - 1).getMsg_type() == ChatUtil.MSGT_SYS_EVENT)) {
			showPoint = true;
		}
		return showPoint;
	}

	public int DIRECT_LEFT = 1;
	public int DIRECT_RIGHT = 2;

	public void setCommonView(MsgInfo msgInfo, int position, int direct,
			ImageView avatar, ImageView avatarPoint, ImageView avatarLineTop,
			ImageView avatarLineBottom, TextView name, ImageView failIcon,
			View sending) {
		if (direct == DIRECT_RIGHT) {
			if (msgInfo.getSend_status() == MsgInfo.SEND_STATUS_FAILED) {
				failIcon.setVisibility(View.VISIBLE);
				setFailIconOnClickListener(failIcon, msgInfo.getMsg_type(),
						msgInfo.getSubType(), msgInfo.getMsg(),
						msgInfo.getMsg_id());
			} else {
				failIcon.setVisibility(View.GONE);
			}
			if (msgInfo.getSend_status() == MsgInfo.SEND_STATUS_UNKNOW) {
				sending.setVisibility(View.VISIBLE);
			} else {
				sending.setVisibility(View.GONE);
			}
		}
		if (showPoint(position) == true) {
			avatar.setVisibility(View.INVISIBLE);
			avatarPoint.setVisibility(View.VISIBLE);
			avatarLineTop.setVisibility(View.VISIBLE);
			name.setVisibility(View.INVISIBLE);
		} else {
			avatar.setVisibility(View.VISIBLE);
			avatarPoint.setVisibility(View.INVISIBLE);
			avatarLineTop.setVisibility(View.INVISIBLE);
			avatarLineBottom.setVisibility(View.INVISIBLE);

			if (direct == DIRECT_LEFT) {
				if(((ChatActivityParent)mActivity).isGroup){	//群聊显示名称为id
					name.setVisibility(View.VISIBLE);
					name.setText(msgInfo.getFrom_id());					
				} else{
					name.setVisibility(View.INVISIBLE);
				}
				CommonImageUtil.loadImage("drawable://"
						+ Consts.USER_AVATAR_RES_DEFAULT, avatar,
						userAvatarLoadOptions, null);
			} else if (direct == DIRECT_RIGHT) {
				name.setVisibility(View.INVISIBLE);
				if (LoginManager.loginUserInfo.getAvatar() != null
						&& !LoginManager.loginUserInfo.getAvatar().equals(
								"null")) {
					CommonImageUtil.loadImage(
							LoginManager.loginUserInfo.getAvatar(), avatar,
							userAvatarLoadOptions, null);
				} else {
					CommonImageUtil.loadImage("drawable://"
							+ Consts.USER_AVATAR_RES_DEFAULT, avatar,
							userAvatarLoadOptions, null);
				}
			}

		}

		boolean showLineBottom = false;
		if (position < mMsgInfos.size() - 1
				&& msgInfo.getFrom_id().equals(
						mMsgInfos.get(position + 1).getFrom_id())) {
			if (getItemInfoType(position + 1) != ChatUtil.MSGT_SYS_EVENT) {
				showLineBottom = true;
			}
		}
		if (showLineBottom) {
			avatarLineBottom.setVisibility(View.VISIBLE);
		} else {
			avatarLineBottom.setVisibility(View.INVISIBLE);
		}
	}

	public void setCommonLeftView(MsgInfo msgInfo, int position,
			ImageView avatar, ImageView avatarPoint, ImageView avatarLineTop,
			ImageView avatarLineBottom, TextView name) {
		setCommonView(msgInfo, position, DIRECT_LEFT, avatar, avatarPoint,
				avatarLineTop, avatarLineBottom, name, null, null);
	}

	public void setcommonRightView(MsgInfo msgInfo, int position,
			ImageView avatar, ImageView avatarPoint, ImageView avatarLineTop,
			ImageView avatarLineBottom, TextView name, ImageView failIcon,
			View sending) {
		setCommonView(msgInfo, position, DIRECT_RIGHT, avatar, avatarPoint,
				avatarLineTop, avatarLineBottom, name, failIcon, sending);
	}

	public void setItemContentClickListener(final View view, final int position) {
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v2) {
				int p = position;
				int infoType = getItemInfoType(p);
				final MsgInfo info = (MsgInfo) getItem(p);
				if (infoType == ChatUtil.MSGT_PICTURE
						|| infoType == -ChatUtil.MSGT_PICTURE) {// 图片 点击查看大图
					ArrayList<String> msgIds = new ArrayList<String>();
					for (int i = 0; i < mMsgInfos.size(); i++) {
						if (mMsgInfos.get(i).getMsg_type() == ChatUtil.MSGT_PICTURE
								|| mMsgInfos.get(i).getMsg_type() == -ChatUtil.MSGT_PICTURE) {
							msgIds.add(mMsgInfos.get(i).getMsg_id());
						}
					}
					Intent intent = new Intent(mActivity,
							ShowChatImgActivity.class);
					intent.putExtra("msgIds", (Serializable) msgIds);
					intent.putExtra("currentMsgId", info.getMsg_id());
					intent.putExtra("conversationId", conversationId);
					mActivity.startActivity(intent);
				} else if (infoType == ChatUtil.MSGT_AUDIO
						|| infoType == -ChatUtil.MSGT_AUDIO) {
					playAudioMsg(info);
				}
			}
		});
	}

	synchronized public void playAudioMsg(final MsgInfo info) {
		if (!DeviceUtil.checkSDCard()) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(mActivity,
							ResString.getString_wm_no_sd_card(),
							Toast.LENGTH_SHORT).show();
				}
			});
			return;
		}
		MsgInfo prePlayingMsg = getCurrentPlayingAudioMsg();
		if (mAudioPlayHelp.isPlaying()) {
			mAudioPlayHelp.stop();
			if (prePlayingMsg != null) {
				stopPlayingAnim(prePlayingMsg.getMsg_id());
				if (prePlayingMsg.getMsg_type() == ChatUtil.MSGT_CONTENT
						&& prePlayingMsg.getSubType().equals(
								ChatUtil.MC_TYPE_WHISPER_VOICE)) {
					deleteData(prePlayingMsg);
				}
			}
		}

		if (prePlayingMsg == null
				|| !prePlayingMsg.getMsg_id().equals(info.getMsg_id())) {
			if (!mAudioPlayHelp.isPlaying()) {// 没在播放
				String filePath = null;
				int audioLength = 0;
				if (info.getMsg_type() == ChatUtil.MSGT_AUDIO) {
					// 把未读的标记为已�?
					if (info.getAudio_readtime() == null
							|| info.getAudio_readtime().equals("")) {
						setAudioMsgRead(info.getMsg_id());
					}

					filePath = info.getMsg();
					String checkAndMoveFileToSdFromCacheDirNewPath = FileUtil
							.checkAndMoveFileToSdFromCacheDir(filePath);
					if (checkAndMoveFileToSdFromCacheDirNewPath != null
							&& !checkAndMoveFileToSdFromCacheDirNewPath
									.equals(filePath)) {
						filePath = checkAndMoveFileToSdFromCacheDirNewPath;
						info.setMsg(filePath);
						ObserverManager.getInstance()
								.notifyMsgContentChangedObservers(
										info.getMsg_id(), info.getMsg());
					}

					audioLength = Integer.parseInt(info.getExtra());
				}

				try {
					File file = new File(filePath);
					if (file.exists()) {
						setAudioViewSelection(info.getMsg_id());
						boolean flag = false;
						long wakeLockTime = AudioPlayHelp.WAKELOCK_TIME_PLAYING;
						try {
							wakeLockTime = audioLength * 1000 + 10000;// add 10
							// sec
						} catch (Exception e) {

						}
						flag = mAudioPlayHelp.play(filePath,
								new OnCompletionListener() {

									@Override
									public void onCompletion(MediaPlayer mp) {
										PlayUtil.playMusic2(ResRaw
												.getRaw_audio_end());
										stopPlayingAnim(info.getMsg_id());
										if (info.getMsg_type() == ChatUtil.MSGT_AUDIO) {
											playNextUnreadAudio(info
													.getMsg_id());
										}
									}
								}, new AudioStopListener() {
									@Override
									public void stop() {
										stopPlayingAnim(info.getMsg_id());
									}
								}, wakeLockTime);

						if (!flag) {// play failed
							mAudioPlayHelp.stop();
							stopPlayingAnim(info.getMsg_id());
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(
											mActivity,
											mActivity.getText(ResString
													.getString_wm_play_error()),
											Toast.LENGTH_SHORT).show();
								}
							});
						}
					} else {
						Toast.makeText(
								mActivity,
								mActivity.getText(ResString
										.getString_wm_file_not_exist()),
								Toast.LENGTH_SHORT).show();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {// 正在播放
				mAudioPlayHelp.stop();
				stopPlayingAnim();
			}

		}
	}

	public void stopAudioPlaying() {
		mAudioPlayHelp.stop();
		stopPlayingAnim();
	}

	synchronized public void stopPlayingAnim(String msgId) {
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.get(i).setAudioPlaying(false);
				notifyDataSetChanged();
				break;
			}
		}
	}

	synchronized public void stopPlayingAnim() {
		boolean flag = false;
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).isAudioPlaying()) {
				mMsgInfos.get(i).setAudioPlaying(false);
				flag = true;
			}
		}
		if (flag) {
			notifyDataSetChanged();
		}
	}

	public MsgInfo getCurrentPlayingAudioMsg() {
		MsgInfo msgInfo = null;
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).isAudioPlaying()) {
				msgInfo = mMsgInfos.get(i);
				break;
			}
		}
		return msgInfo;
	}

	/**
	 * play audio next of msgId
	 * 
	 * @param msgId
	 */
	public void playNextUnreadAudio(String msgId) {
		boolean flag = false;
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (getItemViewType(i) == 2 || getItemViewType(i) == 3) {
				if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
					flag = true;
					continue;
				}
				if (flag && mMsgInfos.get(i).getAudio_readtime() != null
						&& !mMsgInfos.get(i).getAudio_readtime().equals("")) {
					break;
				}
				if (flag
						&& ((mMsgInfos.get(i).getAudio_readtime() == null || mMsgInfos
								.get(i).getAudio_readtime().equals("")))) {
					playAudioMsg(mMsgInfos.get(i));
					break;
				}
			}
		}
	}

	public void setFailIconOnClickListener(View v, final int msgType,
			final String subType, final String data, final String msgId) {
		if (ChatUtil.checkCanResendMsgType(msgType, subType)) {
			v.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(mActivity)
							.setTitle(
									ResString.getString_wm_sure_to_resend_msg())
							.setPositiveButton(ResString.getString_confirm(),
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											MsgInfo msgInfo = MsgInfoTbl
													.getInstance().getMsgInfo(
															msgId,
															conversationId);
											if (msgInfo != null) {
												if (msgType == ChatUtil.MSGT_AUDIO) {
													try {
														Class.forName("com.ioyouyun.ui.chat.opensource.ChatActivity")
														.getMethod("resendAudioMsg", MsgInfo.class)
														.invoke(mActivity, msgInfo);
													} catch (IllegalArgumentException e) {
														e.printStackTrace();
													} catch (IllegalAccessException e) {
														e.printStackTrace();
													} catch (InvocationTargetException e) {
														e.printStackTrace();
													} catch (NoSuchMethodException e) {
														e.printStackTrace();
													} catch (ClassNotFoundException e) {
														e.printStackTrace();
													}
												} else {
													try {
														Class.forName("com.ioyouyun.ui.chat.opensource.ChatActivity")
														.getMethod("sendMsg", int.class,String.class,String.class,String.class)
														.invoke(mActivity, msgType,subType,data,msgInfo.getMessageIden());
													} catch (IllegalArgumentException e) {
														e.printStackTrace();
													} catch (IllegalAccessException e) {
														e.printStackTrace();
													} catch (InvocationTargetException e) {
														e.printStackTrace();
													} catch (NoSuchMethodException e) {
														e.printStackTrace();
													} catch (ClassNotFoundException e) {
														e.printStackTrace();
													}
												}
												deleteData(msgId);
											}
										}
									})
							.setNegativeButton(ResString.getString_cancel(),
									null).create().show();
				}
			});
		}
	}

	synchronized public void setAudioViewSelection(String msgId) {
		for (int i = 0; i < mMsgInfos.size(); i++) {
			if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
				mMsgInfos.get(i).setAudioPlaying(true);
				notifyDataSetChanged();
				break;
			}
		}
	}

	public void setAudioDrawableUnAnim(TextView tv, int res, int direction) {
		Drawable drawable = mActivity.getResources().getDrawable(res);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		switch (direction) {
		case 1:// left
			tv.setCompoundDrawables(drawable, null, null, null);
			break;
		case 2:// right
			tv.setCompoundDrawables(null, null, drawable, null);
			break;
		}
	}

	public void setAudioDrawableAnim(TextView tv, int res, int direction) {
		AnimationDrawable animationDrawable = (AnimationDrawable) mActivity
				.getResources().getDrawable(res);
		animationDrawable.setBounds(0, 0, animationDrawable.getMinimumWidth(),
				animationDrawable.getMinimumHeight());
		switch (direction) {
		case 1:// left
			tv.setCompoundDrawables(animationDrawable, null, null, null);
			break;
		case 2:// right
			tv.setCompoundDrawables(null, null, animationDrawable, null);
			break;
		}
		animationDrawable.start();
	}

	public void setItemContentLongClickListener(final View view,
			final int position) {
		view.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				int p = position;
				int infoType = getItemInfoType(p);
				String subType = getItemInfoSubType(p);
				final MsgInfo info = (MsgInfo) getItem(p);
				final String[] mItems = ChatUtil.getMsgAlertOptionItems(
						mActivity, infoType, subType);
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setItems(mItems, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mItems[which].equals(mActivity.getResources()
								.getString(ResString.getString_wm_delete()))) {
							deleteData(info);
						} else if (mItems[which].equals(mActivity
								.getResources().getString(
										ResString.getString_wm_copy()))) {
							copyData(info);
						}
					}
				});
				builder.create().show();
				return true;
			}
		});
	}

	synchronized public List<MsgInfo> addTimeMsgInfos(List<MsgInfo> msgInfos) {
		List<MsgInfo> resultMsgInfos = msgInfos;
		for (int i = resultMsgInfos.size() - 1; i >= 0; i--) {
			boolean flag = false;
			if (i == 0) {
				flag = true;
			} else {
				long currentMsgTime = Long.parseLong(resultMsgInfos.get(i)
						.getTimestamp());
				long preMsgTime = Long.parseLong(resultMsgInfos.get(i - 1)
						.getTimestamp());
				if (currentMsgTime - preMsgTime >= ChatUtil.MSG_TIME_SEPARATE) {
					flag = true;
				}
			}

			if (flag == true) {// 插入时间�?
				MsgInfo msgInfo = new MsgInfo();
				msgInfo.setFrom_id("0");
				msgInfo.setMsg_id("0");
				msgInfo.setMsg_type(ChatUtil.MSGT_TIME);
				msgInfo.setTimestamp(resultMsgInfos.get(i).getTimestamp());
				resultMsgInfos.add(i, msgInfo);
			}
		}
		return resultMsgInfos;
	}

	synchronized public void release() {
		mAudioPlayHelp.release();
		ObserverManager.getInstance().removeFileUploadObserver(
				fileUploadObserverImpl);
		ObserverManager.getInstance().removeDeleteMsgObserver(
				deleteMsgObserverImpl);
		ObserverManager.getInstance().removeMsgFlagChangedObserver(
				msgFlagChangedObserverImpl);
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	class FileUploadObserverImpl implements FileUploadObserver {

		@Override
		public void handle(String msgId, final int progress) {
			boolean existMsg = false;
			for (int i = 0; i < mMsgInfos.size(); i++) {
				if (mMsgInfos.get(i).getMsg_id().equals(msgId)) {
					existMsg = true;
					break;
				}
			}

			if (existMsg) {
				if (progress >= 100) {
					fileUploadProgress.remove(msgId);
				} else {
					fileUploadProgress.put(msgId, progress);
				}
				View v = mListView.findViewWithTag("progress_" + msgId);
				if (v != null) {
					final ProgressBar progressBar = (ProgressBar) v;
					if (progress >= 100) {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								if (progressBar.getVisibility() != View.VISIBLE) {
									progressBar.setVisibility(View.VISIBLE);
								}
								progressBar.setProgress(progress);
							}
						});

						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (progressBar.getVisibility() != View.GONE) {
									progressBar.setVisibility(View.GONE);
								}
							}
						}, 500);
					} else {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								if (progressBar.getVisibility() != View.VISIBLE) {
									progressBar.setVisibility(View.VISIBLE);
								}
								progressBar.setProgress(progress);
							}
						});
					}
				}
			} else {
				if (fileUploadProgress.containsKey(msgId)) {
					fileUploadProgress.remove(msgId);
				}
			}

		}

	}

	class DeleteMsgObserverImpl implements DeleteMsgObserver {

		@Override
		public void handle(String conversationId, final String msgId) {
			if (conversationId.equals(conversationId)) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						deleteData2(msgId);
					}
				});
			}
		}
	}

	@Override
	public void change(boolean close) {
		try {
			Class.forName("com.ioyouyun.ui.chat.opensource.ChatActivity")
			.getMethod("setBlackViewVisibility", int.class)
			.invoke(mActivity, close ? View.VISIBLE : View.GONE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	class MsgFlagChangedObserverImpl implements MsgFlagChangedObserver {

		@Override
		public void handle(String conversationId, int flag, long afterTime) {
			if (conversationId.equals(MessageParentAdapter.this.conversationId)) {
				synchronized (MessageParentAdapter.this) {
					for (int i = 0; i < mMsgInfos.size(); i++) {
						mMsgInfos.get(i).setFlag(flag);
					}
				}
			}
		}

	}

}
