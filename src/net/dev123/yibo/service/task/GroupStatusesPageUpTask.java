package net.dev123.yibo.service.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.dev123.commons.Paging;
import net.dev123.commons.util.ListUtil;
import net.dev123.exception.ExceptionCode;
import net.dev123.exception.LibException;
import net.dev123.mblog.MicroBlog;
import net.dev123.mblog.entity.Group;
import net.dev123.yibo.R;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.GlobalVars;
import net.dev123.yibo.common.NetType;
import net.dev123.yibo.common.ResourceBook;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.db.LocalStatus;
import net.dev123.yibo.service.adapter.GroupStatusesListAdapter;
import net.dev123.yibo.service.adapter.StatusUtil;
import net.dev123.yibo.widget.PullToRefreshListView;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GroupStatusesPageUpTask extends AsyncTask<Void, Void, Boolean> {
	private static final String TAG = "MyHomePageUpTask";
	private static ReentrantLock lock = new ReentrantLock();
    private Context context;
	private MicroBlog microBlog = null;
	
	private Group group;
	private GroupStatusesListAdapter adapter;
	private PullToRefreshListView listView;
	private List<net.dev123.mblog.entity.Status> statusList;
    private String resultMsg = null;
    private boolean isAutoUpdate = false;
    private boolean isEmptyAdapter = false;
    private boolean isUpdateConflict = false;
	public GroupStatusesPageUpTask(GroupStatusesListAdapter adapter) {
		this.adapter = adapter;
		this.context = adapter.getContext();
		this.group = adapter.getGroup();
		statusList = new ArrayList<net.dev123.mblog.entity.Status>();

		microBlog = GlobalVars.getMicroBlog(adapter.getAccount());
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		isEmptyAdapter = (adapter.getMax() == null);
        if (!isAutoUpdate) {

        }
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
				isUpdateConflict = true;
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
				List<net.dev123.mblog.entity.Status> statuses = 
					microBlog.getGroupStatuses(group.getId(), paging);
				if (ListUtil.isNotEmpty(statuses)) {
				    statusList.addAll(statuses);
				}
			} catch (LibException e) {
				if (Constants.DEBUG) Log.e(TAG, "Task", e);
				resultMsg = ResourceBook.getStatusCodeValue(e.getExceptionCode(), context);
			}
		}
		// Util.getResponseCounts(statusList, microBlog);

		isSuccess = ListUtil.isNotEmpty(statusList);
		if (isSuccess 
			&& (paging.hasNext()
				|| (paging.isLastPage() 
					&& since == null))) {
			LocalStatus localStatus = StatusUtil.createDividerStatus(statusList, adapter.getAccount());
			if (paging.isLastPage() && since == null) {
				localStatus.setLocalDivider(true);
				adapter.getPaging().setLastPage(true);
			}
			statusList.add(localStatus);
		}

		lock.unlock();
		return isSuccess;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		super.onPostExecute(result);

		if (result) {
			adapter.addCacheToFirst(statusList);
			String msg = context.getString(R.string.msg_refresh_frends_timeline, statusList.size());
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		} else {
			if (resultMsg != null && !isAutoUpdate) {
				Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show();
			} else if (resultMsg == null && !isAutoUpdate) {
				Toast.makeText(context, R.string.msg_latest_data, Toast.LENGTH_LONG).show();
			}

			if (isEmptyAdapter) {
				setEmptyView();
			}
		}

		if (!isAutoUpdate && listView != null) {
            listView.onRefreshComplete();
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
		statusList.add(divider);
		adapter.addCacheToFirst(statusList);
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
