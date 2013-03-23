package net.dev123.yibo.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.dev123.commons.Paging;
import net.dev123.commons.util.ListUtil;
import net.dev123.exception.ExceptionCode;
import net.dev123.exception.LibException;
import net.dev123.mblog.MicroBlog;
import net.dev123.yibo.R;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.GlobalVars;
import net.dev123.yibo.common.NetType;
import net.dev123.yibo.common.NotificationEntity;
import net.dev123.yibo.common.ResourceBook;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.db.LocalStatus;
import net.dev123.yibo.service.adapter.MyHomeListAdapter;
import net.dev123.yibo.service.adapter.StatusUtil;
import net.dev123.yibo.widget.PullToRefreshListView;
import net.dev123.yibo.widget.Skeleton;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class MyHomePageUpTask extends AsyncTask<Void, Void, Boolean> {
	private static final String TAG = "MyHomePageUpTask";
	private static ReentrantLock lock = new ReentrantLock();
    private Context context;
	private MicroBlog microBlog = null;
	private MyHomeListAdapter adapter;
	private PullToRefreshListView listView;
	private List<net.dev123.mblog.entity.Status> listStatus;
    private String resultMsg = null;
    private boolean isAutoUpdate = false;
    private boolean isEmptyAdapter = false;
	public MyHomePageUpTask(MyHomeListAdapter adapter) {
		this.adapter = adapter;
		this.context = adapter.getContext();
		listStatus = new ArrayList<net.dev123.mblog.entity.Status>();

		microBlog = GlobalVars.getMicroBlog(adapter.getAccount());
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		isEmptyAdapter = (adapter.getMax() == null);
        if (GlobalVars.NET_TYPE == NetType.NONE) {
        	cancel(true);
        	if (!isAutoUpdate) {
        	    resultMsg = ResourceBook.getStatusCodeValue(ExceptionCode.NET_UNCONNECTED, context);
        	    onPostExecute(false);
        	}
        }
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		boolean isSuccess = false;
		if (microBlog == null) {
			return isSuccess;
		}

		if (isAutoUpdate) {
			lock.lock();
		} else {
			if (!lock.tryLock()) {
				resultMsg = context.getString(R.string.msg_update_conflict);
				return false;
			}
		}

		Paging<net.dev123.mblog.entity.Status> paging = new Paging<net.dev123.mblog.entity.Status>();
		paging.setPageSize(GlobalVars.UPDATE_COUNT);
		net.dev123.mblog.entity.Status since = adapter.getMax();
		if (since instanceof LocalStatus
			&& ((LocalStatus)since).isDivider()) {
			since = null;
		}
		paging.setGlobalSince(since);

		if (paging.hasNext()) {
			paging.moveToNext();
			try {
				List<net.dev123.mblog.entity.Status> statuses = microBlog.getHomeTimeline(paging);
				listStatus.addAll(statuses);
			} catch (LibException e) {
				if (Constants.DEBUG) Log.e(TAG, e.getMessage(), e);
				resultMsg = ResourceBook.getStatusCodeValue(e.getExceptionCode(), context);
			}
		}
		// Util.getResponseCounts(listStatus, microBlog);

		isSuccess = ListUtil.isNotEmpty(listStatus);
		if (isSuccess && paging.hasNext()) {
			LocalStatus localStatus = StatusUtil.createDividerStatus(listStatus, adapter.getAccount());
			listStatus.add(localStatus);
		}
		//添加到适配器中，此处没有更新ui
		if (isSuccess) {
			adapter.addNewBlogs(listStatus);
		}

		lock.unlock();
		return isSuccess;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

        int cacheSize = adapter.getListNewBlogs().size();
		if (result || cacheSize > 0) {
			if (isAutoUpdate) {
				sendBroadcast();
			} else {
				addToAdapter();
			}
		} else {
			if (resultMsg != null && !isAutoUpdate) {
				Toast.makeText(adapter.getContext(), resultMsg, Toast.LENGTH_LONG).show();
			} else if (resultMsg == null && !isAutoUpdate) {
				Toast.makeText(adapter.getContext(), R.string.msg_latest_data, Toast.LENGTH_LONG).show();
			}

			if (isEmptyAdapter) {
				setEmptyView();
			}
		}

		if (!isAutoUpdate && listView != null) {
            listView.onRefreshComplete();
		}
	}

	private void sendBroadcast() {
		//发送更新广播;
		Intent intent = new Intent(Constants.ACTION_RECEIVER_AUTO_UPDATE_NOTIFY);
		NotificationEntity entity = adapter.getNotificationEntity();
		intent.putExtra("NOTIFICATION_ENTITY", entity);
		intent.putExtra("ACCOUNT", adapter.getAccount());
		context.sendBroadcast(intent);
	}

	private void addToAdapter() {
		List<net.dev123.mblog.entity.Status> listNewBlog = adapter.getListNewBlogs();
		int cacheSize = listNewBlog.size();
		//如果通知已经存在;
		if (cacheSize > 0) {
			NotificationManager notiManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notiManager.cancel(adapter.getAccount().getAccountId().intValue() * 100 + Skeleton.TYPE_MY_HOME);
		}

		if (cacheSize > 0
			&& listNewBlog.get(cacheSize - 1) instanceof LocalStatus
		) {
			cacheSize--;
		}

		adapter.refresh();
		if (!isAutoUpdate) {
			Toast.makeText(
				adapter.getContext(),
				adapter.getContext().getString(R.string.msg_refresh_frends_timeline, cacheSize),
				Toast.LENGTH_LONG
			).show();
		}
	}

	private void setEmptyView() {
		LocalAccount account = adapter.getAccount();
		if (account == null) {
			return;
		}
		LocalStatus divider = new LocalStatus();
		divider.setDivider(true);
		divider.setLocalDivider(true);
		listStatus.add(divider);
		adapter.addCacheToFirst(listStatus);
	}

	public boolean isAutoUpdate() {
		return isAutoUpdate;
	}

	public void setAutoUpdate(boolean isAutoUpdate) {
		this.isAutoUpdate = isAutoUpdate;
	}

	public PullToRefreshListView getListView() {
		return listView;
	}

	public void setListView(PullToRefreshListView listView) {
		this.listView = listView;
	}
}
