package net.dev123.yibo.service.cache;

import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.service.adapter.CommentsListAdapter;
import net.dev123.yibo.service.adapter.DirectMessagesListAdapter;
import net.dev123.yibo.service.adapter.MentionsListAdapter;
import net.dev123.yibo.service.adapter.MyHomeListAdapter;
import net.dev123.yibo.service.cache.Cache;
import net.dev123.yibo.service.cache.ReclaimLevel;

public class AdapterCollectionCache implements Cache {
	private LocalAccount account;
	
	private MyHomeListAdapter myHomeListAdapter;
	private MentionsListAdapter mentionsListAdapter;
	private CommentsListAdapter commentsListAdapter;
	private DirectMessagesListAdapter directMessagesListAdapter;
	
	public AdapterCollectionCache(LocalAccount account) {
		this.account = account;
	}
	
	@Override
	public void flush() {
	}

	@Override
	public boolean reclaim(ReclaimLevel level) {
		if (level == null) {
			return false;
		}
		if (myHomeListAdapter != null) {
			myHomeListAdapter.reclaim(level);
		}
		if (mentionsListAdapter != null) {
			mentionsListAdapter.reclaim(level);
		}
		if (commentsListAdapter != null) {
			commentsListAdapter.reclaim(level);
		}
		if (directMessagesListAdapter != null) {
			directMessagesListAdapter.reclaim(level);
		}
		return true;
	}

	@Override
	public void clear() {
		if (myHomeListAdapter != null) {
			myHomeListAdapter.clear();
		}
		if (mentionsListAdapter != null) {
			mentionsListAdapter.clear();
		}
		if (commentsListAdapter != null) {
			commentsListAdapter.clear();
		}
		if (directMessagesListAdapter != null) {
			directMessagesListAdapter.clear();
		}
	}

	public LocalAccount getAccount() {
		return account;
	}

	public void setAccount(LocalAccount account) {
		this.account = account;
	}

	public MyHomeListAdapter getMyHomeListAdapter() {
		return myHomeListAdapter;
	}

	public void setMyHomeListAdapter(MyHomeListAdapter myHomeListAdapter) {
		this.myHomeListAdapter = myHomeListAdapter;
	}

	public MentionsListAdapter getMentionsListAdapter() {
		return mentionsListAdapter;
	}

	public void setMentionsListAdapter(MentionsListAdapter mentionsListAdapter) {
		this.mentionsListAdapter = mentionsListAdapter;
	}

	public CommentsListAdapter getCommentsListAdapter() {
		return commentsListAdapter;
	}

	public void setCommentsListAdapter(CommentsListAdapter commentsListAdapter) {
		this.commentsListAdapter = commentsListAdapter;
	}

	public DirectMessagesListAdapter getDirectMessagesListAdapter() {
		return directMessagesListAdapter;
	}

	public void setDirectMessagesListAdapter(
			DirectMessagesListAdapter directMessagesListAdapter) {
		this.directMessagesListAdapter = directMessagesListAdapter;
	}

}
