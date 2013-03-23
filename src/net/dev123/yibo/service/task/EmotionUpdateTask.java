package net.dev123.yibo.service.task;

import java.io.File;

import net.dev123.exception.LibException;
import net.dev123.yibo.common.EmotionLoader;
import net.dev123.yibo.common.ImageUtil;
import net.dev123.yibo.common.YiBoMeUtil;
import net.dev123.yibo.common.ZipUtil;
import net.dev123.yibome.YiBoMe;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Config;
import android.util.Log;

/**
 * @author Weiping Ye
 * @version 创建时间：2011-9-16 下午1:28:49
 **/
public class EmotionUpdateTask extends AsyncTask<Void, Void, Void> {

	private static boolean isNeedDownload = true;
	private static final String TAG = EmotionUpdateTask.class.getSimpleName();
	@Override
	protected Void doInBackground(Void... params) {
		if (!isNeedDownload) {
			return null;
		}
		
		try {
			YiBoMe yiboMe = YiBoMeUtil.getYiBoMeNullAuth();
			String versionInfo = yiboMe.getEmotionVersionInfo();
			JSONObject jsonObj = new JSONObject(versionInfo);
			int version = jsonObj.getInt("version");
			String url = jsonObj.getString("url");
			if (url != null 
				&& version > EmotionLoader.getVersionImages()) {
				if (Config.DEBUG) Log.d(TAG, "downloading emotions.");
				String baseDir = EmotionLoader.getEmotionsBaseDir();
				File file = new File(baseDir + "emotions.zip");
				try {
					ImageUtil.getFileByUrl(url, file);
				} catch (LibException e) {
					e.printStackTrace();
				}
				ZipUtil zipUtil = new ZipUtil();
				zipUtil.unZip(baseDir + "emotions.zip", baseDir);
				if (file.exists()) {
					file.delete();
				}
			}
		} catch (Exception e) {
			if (Config.DEBUG) {
				Log.d(TAG, e.getMessage(), e);
			}
		} finally {
			isNeedDownload = false;
		}
		return null;
	}

}
