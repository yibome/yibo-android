package net.dev123.yibo.service.adapter;

import net.dev123.yibo.R;
import net.dev123.yibo.common.theme.Theme;
import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.service.listener.CommentsOfStatusReplyClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CommentOfStatusHolder {

	TextView tvScreenName;
	TextView tvCreatedAt;
	TextView tvText;
	Button btnCommentReply;
	
	CommentsOfStatusReplyClickListener replyClickListener;
	public CommentOfStatusHolder(View convertView) {
		if (convertView == null) {
			throw new IllegalArgumentException("convertView is null!");
		}
		
		tvScreenName = (TextView) convertView.findViewById(R.id.tvScreenName);
		tvCreatedAt = (TextView) convertView.findViewById(R.id.tvCreatedAt);
		tvText = (TextView) convertView.findViewById(R.id.tvText);
		btnCommentReply = (Button) convertView.findViewById(R.id.btnCommentReply);
		replyClickListener = new CommentsOfStatusReplyClickListener();
        btnCommentReply.setOnClickListener(replyClickListener);
        
        //主题设置
        Theme theme = ThemeUtil.createTheme(convertView.getContext());
        tvScreenName.setTextColor(theme.getColor("highlight"));
        tvCreatedAt.setTextColor(theme.getColor("comment_time"));
        tvText.setTextColor(theme.getColor("content"));
        tvText.setLinkTextColor(theme.getColorStateList("selector_text_link"));
        btnCommentReply.setBackgroundDrawable(theme.getDrawable("selector_btn_comment_reply"));
        
		reset();
	}
	
	public void reset() {
		if (tvScreenName != null) {
			tvScreenName.setText("");
		}
		if (tvCreatedAt != null) {
			tvCreatedAt.setText("");
		}
		if (tvText != null) {
			tvText.setText("");
		}
		if (replyClickListener != null) {
			replyClickListener.setComment(null);
		}
	}
	
	public void recycle() {
	}
}
