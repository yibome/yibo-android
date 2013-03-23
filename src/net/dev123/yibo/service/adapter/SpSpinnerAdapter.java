package net.dev123.yibo.service.adapter;

import net.dev123.commons.ServiceProvider;
import net.dev123.yibo.R;
import net.dev123.yibo.common.theme.Theme;
import net.dev123.yibo.common.theme.ThemeUtil;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SpSpinnerAdapter extends BaseAdapter {

	private ServiceProvider[] serviceProviders = {
		ServiceProvider.Sina,
		ServiceProvider.Tencent,
		ServiceProvider.Sohu,
		ServiceProvider.NetEase,
		ServiceProvider.Fanfou,
		ServiceProvider.Twitter,
		ServiceProvider.RenRen,
		ServiceProvider.KaiXin,
		ServiceProvider.QQZone
	};

	private String[] icons = {
		"icon_logo_sina_min",
		"icon_logo_tencent_min",
		"icon_logo_sohu_min",
		"icon_logo_netease_min",
		"icon_logo_fanfou_min",
		"icon_logo_twitter_min",
		"icon_logo_renren_min",
		"icon_logo_kaixin_min",
		"icon_logo_qqzone_min"
	};

	private String[] spNames;

	private LayoutInflater layoutInflater;
	public SpSpinnerAdapter(Context context) {
		this.spNames = context.getResources().getStringArray(R.array.service_provider);
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return serviceProviders.length;
	}

	@Override
	public ServiceProvider getItem(int position) {
		return serviceProviders[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ServiceProviderHolder holder = null;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_item_sp, null);
			holder = new ServiceProviderHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ServiceProviderHolder)convertView.getTag();
		}
		
		holder.reset();
		
		Theme theme = ThemeUtil.createTheme(convertView.getContext());
		holder.ivSpIcon.setImageDrawable(theme.getDrawable(icons[position]));		
		holder.tvSpName.setText(spNames[position]);
		
		return convertView;
	}

	@Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
