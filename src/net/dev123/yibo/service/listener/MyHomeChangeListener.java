package net.dev123.yibo.service.listener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.dev123.commons.Constants;
import net.dev123.mblog.entity.Group;
import net.dev123.yibo.AutoUpdateService;
import net.dev123.yibo.R;
import net.dev123.yibo.YiBoApplication;
import net.dev123.yibo.common.CacheManager;
import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.service.adapter.AdapterUtil;
import net.dev123.yibo.service.adapter.GroupStatusesListAdapter;
import net.dev123.yibo.service.adapter.MyHomeListAdapter;
import net.dev123.yibo.service.cache.AdapterCollectionCache;
import net.dev123.yibo.service.cache.Cache;
import net.dev123.yibo.widget.PullToRefreshListView;
import net.dev123.yibo.widget.Skeleton;
import net.dev123.yibo.widget.ValueSetEvent;
import net.dev123.yibo.widget.ViewChangeEvent;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MyHomeChangeListener implements PropertyChangeListener {
	private Activity context;
	private YiBoApplication yibo;
	private Map<LocalAccount, View> viewMap;

	private HomePageEditStatusClickListener editStatusClickListner;
	private HomePageRefreshListener refreshListener;
	private MicroBlogItemClickListener itemClickListener;
	private StatusRecyclerListener recyclerListener;
	public MyHomeChangeListener(Context context) {
		this.context = (Activity)context;
		this.yibo = (YiBoApplication)context.getApplicationContext();
        viewMap = new HashMap<LocalAccount, View>();

        editStatusClickListner = new HomePageEditStatusClickListener(context);
        refreshListener = new HomePageRefreshListener();
        itemClickListener = new MicroBlogItemClickListener(context);
        recyclerListener = new StatusRecyclerListener();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
        if (event instanceof ViewChangeEvent) {
        	viewChange(event);
        } else if (event instanceof ValueSetEvent) {
        	valueSet(event);
        }
	}

	private void viewChange(PropertyChangeEvent event) {
		if (!((event instanceof ViewChangeEvent)
				&& event.getNewValue().equals(Skeleton.TYPE_MY_HOME))) {
			return;
		}

		ViewChangeEvent changeEvent = (ViewChangeEvent)event;
		ViewGroup viewGroup = (ViewGroup)changeEvent.getView();
		viewGroup.removeAllViews();

		View view = getContentView(changeEvent.getAccount());
		if (view != null) {
	        viewGroup.addView(view);
		}

	    updateHeader(changeEvent);
	}

	private void updateHeader(ViewChangeEvent changeEvent) {
		TextView tvTitle = (TextView)context.findViewById(R.id.tvTitle);
	    LocalAccount account = changeEvent.getAccount();
	    
	    ListView lvMicroBlog = (ListView)context.findViewById(R.id.lvMicroBlog);
	    if (lvMicroBlog == null) {
	    	return;
	    }
	    BaseAdapter adapter = AdapterUtil.getAdapter(lvMicroBlog.getAdapter());
	    String title = "";
	    if (adapter instanceof MyHomeListAdapter) {
		    if (account.getUser() != null) {
		    	title += account.getUser().getScreenName() + "@";
		    }
		    title += account.getServiceProvider().getServiceProviderName();
	    } else if (adapter instanceof GroupStatusesListAdapter) {
	    	GroupStatusesListAdapter statusesListAdapter = (GroupStatusesListAdapter)adapter;
	    	Group group = statusesListAdapter.getGroup();
	    	title = group.getName();
	    }

	    tvTitle.setText(title);

	    View llHeaderBase = ((Activity)context).findViewById(R.id.llHeaderBase);
	    llHeaderBase.setVisibility(View.VISIBLE);
	    View llHeaderMessage = ((Activity)context).findViewById(R.id.llHeaderMessage);
	    llHeaderMessage.setVisibility(View.GONE);

	    ImageButton ibProfileImage = (ImageButton) context.findViewById(R.id.ibProfileImage);
	    ibProfileImage.setVisibility(View.VISIBLE);
		ImageButton ibGroup = (ImageButton) context.findViewById(R.id.ibGroup);
		ibGroup.setVisibility(View.VISIBLE);
		ImageButton ibEdit = (ImageButton) context.findViewById(R.id.ibEdit);
		ibEdit.setVisibility(View.VISIBLE);
		ibEdit.setOnClickListener(editStatusClickListner);
	}

	private void valueSet(PropertyChangeEvent event) {
		ValueSetEvent setEvent = (ValueSetEvent)event;
		LocalAccount account = setEvent.getAccount();

		switch(setEvent.getAction()) {
		case ACTION_INIT_ADAPTER:
			initAdapter(account);
			break;
		case ACTION_RECLAIM_MEMORY:
			//reclaimView(yibo.getCurrentAccount());
			break;
		default:
			break;
		}
	}

	private View getContentView(LocalAccount account) {
		ListView lvMicroBlog = null;
		if (account == null) {
			return lvMicroBlog;
		}

		View contentView = viewMap.get(account);
		if (contentView != null) {
		    lvMicroBlog = (ListView)contentView.findViewById(R.id.lvMicroBlog);
		} else {
	        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        contentView = inflater.inflate(R.layout.home_page_content_list, null);
	        lvMicroBlog = (ListView)contentView.findViewById(R.id.lvMicroBlog);
	        if (lvMicroBlog instanceof PullToRefreshListView) {
	        	((PullToRefreshListView)lvMicroBlog).setOnRefreshListener(refreshListener);
	        }
	        View emptyView = contentView.findViewById(R.id.llLoadingView);
	        ThemeUtil.setContentBackground(contentView);
	        ThemeUtil.setListViewStyle(lvMicroBlog);
	        ThemeUtil.setListViewLoading(emptyView);
	        
	        lvMicroBlog.setOnItemClickListener(itemClickListener);

	        MyHomeListAdapter adapter = initAdapter(account);
			lvMicroBlog.setAdapter(adapter);
			lvMicroBlog.setEmptyView(emptyView);
			MicroBlogContextMenuListener contextMenuListener = 
				new MicroBlogContextMenuListener(lvMicroBlog);
			lvMicroBlog.setOnCreateContextMenuListener(contextMenuListener);
			lvMicroBlog.setRecyclerListener(recyclerListener);
            lvMicroBlog.setOnScrollListener(new AutoLoadMoreListener());
            
			viewMap.put(account, contentView);
			
			//首页进入更新
			if (yibo.isRefreshOnFirstEnter() && adapter.getCount() > 0) {
	            PullToRefreshListView prListView = (PullToRefreshListView)lvMicroBlog;
	            prListView.setSelection(0);
	            prListView.prepareForRefresh();
	            prListView.onRefresh();
	        }
		}

		lvMicroBlog.setFastScrollEnabled(yibo.isSliderEnabled());
		return contentView;
	}

	private MyHomeListAdapter initAdapter(LocalAccount account) {
		Cache cache = CacheManager.getInstance().getCache(account);
		AdapterCollectionCache adapterCache = (AdapterCollectionCache)cache;
		if (adapterCache == null) {
			adapterCache = new AdapterCollectionCache(account);
			CacheManager.getInstance().putCache(account, adapterCache);
		}
		MyHomeListAdapter adapter = adapterCache.getMyHomeListAdapter();
		if (adapter == null) {
			adapter = new MyHomeListAdapter(context, account);
			adapterCache.setMyHomeListAdapter(adapter);
			AutoUpdateService.registerUpdateAccount(account);
		}

		return adapter;
	}
	
	private void reclaimView(LocalAccount account) {
		if (account == null) {
			return;
		}
		
		List<View> viewList = new ArrayList<View>();
		for (LocalAccount temp : viewMap.keySet()) {
			if (account.getAccountId() == temp.getAccountId()) {
				continue;
			}
			View contentView = viewMap.get(account);
			if (contentView != null) {
			    ListView lvMicroBlog = (ListView)contentView.findViewById(R.id.lvMicroBlog);
			    if (lvMicroBlog != null) {
			    	lvMicroBlog.reclaimViews(viewList);
			    }
			}
			
			if (Constants.DEBUG) Log.v("MyHomeChangeListener", "reclaimViews: " + viewList.size());
		    viewList.clear();
		}
	}
}
