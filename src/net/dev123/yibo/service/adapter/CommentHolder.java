package net.dev123.yibo.service.adapter;

import net.dev123.yibo.R;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.GlobalVars;
import net.dev123.yibo.common.GlobalResource;
import net.dev123.yibo.common.theme.Theme;
import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.service.task.ImageLoad4HeadTask;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentHolder {
	private static final String TAG = "CommentHolder";
	private Context context;
	ImageView ivProfilePicture;
	TextView tvScreenName;
	ImageView ivVerify;
	TextView tvCreatedAt;
	TextView tvText;
	TextView tvReplyText;

	ImageLoad4HeadTask headTask;

	public CommentHolder(View convertView) {
		if (convertView == null) {
			throw new IllegalArgumentException("convertView is null!");
		}
		context = convertView.getContext();
		ivProfilePicture = (ImageView) convertView.findViewById(R.id.ivProfilePicture);
		tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
		ivVerify = (ImageView) convertView.findViewById(R.id.ivVerify);
		tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
		tvText = (TextView) convertView.findViewById(R.id.tvText);		
		tvReplyText = (TextView) convertView.findViewById(R.id.tvReplyText);

		//设置主题 
        Theme theme = ThemeUtil.createTheme(context);
        tvScreenName.setTextColor(theme.getColor("highlight"));
        ivVerify.setImageDrawable(GlobalResource.getIconVerification(context));
        tvText.setTextColor(theme.getColor("content"));
        tvText.setLinkTextColor(theme.getColorStateList("selector_text_link"));
        tvReplyText.setTextColor(theme.getColor("quote"));
        tvReplyText.setLinkTextColor(theme.getColorStateList("selector_text_link"));
		tvReplyText.setBackgroundDrawable(GlobalResource.getBgRetweetFrame(context));
		tvReplyText.setPadding(theme.dip2px(10), theme.dip2px(12), 
	        theme.dip2px(10), theme.dip2px(6));
		
		reset();
	}

	public void reset() {
		if (tvCreatedAt != null) {
			tvCreatedAt.setText("");
    		tvCreatedAt.setTextColor(GlobalResource.getStatusTimelineReadColor(context));
		}
		if (ivProfilePicture != null) {
			ivProfilePicture.setVisibility(View.GONE);
			ivProfilePicture.setImageDrawable(GlobalResource.getDefaultMinHeader(context));
		}
		if (ivVerify != null) {
			ivVerify.setVisibility(View.GONE);
		}
		if (tvText != null) {
			tvText.setText("");
    		if (tvText.getTextSize() != GlobalVars.FONT_SIZE_HOME_BLOG) {
    		    tvText.setTextSize(GlobalVars.FONT_SIZE_HOME_BLOG);
    		    if (Constants.DEBUG) Log.d(TAG, "tweet FontSize: " + GlobalVars.FONT_SIZE_HOME_BLOG);
    	    }
		}
		if (tvReplyText != null) {
			tvReplyText.setText("");
			if (tvReplyText.getTextSize() != GlobalVars.FONT_SIZE_HOME_RETWEET) {
				tvReplyText.setTextSize(GlobalVars.FONT_SIZE_HOME_RETWEET);
			}
		}
		headTask = null;
	}

	public void recycle() {
		if (headTask != null) {
			headTask.cancel(true);
		}
		headTask = null;
		if (Constants.DEBUG) Log.d(TAG, "comment convertView recycle");
	}
}
