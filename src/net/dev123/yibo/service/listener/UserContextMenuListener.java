package net.dev123.yibo.service.listener;

import net.dev123.mblog.entity.User;
import net.dev123.yibo.EditDirectMessageActivity;
import net.dev123.yibo.ProfileActivity;
import net.dev123.yibo.R;
import net.dev123.yibo.service.adapter.AdapterUtil;
import net.dev123.yibo.service.adapter.CacheAdapter;
import net.dev123.yibo.service.task.GroupMemberUnfollowTask;
import android.content.Context;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

public class UserContextMenuListener implements
		OnCreateContextMenuListener {
	private ListView lvUser;

    private int position;

	public UserContextMenuListener(ListView lvUser) {
		this.lvUser = lvUser;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		Adapter adapter = lvUser.getAdapter();
		this.position = info.position;
		User user= (User)adapter.getItem(position);
		if (user == null) {
			return;
		}

		Context context = v.getContext();
		analyzeUserMenu(adapter, user, menu, context);
	}

	private void analyzeUserMenu(final Adapter adapter, final User user, ContextMenu menu, final Context context) {
		menu.setHeaderTitle(R.string.menu_title_blog);
	    int order = 0;

	    MenuItem commentMenu = menu.add(0, 1, order++, R.string.menu_group_member_user_profile);
	    Intent commentIntent = new Intent(context, ProfileActivity.class);
	    commentIntent.putExtra("USER", user);
	    commentMenu.setIntent(commentIntent);

	    MenuItem unfollowMenu = menu.add(0, 2, order++, R.string.menu_group_member_unfollow);
	    unfollowMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				CacheAdapter<User> cacheAdapter = (CacheAdapter<User>)AdapterUtil.getCacheAdapter(adapter);
				GroupMemberUnfollowTask task = new GroupMemberUnfollowTask(cacheAdapter, user);
				task.execute();
				return false;
			}
	    });
	    
	    MenuItem messageMenu = menu.add(0, 2, order++, R.string.menu_group_member_message);
	    Intent messageIntent = new Intent(context, EditDirectMessageActivity.class);
	    messageIntent.putExtra("DISPLAY_NAME", user.getDisplayName());
		messageMenu.setIntent(messageIntent);
	}
}
