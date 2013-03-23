package net.dev123.yibo.service.listener;

import net.dev123.mblog.entity.User;
import net.dev123.yibo.ProfileActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public 	class ImageHeadClickListener implements View.OnClickListener {
	private User user;
	public ImageHeadClickListener() {
	}
	
	@Override
	public void onClick(View v) {
		if (user == null) {
			return;
		}
	    Intent intent = new Intent();
	    Bundle bundle = new Bundle();
	        
		bundle.putSerializable("USER", user);
		intent.putExtras(bundle);

		Activity context = (Activity)v.getContext();
		intent.setClass(context, ProfileActivity.class);
		context.startActivity(intent);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
