package net.dev123.yibo.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import net.dev123.commons.ServiceProvider;
import net.dev123.commons.util.StringUtil;
import net.dev123.mblog.Emotions;
import net.dev123.yibo.R;
import net.dev123.yibo.YiBoApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.Config;
import android.util.Log;

/**
 * @author Weiping Ye
 * @version 创建时间：2011-9-8 下午1:03:28
 **/
public class EmotionLoader {
	public static final String TAG = EmotionLoader.class.getSimpleName();	
	public static final String BASE_TAB = "base";
	//private static Logger logger = LoggerFactory.getLogger(EmotionLoader.class.getSimpleName());
	private static android.graphics.BitmapFactory.Options opt = null;
	
	private static HashMap<String, Map<String, String>> emotionTabMap 
		= new HashMap<String, Map<String, String>>();
	private static Map<String, String[]> emotionsArrayMap = new HashMap<String, String[]>();
	private static Set<String> tabNameSet = new HashSet<String>();
	
	private static Application application = null;
	private static int versionImages = 0;
	private static boolean isInit = false;
	
	private EmotionLoader(Context context) {
	}
	
	public static synchronized void init(Application app) {
		if (isInit) {
			return;
		}
		if (Config.DEBUG) {
			Log.d(TAG, "init emotions...");
		}
		
		application = app;
		
		InputStream isEmotionsFan2Jian = null;	
		InputStream isEmotionsSpecialized = null;
		InputStream isEmotionsImages = null;
		if (isEmotionsFan2Jian == null) {
			isEmotionsFan2Jian = application.getResources().openRawResource(R.raw.emotions_fan2jian);
		}
		
		if (isEmotionsSpecialized == null) {
			isEmotionsSpecialized = application.getResources().openRawResource(R.raw.emotions_specialized);
		}
		
		Emotions.init(isEmotionsFan2Jian, isEmotionsSpecialized);
		try {
			if (isEmotionsFan2Jian != null) {
				isEmotionsFan2Jian.close();
			}
			if (isEmotionsSpecialized != null) {
				isEmotionsSpecialized.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		if (isEmotionsImages == null) {
			isEmotionsImages = application.getResources().openRawResource(R.raw.emotions_images);
		}
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(isEmotionsImages));
		
		String emotionName = null;
		String drawableName = null;
		int equalIndex = -1;
		int tabNameSeparate = -1;
		String tabName = null;
		String expression = null;
		Map<String, String> map = null;
		try {
			do {
				expression = bufferedReader.readLine();
				if (StringUtil.isEmpty(expression) || expression.startsWith("#")) {
					continue;
				}
				equalIndex = expression.indexOf('=');
				if (equalIndex == -1) {
					continue;
				}
				emotionName = expression.substring(0, equalIndex);
				drawableName = expression.substring(equalIndex + 1, expression.length());
				if (emotionName.equals("version")) {
					try {
						setVersionImages(Integer.valueOf(drawableName));
					} catch(NumberFormatException e) {
						Log.e(TAG, "Wrong versionImages: " + drawableName, e);
					}
					continue;
				}
				tabNameSeparate = drawableName.indexOf('|');
				if (tabNameSeparate > -1) {
					tabName = drawableName.substring(0, tabNameSeparate);
					drawableName = drawableName.substring(tabNameSeparate + 1);
				} else {
					tabName = BASE_TAB;
				}

				tabNameSet.add(tabName);
				map = emotionTabMap.get(tabName);
				if (map == null) {
					map = new LinkedHashMap<String, String>();
					emotionTabMap.put(tabName, map);
				}
				map.put(emotionName, drawableName);
			} while (expression != null);
		} catch (IOException e) {
			Log.e(TAG, "Failed to init emotions! " + e.getMessage(), e);
		} finally { 
			try {
				if (isEmotionsImages != null) {
					isEmotionsImages.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "Failed to close stream!", e);
			}
		}
		
		initEmotionArrayMap();
		
		opt = new android.graphics.BitmapFactory.Options();
		float standartDensity = 320f;
		opt.inDensity = Math.round((standartDensity / YiBoApplication.getDisplayWidth()) * 160f);

		isInit = true;
		
		if (Config.DEBUG) Log.d(TAG, "init emotions complete...");
	}
	
	private static void initEmotionArrayMap() {
		if (isInit) {
			return;
		}
		String[] emotionsArray = null;
		Map<String, String> emotionMap = null;
		for(String key : emotionTabMap.keySet()) {
			emotionsArray = new String[emotionTabMap.get(key).size()];
			emotionMap = emotionTabMap.get(key);
			int i = 0;
			for(String subKey : emotionMap.keySet()) {
				emotionsArray[i] = subKey;
				i++;
			}
			emotionsArrayMap.put(key, emotionsArray);
		}
	}
	
	public static boolean containsKey(String emotion) {
		if (!isInit) {
			return false;
		}
		String jian = Emotions.fan2Jian(emotion);
		for(String key : emotionTabMap.keySet()) {
			if (emotionTabMap.get(key).containsKey(jian)) {
				return true;
			}
		}
		return false;
	}

	public static String[] getEmotionsArray() {
		return getEmotionsArray(BASE_TAB);
	}
	
	public static String[] getEmotionsArray(String tabName) {
		if (!isInit) {
			return null;
		}
		return emotionsArrayMap.get(tabName);
	}
	
	public static Drawable getDrawableByEmontionName(String emotionFileName) {
		if (Constants.DEBUG) Log.d(TAG, emotionFileName);
		
		Drawable drawable = null;
		
		Bitmap bm = BitmapFactory.decodeFile(getEmotionsBaseDir() + emotionFileName, opt);
		if (bm != null) {
			drawable = new BitmapDrawable(bm);
		} else {
			String packageName = application.getPackageName();
			int resId = 0;
			if (emotionFileName.lastIndexOf('.') > -1) {
				emotionFileName = emotionFileName.substring(0, 
						emotionFileName.lastIndexOf('.'));
			}
			resId = application.getResources().getIdentifier(
					emotionFileName, "drawable", packageName);
			if (resId == 0) {
				return null;
			}
			drawable = application.getResources().getDrawable(resId);
		}

		return drawable;
	}
	
	public static Drawable getDrawableByEmontion(String emotion)	 {
		String jian = Emotions.fan2Jian(emotion);
		String emotionFileName = null;
		for(String key : emotionTabMap.keySet()) {
			if (emotionTabMap.get(key).containsKey(jian)) {
				emotionFileName = emotionTabMap.get(key).get(jian);
			}
		}
		if (emotionFileName == null) {
			return null;
		}
		return getDrawableByEmontionName(emotionFileName);
	}

	public static Spannable getEmotionSpannable(ServiceProvider serviceProvider, String text) {
		if (!isInit) {
			return null;
		}
		if (StringUtil.isEmpty(text)) {
			return null;
		}

		String content = Html.fromHtml(text).toString();
		Spannable span = Spannable.Factory.getInstance().newSpannable(content);		
		
		Matcher m = Emotions.NORMALIZED_PATTERN.matcher(content);		
		Drawable drawable = null;
		ImageSpan imageSpan = null;
		String emotionKey = null;
        while (m.find()) {
        	emotionKey = m.group(0);
            drawable = EmotionLoader.getDrawableByEmontion(emotionKey);
            if (drawable == null) {
            	continue;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
            span.setSpan(imageSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
		return span;
	}
	
	public static Set<String> getEmotionTabNameSet() {
		return new HashSet<String>(tabNameSet);
	}
	
	public static int getVersionImages() {
		return versionImages;
	}

	private static void setVersionImages(int versionImages) {
		EmotionLoader.versionImages = versionImages;
	}
	
	public static String getEmotionsBaseDir() {
		return YiBoApplication.getSdcardCachePath() + "/emotions/";
	}
	
	public static String getTraditionalToSimplifiedFilePath() {
		return getEmotionsBaseDir() + "emotions_fan2jian.properties";
	}
	
	public static String getSpecializedFilePath() {
		return getEmotionsBaseDir() + "emotions_specialized.properties";
	}
	
	public static String getImagesFilePath() {
		return getEmotionsBaseDir() + "emotions_images.properties";
	}
}
