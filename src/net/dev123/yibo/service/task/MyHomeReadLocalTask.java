package net.dev123.yibo.service.task;

import java.util.List;

import net.dev123.commons.Paging;
import net.dev123.commons.util.ListUtil;
import net.dev123.exception.LibException;
import net.dev123.mblog.MicroBlog;
import net.dev123.yibo.common.GlobalVars;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.db.LocalStatus;
import net.dev123.yibo.service.adapter.MyHomeListAdapter;
import net.dev123.yibo.service.adapter.StatusUtil;
import net.dev123.yibo.service.cache.MyHomeCache;
import net.dev123.yibo.service.cache.wrap.StatusWrap;
import android.os.AsyncTask;

public class MyHomeReadLocalTask extends AsyncTask<net.dev123.mblog.entity.Status, Void, Void> {
	private MyHomeListAdapter adapter;
	private MyHomeCache cache;
	
	private Paging<net.dev123.mblog.entity.Status> paging;
	private LocalStatus divider;
	List<StatusWrap> listWrap = null;
	List<net.dev123.mblog.entity.Status> listStatus = null;
	public MyHomeReadLocalTask(MyHomeListAdapter adapter, MyHomeCache cache, LocalStatus divider) {
	    this.cache = cache;
	    this.adapter = adapter;
	    this.divider = divider;
	    paging = adapter.getPaging();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		divider.setLoading(true);
	}

	@Override
	protected Void doInBackground(net.dev123.mblog.entity.Status... params) {
		if (params == null 
			|| params.length != 2 
			|| !paging.hasNext()) {
			return null;
		}
		
		net.dev123.mblog.entity.Status max = params[0];
		net.dev123.mblog.entity.Status since = params[1];
		paging.setGlobalMax(max);
		paging.setGlobalSince(since);
		
		if (paging.moveToNext()) {
		    listWrap = cache.read(paging);
		}
        
		if (ListUtil.isEmpty(listWrap)) {
			listStatus = getDataFromRemote(max, since);
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		divider.setLoading(false);
		if (ListUtil.isEmpty(listWrap) && ListUtil.isEmpty(listStatus)) {
			paging.setLastPage(true);
			adapter.notifyDataSetChanged();
			return;
		}
		
        if (ListUtil.isNotEmpty(listWrap)) {
        	cache.remove(cache.size() - 1);
        	cache.addAll(cache.size(), listWrap);
        	adapter.notifyDataSetChanged();
        } else if (ListUtil.isNotEmpty(listStatus)) {
        	adapter.addCacheToDivider(divider, listStatus);
        }
		
	}

	private List<net.dev123.mblog.entity.Status> getDataFromRemote(net.dev123.mblog.entity.Status max, net.dev123.mblog.entity.Status since) {
		LocalAccount account = adapter.getAccount();
		MicroBlog microBlog = GlobalVars.getMicroBlog(account);
		List<net.dev123.mblog.entity.Status> listStatus = null;
		if (microBlog == null) {
			return listStatus;
		}
		
		Paging<net.dev123.mblog.entity.Status> paging = new Paging<net.dev123.mblog.entity.Status>();
		paging.setGlobalMax(max);
		paging.setGlobalSince(since);
		
		if (paging.moveToNext()) {
		    try {
			    listStatus = microBlog.getHomeTimeline(paging);
		    } catch (LibException e) {
			    //resultMsg = e.getDescription();
			    paging.moveToPrevious();
		    }
		}
		Util.getResponseCounts(listStatus, microBlog);

		boolean isSuccess = ListUtil.isNotEmpty(listStatus);
		if (isSuccess && paging.hasNext()) {
			LocalStatus localStatus = StatusUtil.createDividerStatus(listStatus, account);
			listStatus.add(localStatus);
		}
		
		return listStatus;
	}
}
