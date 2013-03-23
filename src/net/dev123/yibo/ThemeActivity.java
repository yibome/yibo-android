package net.dev123.yibo;

import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.service.adapter.ThemeListAdapter;
import net.dev123.yibo.service.listener.GoBackClickListener;
import net.dev123.yibo.service.listener.GoHomeClickListener;
import net.dev123.yibo.service.listener.ThemeRecyclerListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ThemeActivity extends BaseActivity {
	private YiBoApplication yibo;
    private ThemeListAdapter adapter;

	private LocalAccount account;

	private ListView lvTheme;
	private View listFooter;

	private ThemeRecyclerListener themeRecyclerListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.theme);

		yibo = (YiBoApplication) getApplication();
		initComponents();
		bindEvent();
	}

	private void initComponents() {
		LinearLayout llHeaderBase = (LinearLayout)findViewById(R.id.llHeaderBase);
		lvTheme = (ListView) findViewById(R.id.lvTheme);
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		
		ThemeUtil.setSecondaryHeader(llHeaderBase);
		ThemeUtil.setContentBackground(lvTheme);
		ThemeUtil.setListViewStyle(lvTheme);
		tvTitle.setText(R.string.title_theme);
		
		Intent intent = this.getIntent();
		account = (LocalAccount)intent.getSerializableExtra("ACCOUNT");
        if (account == null) {
        	account = yibo.getCurrentAccount();
        }
        
		adapter = new ThemeListAdapter(this, account);
		
		lvTheme.setFastScrollEnabled(yibo.isSliderEnabled());
		//showLoadingFooter();
		lvTheme.setAdapter(adapter);

		themeRecyclerListener = new ThemeRecyclerListener();
		lvTheme.setRecyclerListener(themeRecyclerListener);
	}

	private void bindEvent() {
		Button btnBack = (Button)this.findViewById(R.id.btnBack);
		btnBack.setOnClickListener(new GoBackClickListener());

		Button btnOperate = (Button) this.findViewById(R.id.btnOperate);
		btnOperate.setVisibility(View.VISIBLE);
		btnOperate.setText(R.string.btn_home);
		btnOperate.setOnClickListener(new GoHomeClickListener());

		//ListView lvTheme = (ListView)this.findViewById(R.id.lvTheme);
		//lvTheme.setOnItemClickListener(new SocialGraphItemClickListener(this));
	}

	public void showLoadingFooter() {
		if (listFooter != null) {
			lvTheme.removeFooterView(listFooter);
		}
		listFooter = getLayoutInflater().inflate(R.layout.list_item_loading, null);
		ThemeUtil.setListViewLoading(listFooter);
		
		lvTheme.addFooterView(listFooter);
	}

	public void showMoreFooter() {
		if (listFooter != null) {
			lvTheme.removeFooterView(listFooter);
		}

		listFooter = getLayoutInflater().inflate(R.layout.list_item_more, null);
        ThemeUtil.setListViewMore(listFooter);		
		listFooter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//executeTask();
			}
		});
		lvTheme.addFooterView(listFooter);
	}

	public void showNoMoreFooter() {
		if (listFooter != null) {
			lvTheme.removeFooterView(listFooter);
		}
		listFooter = getLayoutInflater().inflate(R.layout.list_item_more, null);
		ThemeUtil.setListViewMore(listFooter);
		
		TextView tvFooter = (TextView)listFooter.findViewById(R.id.tvFooter);		
		tvFooter.setText(R.string.label_no_more);
		lvTheme.addFooterView(listFooter);
	}
}
