package com.ioyouyun.ui.chat.opensource;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioyouyun.ui.chat.bean.Conversation;
import com.ioyouyun.ui.chat.bean.GroupUserProps;
import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.core.Consts;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil;
import com.ioyouyun.ui.chat.core.util.CommonImageUtil;
import com.ioyouyun.ui.chat.core.util.ExpEmojiUtil;
import com.ioyouyun.ui.chat.core.util.StringUtil;
import com.ioyouyun.ui.chat.core.util.TimeUtil;
import com.ioyouyun.ui.chat.lib.ResColor;
import com.ioyouyun.ui.chat.lib.ResDrawable;
import com.ioyouyun.ui.chat.lib.ResId;
import com.ioyouyun.ui.chat.lib.ResLayout;

public class ConversationAdapter extends ConversationParentAdapter {

	public ConversationAdapter(Activity activity, int useType) {
		super(activity);
		super.useType = useType;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mLayoutInflater.inflate(ResLayout.getLayout_wm_conversation_item(), null);
			viewHolder.avatar = (ImageView) convertView.findViewById(ResId
					.getId_avatar());
			viewHolder.name = (TextView) convertView.findViewById(ResId
					.getId_name());
			viewHolder.text = (TextView) convertView.findViewById(ResId
					.getId_text());
			viewHolder.count = (TextView) convertView.findViewById(ResId
					.getId_count());
			viewHolder.rootLayout = convertView.findViewById(ResId
					.getId_rootLayout());
			viewHolder.msgIcon = (ImageView) convertView.findViewById(ResId
					.getId_msg_icon());
			viewHolder.privateIcon = convertView.findViewById(ResId
					.getId_private_icon());
			viewHolder.unreadPoint = convertView.findViewById(ResId
					.getId_unread_point());
			viewHolder.msgIcon2 = (ImageView) convertView.findViewById(ResId
					.getId_msg_icon2());
			viewHolder.time = (TextView) convertView.findViewById(ResId
					.getId_time());
			viewHolder.draftsFlag = (ImageView) convertView.findViewById(ResId
					.getId_draftsFlag());
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		Conversation conversation = (Conversation) getItem(position);
		if (conversation.getDraftContet() != null
				&& !conversation.getDraftContet().equals("")) {
			viewHolder.draftsFlag.setVisibility(View.VISIBLE);
		} else {
			viewHolder.draftsFlag.setVisibility(View.GONE);
		}
		if (conversation.getMsg() == null || conversation.getMsg().equals("")
				|| conversation.getSendStatus() == MsgInfo.SEND_STATUS_SUCCESS) {
			viewHolder.msgIcon2.setVisibility(View.GONE);
		} else if (conversation.getSendStatus() == MsgInfo.SEND_STATUS_UNKNOW) {
			viewHolder.msgIcon2.setVisibility(View.VISIBLE);
			viewHolder.msgIcon2.setImageResource(ResDrawable
					.getDrawable_wm_conversation_sending_flag());
		} else if (conversation.getSendStatus() == MsgInfo.SEND_STATUS_FAILED) {
			viewHolder.msgIcon2.setVisibility(View.VISIBLE);
			viewHolder.msgIcon2.setImageResource(ResDrawable
					.getDrawable_wm_s_group_icon_warning());
		}

		if (conversation.getSticktime() != null
				&& !conversation.getSticktime().equals("0")
				&& !conversation.getSticktime().equals("")) {// 置顶
			viewHolder.rootLayout.setBackgroundResource(ResDrawable
					.getDrawable_wm_conversation_top_item_bg());
		} else {
			viewHolder.rootLayout.setBackgroundResource(ResDrawable
					.getDrawable_wm_conversation_item_bg());
		}

		if (conversation.getMsg_type() == 4
				&& conversation.isAudioIsRead() == false) {
			viewHolder.text.setTextColor(mActivity.getResources().getColor(
					ResColor.getColor_wm_red()));
		} else {
			viewHolder.text.setTextColor(mActivity.getResources().getColor(
					ResColor.getColor_wm_vice_title()));
		}

		int msgIconRes = ChatUtil.getChatMsgContentIconDrawableId(
				conversation.getMsg_type(), conversation.isAudioIsRead());
		if (msgIconRes > 0 && conversation.getMsg() != null
				&& !conversation.getMsg().equals("")) {
			viewHolder.msgIcon.setVisibility(View.VISIBLE);
			viewHolder.msgIcon.setImageResource(msgIconRes);
		} else {
			viewHolder.msgIcon.setVisibility(View.GONE);
		}

		viewHolder.unreadPoint.setVisibility(View.GONE);
		viewHolder.count.setBackgroundResource(ResDrawable
				.getDrawable_wm_message_ico_red());
		viewHolder.time.setText(TimeUtil.genBriefTime(conversation
				.getTimestamp()));

		if (conversation.getCount() > 0) {
			viewHolder.count.setVisibility(View.VISIBLE);
			viewHolder.count.setText(StringUtil
					.parseMsgUnreadCount(conversation.getCount()));
		} else {
			viewHolder.count.setVisibility(View.GONE);
		}
		viewHolder.name.setText("");
		CommonImageUtil.loadImage("drawable://"
				+ Consts.USER_AVATAR_RES_DEFAULT, viewHolder.avatar,
				userAvatarLoadOptions, null);
		String txt = "";
		txt = ChatUtil.getConversationContent(conversation, false, null,
				GroupUserProps.SHOW_MARK_UNKNOW);
		viewHolder.text.setText(ExpEmojiUtil.getInstance(mActivity)
				.replaceSmall(txt));

		return convertView;
	}

	class ViewHolder {
		ImageView avatar;
		TextView name;
		TextView text;
		TextView count;
		TextView time;
		View rootLayout;
		ImageView msgIcon;
		View privateIcon;
		View unreadPoint;
		ImageView draftsFlag;
		ImageView msgIcon2;
	}

}
