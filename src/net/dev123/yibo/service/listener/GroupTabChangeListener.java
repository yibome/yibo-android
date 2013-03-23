package net.dev123.yibo.service.listener;

import net.dev123.commons.Paging;
import net.dev123.yibo.GroupActivity;
import net.dev123.yibo.R;
import net.dev123.yibo.service.adapter.GroupListAdapter;
import net.dev123.yibo.service.adapter.SocialGraphListAdapter;
import net.dev123.yibo.service.task.GroupTask;
import net.dev123.yibo.service.task.SocialGraphTask;
import net.dev123.yibo.widget.TabButton.OnTabChangeListener;
import android.view.View;
import android.widget.ListView;

public class GroupTabChangeListener implements OnTabChangeListener {
    private GroupActivity context;
    private SocialGraphItemClickListener socialGraphItemClickListener;
    private GroupItemClickListener groupItemClickListener;
    private GroupContextMenuListener groupContextMenuListener;
	public GroupTabChangeListener(GroupActivity context) {
		this.context = context;
		this.groupItemClickListener = new GroupItemClickListener(context);
		this.socialGraphItemClickListener = new SocialGraphItemClickListener(context);
	}
	
	@Override
	public void onTabChange(View v, int which) {
		ListView lvUser = (ListView) context.findViewById(R.id.lvUser);
		Paging<?> paging = null;
		boolean isInit = false;
		switch (which) {
		case 0:
			SocialGraphListAdapter sgAdapter = context.getSgAdapter();
			if (sgAdapter == null) {
				sgAdapter = new SocialGraphListAdapter(context, 
					context.getCurrentAccount(), context.getSocialGraphType());
				context.setSgAdapter(sgAdapter);
				new SocialGraphTask(sgAdapter, context.getUser()).execute();
				isInit = true;
			}
			paging = sgAdapter.getPaging();
			lvUser.setAdapter(sgAdapter);
			lvUser.setOnItemClickListener(socialGraphItemClickListener);
            lvUser.setOnCreateContextMenuListener(null);
			break;
		case 1:
			GroupListAdapter groupAdapter = context.getGroupAdapter();
			if (groupAdapter == null) {
				groupAdapter = new GroupListAdapter(context, context.getCurrentAccount());
				context.setGroupAdapter(groupAdapter);
				groupContextMenuListener = new GroupContextMenuListener(groupAdapter);
				
				new GroupTask(groupAdapter).execute();
				isInit = true;
			}
			paging = groupAdapter.getPaging();
			lvUser.setAdapter(groupAdapter);
			lvUser.setOnItemClickListener(groupItemClickListener);
			lvUser.setOnCreateContextMenuListener(groupContextMenuListener);
			break;
		}
		
		if (isInit) {
			return;
		}
	    if (paging.hasNext()) {
	    	context.showMoreFooter();
	    } else {
	    	context.showNoMoreFooter();
	    }
	}

}
