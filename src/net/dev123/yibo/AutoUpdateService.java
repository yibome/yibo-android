package net.dev123.yibo;

import java.util.ArrayList;
import java.util.List;

import net.dev123.yibo.common.Constants;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.service.listener.ShakeUpdateListener;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

public class AutoUpdateService extends Service {
    private static final String TAG = AutoUpdateService.class.getSimpleName();
	private static List<LocalAccount> accountList = new ArrayList<LocalAccount>();

	private YiBoApplication yibo;
	private AutoUpdateNotifyReceiver updateNotifyReceiver;
	private AutoUpdateReceiver updateReceiver;
	private ConnectionChangeReceiver connChangeReceiver;
	private AlarmManager alarmManager;
	private ShakeUpdateListener shakeUpdateListener;

	private ScreenOffReceiver screenOffReceiver;
	private ScreenOnReceiver screenOnReceiver;
	@Override
	public void onCreate() {
		super.onCreate();

		//启动网络监听;
		connChangeReceiver = new ConnectionChangeReceiver();
		IntentFilter connFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		this.registerReceiver(connChangeReceiver, connFilter);

		updateNotifyReceiver = new AutoUpdateNotifyReceiver();
		IntentFilter updateNotifyFilter = new IntentFilter(Constants.ACTION_RECEIVER_AUTO_UPDATE_NOTIFY);
		this.registerReceiver(updateNotifyReceiver, updateNotifyFilter);

		updateReceiver = new AutoUpdateReceiver(accountList);
		IntentFilter updateFilter = new IntentFilter(Constants.ACTION_RECEIVER_AUTO_UPDATE);
		this.registerReceiver(updateReceiver, updateFilter);

		yibo = (YiBoApplication)this.getApplication();

		shakeUpdateListener = new ShakeUpdateListener(this);
		shakeUpdateListener.startMonitor();
		
		//锁屏和解屏的接收器
		screenOffReceiver = new ScreenOffReceiver();
		IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		this.registerReceiver(screenOffReceiver, screenOffFilter);
		
		screenOnReceiver = new ScreenOnReceiver();
		IntentFilter screenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		this.registerReceiver(screenOnReceiver, screenOnFilter);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		Intent updateIntent = new Intent(Constants.ACTION_RECEIVER_AUTO_UPDATE);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, updateIntent, 0);

		if (alarmManager != null) {
	        alarmManager.cancel(pi);
		}  else {
			alarmManager = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		}

		alarmManager.setRepeating(
			AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 120000,
			yibo.getUpdateInterval() * 1000, pi
		);//重复设置
		
		if(Constants.DEBUG) {
			Log.v(TAG, "start autoUpdateService, interval: " + yibo.getUpdateInterval() + "s!");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.unregisterReceiver(updateNotifyReceiver);
		this.unregisterReceiver(updateReceiver);
		this.unregisterReceiver(connChangeReceiver);
        this.unregisterReceiver(screenOffReceiver);
        this.unregisterReceiver(screenOnReceiver);
        
		if (alarmManager != null) {
			Intent updateIntent = new Intent(Constants.ACTION_RECEIVER_AUTO_UPDATE);
	        PendingIntent pi = PendingIntent.getBroadcast(this, 0, updateIntent, 0);
	        alarmManager.cancel(pi);
		}
		if (shakeUpdateListener != null) {
			shakeUpdateListener.stopMonitor();
		}
		if (Constants.DEBUG) {
			Log.v(TAG, "AutoUpdateService destory");
		}
	}

	public static void registerUpdateAccount(LocalAccount account) {
		if (account == null || accountList.contains(account)) {
			return;
		}
		accountList.add(account);
		if (Constants.DEBUG) {
			Log.v(TAG, "register update account, accountId:" + account.getAccountId());
		}
	}

	public static void removeUpdateAccount(LocalAccount account) {
		if (account == null || !accountList.contains(account)) {
			return;
		}
		accountList.remove(account);
	}
	
	public class ScreenOffReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.DEBUG) Log.d(TAG, "Screen Off");
			if (shakeUpdateListener != null) {
				shakeUpdateListener.stopMonitor();
			}			
		}
	}
	
	public class ScreenOnReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Constants.DEBUG) Log.d(TAG, "Screen On");
			if (shakeUpdateListener != null) {
				shakeUpdateListener.startMonitor();
			}			
		}
	}
}