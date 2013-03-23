package net.dev123.yibo.service.adapter;

import net.dev123.commons.ServiceProvider;
import net.dev123.yibo.R;
import net.dev123.yibo.common.theme.Theme;
import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.service.task.ImageLoad4HeadTask;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class AccountSelectorHolder {
	ImageView ivProfilePicture;
	TextView tvScreenName;
	TextView tvSPName;
	CheckBox cbAccountSelector;
	
	ImageLoad4HeadTask headTask;
	Theme theme;
	public AccountSelectorHolder(View convertView) {
		if (convertView == null) {
			throw new IllegalArgumentException("convertView is null!");
		}
		ivProfilePicture = (ImageView)convertView.findViewById(R.id.ivProfileImage);
		tvScreenName = (TextView)convertView.findViewById(R.id.tvScreenName);
		tvSPName = (TextView)convertView.findViewById(R.id.tvSPName);
		cbAccountSelector = (CheckBox)convertView.findViewById(R.id.cbAccountSelector);
		
		//设置主题 
		theme = ThemeUtil.createTheme(convertView.getContext());
		tvScreenName.setTextColor(theme.getColor("content"));
		tvSPName.setTextColor(theme.getColor("quote"));
		cbAccountSelector.setButtonDrawable(theme.getDrawable("selector_checkbox"));
	}
	
	public void reset(ServiceProvider sp) {
		if (ivProfilePicture != null) {
			Drawable drawable = null;
			switch (sp) {
			case Sina:
				drawable = theme.getDrawable("icon_logo_sina_min");
				break;
			case Sohu:
				drawable = theme.getDrawable("icon_logo_sohu_min");
				break;
			case NetEase:
				drawable = theme.getDrawable("icon_logo_netease_min");
				break;
			case Tencent:
				drawable = theme.getDrawable("icon_logo_tencent_min");
				break;
			case Twitter:
				drawable = theme.getDrawable("icon_logo_twitter_min");
				break;
			case Fanfou:
				drawable = theme.getDrawable("icon_logo_fanfou_min");
				break;
			case RenRen:
				drawable = theme.getDrawable("icon_logo_renren_min");
				break;
			case KaiXin:
				drawable = theme.getDrawable("icon_logo_kaixin_min");
				break;
			case QQZone:
				drawable = theme.getDrawable("icon_logo_qqzone_min");
				break;
			default:
				drawable = theme.getDrawable("icon_header_default_min");
				break;
			}
			ivProfilePicture.setImageDrawable(drawable);
		}
		
		if (tvScreenName != null) {
			tvScreenName.setText("");
		}
		if (tvSPName != null) {
			tvSPName.setText("");
		}
	}
}
