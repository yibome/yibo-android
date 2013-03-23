package net.dev123.yibo.service.task;

import java.util.List;

import net.dev123.commons.Paging;
import net.dev123.commons.util.ListUtil;
import net.dev123.entity.BaseUser;
import net.dev123.exception.LibException;
import net.dev123.mblog.MicroBlog;
import net.dev123.mblog.entity.User;
import net.dev123.yibo.UserQuickSelectorActivity;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.GlobalVars;
import net.dev123.yibo.common.ResourceBook;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.db.Relation;
import net.dev123.yibo.db.SocialGraphDao;
import net.dev123.yibo.service.adapter.UserQuickSelectorListAdapter;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UserQuickSelectorTask extends AsyncTask<Void, Void, List<? extends BaseUser>> {
	private static final String TAG = "UserQuickSelectorTask";
	private MicroBlog microBlog = null;

	private UserQuickSelectorActivity context;
	private UserQuickSelectorListAdapter adapter = null;
	private Paging<User> paging;

	private LocalAccount account;
	private Relation relation = Relation.Followingship;
	private User user;

	private String resultMsg;
	public UserQuickSelectorTask(UserQuickSelectorListAdapter adapter, Relation relation) {
		this.adapter = adapter;
		this.context = (UserQuickSelectorActivity)adapter.getContext();
		this.relation = relation;
		this.account = adapter.getAccount();
		this.user = (User) account.getUser();
		this.microBlog = GlobalVars.getMicroBlog(account);
	}

	@Override
	protected void onPreExecute() {
		context.showLoadingFooter();
	}

	@Override
	protected List<? extends BaseUser> doInBackground(Void... params) {
		if (adapter == null || microBlog == null) {
			return null;
		}

	    List<? extends BaseUser> userList = null;
	    boolean isFirstLoad = context.isFirstLoad();
	    SocialGraphDao dao = new SocialGraphDao(context);
		try {
			paging = adapter.getPaging();
			if (!isFirstLoad && paging.moveToNext()) {
				if (relation == Relation.Followingship) {
					userList = dao.getFriends(user, paging);
				} else {
					userList = dao.getFollowers(user, paging);
				}
				if (paging.getPageIndex() == 1 && ListUtil.isEmpty(userList)) {
					isFirstLoad = true;
					context.setFirstLoad(isFirstLoad);
				}
			}

			SocialGraphCacheTask cacheTask = null;
			if (isFirstLoad) {
				//第一次加载时，做一个缓冲task
				if (paging.getPageIndex() == 1 && adapter.getCount() == 0) {
					Paging<User> remotePaging = new Paging<User>();
					adapter.setPaging(remotePaging);
					//缓冲远程数据;
					cacheTask = new SocialGraphCacheTask(
						context, account, relation);
				}

				paging = adapter.getPaging();
				if (paging.moveToNext()) {
					if (relation == Relation.Followingship) {
						userList = microBlog.getFriends(paging);
					} else {
					    userList = microBlog.getFollowers(paging);
					}
				}
			} else if (paging.getPageIndex() == 1) {
				//防止新关注，缓冲第一次。
				cacheTask = new SocialGraphCacheTask(
						context, account, relation);
				cacheTask.setCycleTime(1);
				cacheTask.setPageSize(20);
			}
			if (cacheTask != null) {
			    cacheTask.execute();
			}
		} catch (LibException e) {
			if (Constants.DEBUG) Log.e(TAG, "Task", e);
			resultMsg = ResourceBook.getStatusCodeValue(e.getExceptionCode(), context);
			paging.moveToPrevious();
		}

		return userList;
	}

	@Override
	protected void onPostExecute(List<? extends BaseUser> result) {
		if (ListUtil.isNotEmpty(result)) {
			adapter.addCacheToDivider(null, (List<User>) result);
		} else {
			adapter.notifyDataSetChanged();

			if (resultMsg != null) {
				Toast.makeText(adapter.getContext(), resultMsg, Toast.LENGTH_LONG).show();
			}
		}

		if (paging.hasNext()) {
			((UserQuickSelectorActivity)context).showMoreFooter();
		} else {
			((UserQuickSelectorActivity)context).showNoMoreFooter();
		}

	}

}
