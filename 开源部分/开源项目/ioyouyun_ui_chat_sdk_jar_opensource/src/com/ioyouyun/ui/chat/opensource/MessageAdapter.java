package com.ioyouyun.ui.chat.opensource;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ioyouyun.ui.chat.bean.ChatPicInfo;
import com.ioyouyun.ui.chat.bean.MsgInfo;
import com.ioyouyun.ui.chat.core.chat.util.ChatUtil;
import com.ioyouyun.ui.chat.core.util.CommonImageUtil;
import com.ioyouyun.ui.chat.core.util.ExpEmojiUtil;
import com.ioyouyun.ui.chat.core.util.StringUtil;
import com.ioyouyun.ui.chat.core.util.TextViewLinkClickUtil;
import com.ioyouyun.ui.chat.core.util.TimeUtil;
import com.ioyouyun.ui.chat.lib.ResDrawable;
import com.ioyouyun.ui.chat.lib.ResId;
import com.ioyouyun.ui.chat.lib.ResLayout;
import com.ioyouyun.ui.chat.lib.ResString;
import com.ioyouyun.ui.chat.view.MaskView;
import com.ioyouyun.ui.chat.view.MaskViewSize;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class MessageAdapter extends MessageParentAdapter{

	public MessageAdapter(ListView lv, Activity activity) {
		super(activity);
		init(lv);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		int viewType = getItemViewType(position);
		final MsgInfo info = (MsgInfo) getItem(position);
		if (viewType == 0) {
			TextHolder holder;
			if (convertView == null) {
				holder = new TextHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_in_text_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.text = (TextView) convertView.findViewById(ResId
						.getId_text());
				holder.text.setMaxWidth(screenWidth7p10);
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				convertView.setTag(holder);
			} else {
				holder = (TextHolder) convertView.getTag();
			}
			setItemContentClickListener(holder.text, position);
			setItemContentLongClickListener(holder.text, position);
			holder.text.setText(ExpEmojiUtil.getInstance(mActivity)
					.replaceSmall(info.getMsg()));
			TextViewLinkClickUtil.setTextViewLinkClick(mActivity, holder.text);
			setCommonLeftView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name);
		} else if (viewType == 1) {
			TextHolder holder;
			if (convertView == null) {
				holder = new TextHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_out_text_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.text = (TextView) convertView.findViewById(ResId
						.getId_text());
				holder.text.setMaxWidth(screenWidth7p10);
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				holder.sending = convertView
						.findViewById(ResId.getId_sending());
				convertView.setTag(holder);
			} else {
				holder = (TextHolder) convertView.getTag();
			}
			setItemContentClickListener(holder.text, position);
			setItemContentLongClickListener(holder.text, position);
			holder.text.setText(ExpEmojiUtil.getInstance(mActivity)
					.replaceSmall(info.getMsg()));
			TextViewLinkClickUtil.setTextViewLinkClick(mActivity, holder.text);
			setcommonRightView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name, holder.failIcon,
					holder.sending);
		} else if (viewType == 2) {
			AudioHolder holder;
			if (convertView == null) {
				holder = new AudioHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_in_audio_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.audio = (TextView) convertView.findViewById(ResId
						.getId_audio());
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.duration = (TextView) convertView.findViewById(ResId
						.getId_duration());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.unreadFlag = (ImageView) convertView.findViewById(ResId
						.getId_unread_flag());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				convertView.setTag(holder);
			} else {
				holder = (AudioHolder) convertView.getTag();
			}
			setItemContentClickListener(holder.audio, position);
			setItemContentLongClickListener(holder.audio, position);
			if (info.getAudio_readtime() == null
					|| info.getAudio_readtime().equals("")) {
				holder.unreadFlag.setVisibility(View.VISIBLE);
			} else {
				holder.unreadFlag.setVisibility(View.GONE);
			}

			holder.audio.setTag("left");
			if (info.isAudioPlaying()) {
				holder.audio.setSelected(true);
				setAudioDrawableAnim(holder.audio,
						ResDrawable.getDrawable_wm_chating_audio_anim_left(), 1);
			} else {
				holder.audio.setSelected(false);
				setAudioDrawableUnAnim(holder.audio,
						ResDrawable.getDrawable_wm_s_chat_icon_speech_left3(),
						1);
			}

			try {
				int audioLength = Integer.parseInt(info.getExtra());
				StringUtil.setAudioLength(holder.audio, audioLength);
			} catch (Exception e) {

			}
			holder.duration.setText(info.getExtra() + "\"");
			setCommonLeftView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name);
		} else if (viewType == 3) {
			AudioHolder holder;
			if (convertView == null) {
				holder = new AudioHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_out_audio_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.audio = (TextView) convertView.findViewById(ResId
						.getId_audio());
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.duration = (TextView) convertView.findViewById(ResId
						.getId_duration());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				holder.sending = convertView
						.findViewById(ResId.getId_sending());
				convertView.setTag(holder);
			} else {
				holder = (AudioHolder) convertView.getTag();
			}
			setItemContentClickListener(holder.audio, position);
			setItemContentLongClickListener(holder.audio, position);
			holder.audio.setTag("right");
			if (info.isAudioPlaying()) {
				holder.audio.setSelected(true);
				setAudioDrawableAnim(holder.audio,
						ResDrawable.getDrawable_wm_chating_audio_anim_right(),
						2);
			} else {
				holder.audio.setSelected(false);
				setAudioDrawableUnAnim(holder.audio,
						ResDrawable.getDrawable_wm_s_chat_icon_speech_right3(),
						2);
			}

			try {
				int audioLength = Integer.parseInt(info.getExtra());
				StringUtil.setAudioLength(holder.audio, audioLength);
			} catch (Exception e) {

			}
			holder.duration.setText(info.getExtra() + "\"");
			setcommonRightView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name, holder.failIcon,
					holder.sending);
		} else if (viewType == 4) {
			ImageHolder holder;
			if (convertView == null) {
				holder = new ImageHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_in_img_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.imgProgress = (ProgressBar) convertView
						.findViewById(ResId.getId_img_progress());
				holder.image_layout = convertView.findViewById(ResId
						.getId_image_layout());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				holder.praiseFalse = convertView.findViewById(ResId
						.getId_praise_false());
				holder.praiseTrue = convertView.findViewById(ResId
						.getId_praise_true());
				holder.imgParent = (ViewGroup) convertView.findViewById(ResId
						.getId_img_parent());
				holder.imgDefault = (ImageView) convertView.findViewById(ResId
						.getId_img_default());
				convertView.setTag(holder);
			} else {
				holder = (ImageHolder) convertView.getTag();
			}
			setItemContentClickListener(holder.image_layout, position);
			setItemContentLongClickListener(holder.image_layout, position);
			setCommonLeftView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name);
			String fileUri = null;
			if (info.getThumbnailPath() != null
					&& !info.getThumbnailPath().equals("")) {// 存在缩略�?
				fileUri = "file://" + info.getThumbnailPath();
			} else {
				ChatPicInfo chatPicInfo = ChatPicInfo.getInfo(info.getMsg());
				if (chatPicInfo != null && chatPicInfo.local != null) {
					fileUri = "file://" + chatPicInfo.local;
				}
			}
			holder.imgDefault.setVisibility(View.VISIBLE);
			holder.imgParent.setVisibility(View.GONE);
			if (fileUri != null && !fileUri.equals("")) {
				File cacheFile = ImageLoader.getInstance().getDiscCache()
						.get(fileUri);
				if (cacheFile != null && cacheFile.exists()) {
					MaskViewSize viewSize = MaskView
							.caculateMaskViewSize(
									cacheFile.getPath(),
									(NinePatchDrawable) mActivity
											.getResources()
											.getDrawable(
													ResDrawable
															.getDrawable_wm_chat_img_left_mask()),
									screenWidth1p3, screenWidth1p3,
									screenWidth1p4, screenWidth1p4);
					if (viewSize.viewWidth > 0 && viewSize.viewHeight > 0) {
						ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
								.getLayoutParams();
						layoutParams.height = viewSize.viewHeight;
						layoutParams.width = viewSize.viewWidth;
					} else {
						ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
								.getLayoutParams();
						layoutParams.height = screenWidth1p3;
						layoutParams.width = screenWidth1p3;
					}
				} else {
					ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
							.getLayoutParams();
					layoutParams.height = screenWidth1p3;
					layoutParams.width = screenWidth1p3;
				}
				final View f_convertView = convertView;
				final String f_fileUri = fileUri;
				holder.imgParent.setTag("imgParent" + f_fileUri);
				holder.imgDefault.setTag("imgDefault" + f_fileUri);
				CommonImageUtil.loadImage(fileUri, new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null && f_convertView != null) {
							View v = f_convertView.findViewWithTag("imgParent"
									+ f_fileUri);
							View imgDefault = f_convertView
									.findViewWithTag("imgDefault" + f_fileUri);
							if (v != null && imgDefault != null) {
								ViewGroup imgParent = (ViewGroup) v;
								imgDefault.setVisibility(View.GONE);
								imgParent.setVisibility(View.VISIBLE);
								MaskView imgView = new MaskView(
										mActivity,
										loadedImage,
										(NinePatchDrawable) mActivity
												.getResources()
												.getDrawable(
														ResDrawable
																.getDrawable_wm_chat_img_left_mask()),
										screenWidth1p3, screenWidth1p3,
										screenWidth1p4, screenWidth1p4);
								imgParent.removeAllViews();
								imgParent.addView(imgView);
								ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) imgView
										.getLayoutParams();
								layoutParams.height = imgView.getMaskViewSize() != null ? imgView
										.getMaskViewSize().viewHeight
										: layoutParams.height;
								layoutParams.width = imgView.getMaskViewSize() != null ? imgView
										.getMaskViewSize().viewWidth
										: layoutParams.width;

							}
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
					}
				});
			} else {
				ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
						.getLayoutParams();
				layoutParams.height = screenWidth1p3;
				layoutParams.width = screenWidth1p3;
			}

			holder.imgProgress.setVisibility(View.GONE);
		} else if (viewType == 5) {
			ImageHolder holder;
			if (convertView == null) {
				holder = new ImageHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_out_img_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.imgProgress = (ProgressBar) convertView
						.findViewById(ResId.getId_img_progress());
				holder.image_layout = convertView.findViewById(ResId
						.getId_image_layout());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				holder.sending = convertView
						.findViewById(ResId.getId_sending());
				holder.imgParent = (ViewGroup) convertView.findViewById(ResId
						.getId_img_parent());
				holder.imgDefault = (ImageView) convertView.findViewById(ResId
						.getId_img_default());
				convertView.setTag(holder);
			} else {
				holder = (ImageHolder) convertView.getTag();
			}
			setItemContentClickListener(holder.image_layout, position);
			setItemContentLongClickListener(holder.image_layout, position);
			setcommonRightView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name, holder.failIcon,
					holder.sending);
			holder.imgProgress.setTag("progress_" + info.getMsg_id());
			Integer progressVal = fileUploadProgress.get(info.getMsg_id());
			if (progressVal != null && progressVal < 100) {
				holder.imgProgress.setVisibility(View.VISIBLE);
				holder.imgProgress.setProgress(progressVal);
			} else {
				holder.imgProgress.setVisibility(View.GONE);
			}

			String fileUri = null;
			if (info.getThumbnailPath() != null
					&& !info.getThumbnailPath().equals("")) {// 存在缩略�?
				fileUri = "file://" + info.getThumbnailPath();
			} else {
				ChatPicInfo chatPicInfo = ChatPicInfo.getInfo(info.getMsg());
				if (chatPicInfo != null && chatPicInfo.local != null) {
					fileUri = "file://" + chatPicInfo.local;
				}
			}

			holder.imgDefault.setVisibility(View.VISIBLE);
			holder.imgParent.setVisibility(View.GONE);
			if (fileUri != null && !fileUri.equals("")) {
				File cacheFile = ImageLoader.getInstance().getDiscCache()
						.get(fileUri);
				if (cacheFile != null && cacheFile.exists()) {
					MaskViewSize viewSize = MaskView
							.caculateMaskViewSize(
									cacheFile.getPath(),
									(NinePatchDrawable) mActivity
											.getResources()
											.getDrawable(
													ResDrawable
															.getDrawable_wm_chat_img_right_mask()),
									screenWidth1p3, screenWidth1p3,
									screenWidth1p4, screenWidth1p4);
					if (viewSize.viewWidth > 0 && viewSize.viewHeight > 0) {
						ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
								.getLayoutParams();
						layoutParams.height = viewSize.viewHeight;
						layoutParams.width = viewSize.viewWidth;
					} else {
						ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
								.getLayoutParams();
						layoutParams.height = screenWidth1p3;
						layoutParams.width = screenWidth1p3;
					}
				} else {
					ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
							.getLayoutParams();
					layoutParams.height = screenWidth1p3;
					layoutParams.width = screenWidth1p3;
				}
				final View f_convertView = convertView;
				final String f_fileUri = fileUri;
				holder.imgParent.setTag("imgParent" + f_fileUri);
				holder.imgDefault.setTag("imgDefault" + f_fileUri);
				CommonImageUtil.loadImage(fileUri, new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String imageUri, View view) {
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						if (loadedImage != null && f_convertView != null) {
							View v = f_convertView.findViewWithTag("imgParent"
									+ f_fileUri);
							View imgDefault = f_convertView
									.findViewWithTag("imgDefault" + f_fileUri);
							if (v != null && imgDefault != null) {
								ViewGroup imgParent = (ViewGroup) v;
								imgDefault.setVisibility(View.GONE);
								imgParent.setVisibility(View.VISIBLE);
								MaskView imgView = new MaskView(
										mActivity,
										loadedImage,
										(NinePatchDrawable) mActivity
												.getResources()
												.getDrawable(
														ResDrawable
																.getDrawable_wm_chat_img_right_mask()),
										screenWidth1p3, screenWidth1p3,
										screenWidth1p4, screenWidth1p4);
								imgParent.removeAllViews();
								imgParent.addView(imgView);
								ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) imgView
										.getLayoutParams();
								layoutParams.height = imgView.getMaskViewSize() != null ? imgView
										.getMaskViewSize().viewHeight
										: layoutParams.height;
								layoutParams.width = imgView.getMaskViewSize() != null ? imgView
										.getMaskViewSize().viewWidth
										: layoutParams.width;
							}
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {

					}
				});
			} else {
				ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) holder.imgDefault
						.getLayoutParams();
				layoutParams.height = screenWidth1p3;
				layoutParams.width = screenWidth1p3;
			}
		} else if (viewType == 6) {// 时间线
			TimeHolder holder;
			if (convertView == null) {
				holder = new TimeHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_chat_time_line(), null);
				holder.time = (TextView) convertView.findViewById(ResId
						.getId_time());
				convertView.setTag(holder);
			} else {
				holder = (TimeHolder) convertView.getTag();
			}
			holder.time.setText(TimeUtil.genFullTime(Long.parseLong(info
					.getTimestamp())));
		} else if (viewType == 7) {// 系统事件消息
			SysmsgHolder holder;
			if (convertView == null) {
				holder = new SysmsgHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_chat_sysmsg_line(), null);
				holder.sysmsg_txt = (TextView) convertView.findViewById(ResId
						.getId_sysmsg_txt());
				convertView.setTag(holder);
			} else {
				holder = (SysmsgHolder) convertView.getTag();
			}
			holder.sysmsg_txt.setText(ExpEmojiUtil.getInstance(mActivity)
					.replaceSmall(ChatUtil.getSysMsgBodyDesc(info.getMsg())));
		} else if (viewType == 8) {
			TextHolder holder;
			if (convertView == null) {
				holder = new TextHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_in_text_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.text = (TextView) convertView.findViewById(ResId
						.getId_text());
				holder.text.setMaxWidth(screenWidth7p10);
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				convertView.setTag(holder);
			} else {
				holder = (TextHolder) convertView.getTag();
			}
			
			setItemContentLongClickListener(holder.text, position);
			
			//custom msg content show
			holder.text.setText(info.getMsg());

			setCommonLeftView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name);
		} else if (viewType == 9) {
			TextHolder holder;
			if (convertView == null) {
				holder = new TextHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_out_text_msg(), null);
				holder.avatar = (ImageView) convertView.findViewById(ResId
						.getId_avatar());
				holder.text = (TextView) convertView.findViewById(ResId
						.getId_text());
				holder.text.setMaxWidth(screenWidth7p10);
				holder.failIcon = (ImageView) convertView.findViewById(ResId
						.getId_fail_icon());
				holder.avatarPoint = (ImageView) convertView.findViewById(ResId
						.getId_avatar_point());
				holder.avatarLineTop = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_top());
				holder.avatarLineBottom = (ImageView) convertView
						.findViewById(ResId.getId_avatar_line_bottom());
				holder.name = (TextView) convertView.findViewById(ResId
						.getId_name());
				holder.sending = convertView
						.findViewById(ResId.getId_sending());
				convertView.setTag(holder);
			} else {
				holder = (TextHolder) convertView.getTag();
			}
			
			setItemContentLongClickListener(holder.text, position);
			
			//custom msg content show
			holder.text.setText(info.getMsg());
			
			setcommonRightView(info, position, holder.avatar,
					holder.avatarPoint, holder.avatarLineTop,
					holder.avatarLineBottom, holder.name, holder.failIcon,
					holder.sending);
		} else { // 不兼容的消息
			SysmsgHolder holder;
			if (convertView == null) {
				holder = new SysmsgHolder();
				convertView = mLayoutInflater.inflate(
						ResLayout.getLayout_wm_chat_sysmsg_line(), null);
				holder.sysmsg_txt = (TextView) convertView.findViewById(ResId
						.getId_sysmsg_txt());
				convertView.setTag(holder);
			} else {
				holder = (SysmsgHolder) convertView.getTag();
			}
			holder.sysmsg_txt
					.setText(ResString.getString_wm_incompatible_msg());
		}
		return convertView;
	}

	class SysmsgHolder {
		TextView sysmsg_txt;
	}

	class TextHolder {
		TextView text;
		ImageView avatar;
		ImageView failIcon;
		ImageView avatarPoint;
		ImageView avatarLineTop;
		ImageView avatarLineBottom;
		TextView name;
		View sending;
	}

	class ImageHolder {
		ImageView avatar;
		ImageView failIcon;
		ImageView avatarPoint;
		ImageView avatarLineTop;
		ImageView avatarLineBottom;
		TextView name;
		View sending;
		ProgressBar imgProgress;
		View image_layout;
		View praiseFalse;
		View praiseTrue;
		ViewGroup imgParent;
		ImageView imgDefault;
	}

	class AudioHolder {
		TextView audio;
		TextView duration;
		ImageView unreadFlag;
		ImageView avatar;
		ImageView failIcon;
		ImageView avatarPoint;
		ImageView avatarLineTop;
		ImageView avatarLineBottom;
		TextView name;
		View sending;
	}

	class TimeHolder {
		TextView time;
	}

	
}
