package net.dev123.yibo.service.adapter;

import net.dev123.mblog.entity.Status;
import net.dev123.yibo.R;
import net.dev123.yibo.SearchActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class StatusSearchResultAdapter extends ArrayAdapter<Status> {
	private SearchActivity context;

	public StatusSearchResultAdapter(SearchActivity context) {
		super(context, R.layout.list_item_status);
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Status status = (Status)getItem(position);
		if (status == null) {
			return null;
		}

		convertView = StatusUtil.initConvertView(context, convertView, status.getServiceProvider());
	    StatusUtil.fillConvertView(convertView, status);

		return convertView;
	}
}
