package net.dev123.yibo.service.cache;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.dev123.commons.util.StringUtil;
import net.dev123.yibo.YiBoApplication;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.ImageUtil;
import net.dev123.yibo.service.cache.wrap.CachedImage;
import net.dev123.yibo.service.cache.wrap.CachedImageKey;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ImageCache implements MapCache<CachedImageKey, CachedImage> {
    private static final String TAG             = "ImageCache";

	public static final String IMAGE_HEAD_MINI   = "head_mini";
	public static final String IMAGE_HEAD_NORMAL = "head_normal";
	public static final String IMAGE_THUMBNAIL   = "thumbnail";
	public static final String IMAGE_MIDDLE      = "middle";
	public static final String IMAGE_ORIGIN      = "origin";
	public static final String IMAGE_TEMP        = "temp";
	public static final String IMAGE_EMOTIONS    = "emotions";
	public static final String[] IMAGE_FOLDER = {
		IMAGE_HEAD_MINI, IMAGE_HEAD_NORMAL, IMAGE_THUMBNAIL,
		IMAGE_MIDDLE, IMAGE_ORIGIN, IMAGE_TEMP, IMAGE_EMOTIONS
	};
    private static final long BIG_IMG_SIZE_LEVEL = 120 * 120 * 4;

    private Map<CachedImageKey, CachedImage> memoryCache;
    //private Map<String, BitmapWrap> localCache;

    private static String filePath = "/sdcard/.yibo";  //赋值是为增加默认配置;
    private static String secondaryFilePath = "/data/data/net.dev123.yibo/cache";
    public ImageCache(String filePath, String secondFilePath) {
    	memoryCache = new HashMap<CachedImageKey, CachedImage>(60, 1.0f);
    	if (StringUtil.isNotBlank(filePath)) {
    	    ImageCache.filePath = filePath;
    	}
    	if (StringUtil.isNotBlank(secondFilePath)) {
            ImageCache.secondaryFilePath = secondFilePath;
    	}
    	File cacheFile = new File(ImageCache.filePath);
    	if (!cacheFile.exists()) {
    		cacheFile.mkdir();
    	}
        for (String folder : IMAGE_FOLDER) {
        	File file = new File(ImageCache.filePath + File.separator + folder);
        	if (!file.exists()) {
        		file.mkdirs();
        	}
        	file = new File(ImageCache.secondaryFilePath + File.separator + folder);
        	if (!file.exists()) {
        		file.mkdirs();
        	}
        }

        getCacheHeap();
    }

	private void getCacheHeap() {

	}

	@Override
	public void clear() {
		flush();
		memoryCache.clear();
	}

	@Override
	public boolean containsKey(CachedImageKey key) {
		boolean isContain = true;
		if (!memoryCache.containsKey(key)) {
			String filePath = getRealPath(key);
			if (filePath == null) {
				isContain = false;
			}
		}

		return isContain;
	}

	@Override
	public boolean reclaim(ReclaimLevel level) {
		switch(level) {
		case LIGHT:
	        Set<CachedImageKey> set = memoryCache.keySet();
	        Object[] infoArray = null;
	        try {
	        	infoArray = (Object[])set.toArray();
	        } catch(Exception e) {}

	        if (infoArray == null) {
	        	break;
	        }

	        int size = infoArray.length;
	        Object temp;
	        CachedImageKey key;
	        for (int i = 0; i < size; i++) {
	        	temp = infoArray[i];
	        	if (temp == null
	        		|| !(temp instanceof CachedImageKey)) {
	        		continue;
	        	}
	        	key = (CachedImageKey)temp;
	        	CachedImage wrap = memoryCache.get(key);
	        	if ( wrap == null
	        		|| wrap.getWrap() == null) {
	        		memoryCache.remove(key);
	        	}
	        }
		    break;
		case MODERATE:
		case WEIGHT:
			memoryCache.clear();
			break;
		}

        return true;
	}

	@Override
	public void flush() {
        return;
	}

	@Override
	public CachedImage get(CachedImageKey key) {
		boolean isContain = containsKey(key);
		if (!isContain) {
			return null;
		}

		CachedImage bWrap = null;
		if (memoryCache.containsKey(key)) {
			bWrap = memoryCache.get(key);
		}

		if (bWrap != null && bWrap.getWrap() != null) {
			if(Constants.DEBUG) Log.v("ImageCache", "hit memory cache");
			return bWrap;
		}

	    CachedImage temp = read(key);
	    Bitmap bitmap = null;
	    if (temp != null) {
	    	bitmap = temp.getWrap();
	    	if (bWrap != null) {
	            bWrap.setWrap(bitmap);
	    	} else {
	    		bWrap = temp;
	    		memoryCache.put(key, bWrap);
	    	}
	        if(Constants.DEBUG) Log.v("ImageCache", "hit local cache");
	    }

		return bWrap;
	}

	@Override
	public void put(CachedImageKey key, CachedImage value) {
		if (value == null
			|| key == null
		    || StringUtil.isEmpty(key.getImageUrl())) {
			return;
		}

        Bitmap bitmap = value.getWrap();
		if (bitmap != null
			&& !value.isLocalCached()) {
			write(key, value);
			value.setLocalCached(true);
		}

		memoryCache.put(key, value);
		if (memoryCache.size() > 50) {
			reclaim(ReclaimLevel.LIGHT);
		}
	}

	@Override
	public void remove(CachedImageKey key) {
		memoryCache.remove(key);
	}

	@Override
	public CachedImage read(CachedImageKey key) {
		long startRead = System.currentTimeMillis();
        String realPath = getRealPath(key);
		if (realPath == null) {
			return null;
		}

		File file = new File(realPath);
		byte[] fileBytes = null;
		InputStream fis = null;
		try {
			fis = new FileInputStream(file);
			fis = new BufferedInputStream(fis, 8192);
			int offset = 0;
			int byteCount = (int)file.length();
			int readCount = 0;

			fileBytes = new byte[byteCount];
			while((readCount = fis.read(fileBytes, offset, byteCount)) > 0) {
				offset += readCount;
				byteCount -= readCount;
			}
		} catch (FileNotFoundException e) {
			if(Constants.DEBUG) e.printStackTrace();
		} catch (IOException e) {
			if(Constants.DEBUG) e.printStackTrace();
		} catch (Exception e) {
			if(Constants.DEBUG) e.printStackTrace();
	    } finally {
	    	if (fis != null) {
			    try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		//先指定原始大小
		options.inSampleSize = 1;
		//只进行大小判断
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	    BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length, options);

	    long bitmapSize = ImageUtil.calculateBitmapSize(options);
	    if (bitmapSize >= BIG_IMG_SIZE_LEVEL) {
	    	options.inSampleSize = ImageUtil.getScaleSampleSize(options, 120);
	    	if(Constants.DEBUG) Log.d(TAG, "compress local big bitmap");
	    }
	    options.inJustDecodeBounds = false;
	    Bitmap bitmap = BitmapFactory.decodeByteArray(fileBytes, 0, fileBytes.length, options);

		CachedImage wrap = new CachedImage(bitmap);

		long endRead = System.currentTimeMillis();
		if (Constants.DEBUG) Log.d(TAG, "read local bitmap use time: " + (endRead - startRead) + "ms");
        return wrap;
	}

	@Override
	public void write(CachedImageKey key, CachedImage value) {
		if (value == null) {
			return;
		}

		Bitmap bitmap = value.getWrap();
		if (bitmap == null) {
			return;
		}

		String fileName = File.separator + IMAGE_FOLDER[key.getCacheType()] +
		    File.separator + key.getCachedName();
		File file = new File(filePath + fileName);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			file = new File(secondaryFilePath + fileName);
		}
		if (file.exists()) {
			return;
		}
		if (Constants.DEBUG) Log.v(TAG, file.getPath() + "|media state: " + Environment.getExternalStorageState());

		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);

			bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);

			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
			    try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//把数组写入文件
	public static void write(CachedImageKey key, byte[] imgBytes) {
		if (key == null ||
			StringUtil.isEmpty(key.getImageUrl()) ||
			imgBytes == null ||
			imgBytes.length == 0
		) {
			return;
		}

		String fileName = File.separator + IMAGE_FOLDER[key.getCacheType()] +
		    File.separator + key.getCachedName();
		File file = new File(filePath + fileName);
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			file = new File(secondaryFilePath + fileName);
		}
		if (file.exists()) {
			return;
		}

		FileOutputStream fos = null;
		try {
			file.createNewFile();
			fos = new FileOutputStream(file);

			fos.write(imgBytes);

			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
			    try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static String getRealPath(CachedImageKey key) {
		String filePath = null;
		if (key == null || StringUtil.isEmpty(key.getImageUrl())) {
			return filePath;
		}

		filePath = File.separator + IMAGE_FOLDER[key.getCacheType()] +
		    File.separator + key.getCachedName();
		File file = new File(ImageCache.filePath + filePath);
		if (file.exists() && file.isFile()) {
			return file.getPath();
		}

		file = new File(ImageCache.secondaryFilePath + filePath);
		if (file.exists() && file.isFile()) {
			return file.getPath();
		}

		return null;
	}

	public static String getImageSavePath(CachedImageKey key) {
		String filePath = null;
		if (key == null || StringUtil.isEmpty(key.getImageUrl())) {
			return null;
		}

		filePath = File.separator + IMAGE_FOLDER[key.getCacheType()] +
	        File.separator + key.getCachedName();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			filePath = ImageCache.filePath + filePath;
		} else {
			filePath = ImageCache.secondaryFilePath + filePath;
		}
		return filePath;
	}

	public static String getTempFolder() {
	    String tempFolder = YiBoApplication.getSdcardCachePath() +
            File.separator + IMAGE_TEMP;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        	tempFolder = YiBoApplication.getInnerCachePath() + File.separator + IMAGE_TEMP;
        }
        return tempFolder;
	}

	public static String getImageFolder(String imageType) {
	    String tempFolder = YiBoApplication.getSdcardCachePath() +
            File.separator + imageType;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        	tempFolder = YiBoApplication.getInnerCachePath() + File.separator + imageType;
        }
        return tempFolder;
	}
	
	public void stat() {
		int imageCount = 0;
		int headCount = 0;
		int thumbnailCount = 0;
		int bigCount = 0;
		int memorySize = 0;

		for (CachedImageKey info : memoryCache.keySet()) {
			CachedImage wrap = memoryCache.get(info);
			imageCount++;
			if (wrap == null) {
				continue;
			}
			Bitmap bitmap = wrap.getWrap();
			if (bitmap == null) {
				continue;
			}
			if (info.getCacheType() == CachedImageKey.IMAGE_HEAD_MINI
				|| info.getCacheType() == CachedImageKey.IMAGE_HEAD_NORMAL) {
				headCount ++;
			} else if (info.getCacheType() == CachedImageKey.IMAGE_THUMBNAIL) {
				thumbnailCount++;
			} else {
				bigCount++;
			}
			memorySize += bitmap.getWidth() * bitmap.getHeight() * 4;
		}

		System.out.println("ImageCache stat->"
			+ ", imageCount:" + imageCount
			+ ", headCount:" + headCount
			+ ", thumbnailCount:" + thumbnailCount
			+ ", bigCount:" + bigCount
			+ ", memorySize:" + memorySize);
	}
}
