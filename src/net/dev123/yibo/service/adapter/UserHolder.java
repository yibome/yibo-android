package net.dev123.yibo.service.adapter;

import net.dev123.yibo.R;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.GlobalResource;
import net.dev123.yibo.common.theme.Theme;
import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.service.task.ImageLoad4HeadTask;
import net.dev123.yibo.service.task.RelationshipCheckTask;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class UserHolder {
	private static final String TAG = "UserHolder";
	private Context context;
	ImageView ivProfilePicture;
	TextView tvScreenName;
	ImageView ivVerify;
	TextView tvImpress;
	ImageView ivFriendship;
	Button btnOperate;

	ImageLoad4HeadTask headTask;
	RelationshipCheckTask relationshipCheckTask;
	public UserHolder(View convertView) {
		if (convertView == null) {
			throw new IllegalArgumentException("convertView is null!");
		}
		context = convertView.getContext();
		ivProfilePicture = (ImageView) convertView.findViewById(R.id.ivProfilePicture);
		tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
		ivVerify = (ImageView) convertView.findViewById(R.id.ivVerify);
		tvImpress = (TextView) convertView.findViewById(R.id.tvImpress);
		ivFriendship = (ImageView) convertView.findViewById(R.id.ivFriendship);
		btnOperate = (Button) convertView.findViewById(R.id.btnSocialGraphOperate);
		
		Theme theme = ThemeUtil.createTheme(context);
		tvScreenName.setTextColor(theme.getColor("content"));
		ivVerify.setImageDrawable(theme.getDrawable("icon_verification"));
		tvImpress.setTextColor(theme.getColor("remark"));
		if (ivFriendship != null) {
		    ivFriendship.setBackgroundDrawable(theme.getDrawable("icon_friendship"));
		}
		ThemeUtil.setBtnActionNegative(btnOperate);
	}

	public void reset() {
		if (ivProfilePicture != null) {
			ivProfilePicture.setImageDrawable(GlobalResource.getDefaultMinHeader(context));
		}

		if (tvScreenName != null) {
			tvScreenName.setText("");
		}

		if (ivVerify != null) {
			ivVerify.setVisibility(View.GONE);
		}

		if (tvImpress != null) {
			tvImpress.setText("");
		}

		if (ivFriendship != null) {
			ivFriendship.setVisibility(View.GONE);
		}

		if (btnOperate != null) {
			btnOperate.setVisibility(View.VISIBLE);
			btnOperate.setText(R.string.btn_loading);
			btnOperate.setTextAppearance(btnOperate.getContext(), R.style.btn_action_negative);
			ThemeUtil.setBtnActionNegative(btnOperate);
			btnOperate.setEnabled(false);
		}

		headTask = null;
		relationshipCheckTask = null;
	}

	public void recycle() {
		if (headTask != null) {
			headTask.cancel(true);
		}
		if (relationshipCheckTask != null) {
			relationshipCheckTask.cancel(true);
		}
		if (Constants.DEBUG) Log.d(TAG, "user convertView recycle");
	}
}
