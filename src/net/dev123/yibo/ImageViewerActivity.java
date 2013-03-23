package net.dev123.yibo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.dev123.commons.util.FileUtil;
import net.dev123.commons.util.StringUtil;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.common.ImageUtil;
import net.dev123.yibo.common.theme.ThemeUtil;
import net.dev123.yibo.service.listener.ImageViewerSaveClickListener;
import net.dev123.yibo.service.listener.SlideFinishOnGestureListener.SlideDirection;
import net.dev123.yibo.widget.GifView;
import net.dev123.yibo.widget.GifView.GifImageType;
import net.dev123.yibo.widget.ImageViewTouchBase;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageViewerActivity extends BaseActivity {
	private static final String TAG = ImageViewerActivity.class.getSimpleName();

	private static final int RETRY_COUNT = 3;

	public enum Mode{
		View, Edit
	}

    private boolean isGif;
    private boolean isFullScreen;
	private String imagePath;
	private GifView gifViewer;
	private boolean isInitialized;

	private ImageViewTouchBase ivImageViewer;
	private ImageView ivRotateLeft;
	private ImageView ivRotateRight;
	private ImageView ivZoomIn;
	private ImageView ivZoomOut;

	private Button btnOperate;

	private Mode mode = Mode.View;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.image_viewer);
		initComponent();
		bindEvent();
	}

	private void initComponent() {
		LinearLayout llHeaderBase = (LinearLayout)findViewById(R.id.llHeaderBase);
		LinearLayout llToolbar = (LinearLayout)findViewById(R.id.llToolbar);
		ivRotateLeft = (ImageView) findViewById(R.id.ivRotateLeft);
		ivRotateRight = (ImageView) findViewById(R.id.ivRotateRight);
		ivZoomIn = (ImageView) findViewById(R.id.ivZoomIn);
		ivZoomOut = (ImageView) findViewById(R.id.ivZoomOut);
		
		ThemeUtil.setSecondaryImageHeader(llHeaderBase);
		llToolbar.setBackgroundDrawable(theme.getDrawable("bg_toolbar"));
		int padding8 = theme.dip2px(8);
		llToolbar.setPadding(padding8, padding8, padding8, padding8);
		ivRotateLeft.setBackgroundDrawable(theme.getDrawable("selector_btn_image_rotate_left"));
		ivRotateRight.setBackgroundDrawable(theme.getDrawable("selector_btn_image_rotate_right"));
		ivZoomIn.setBackgroundDrawable(theme.getDrawable("selector_btn_image_zoom_in"));
		ivZoomOut.setBackgroundDrawable(theme.getDrawable("selector_btn_image_zoom_out"));
		
		TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
		tvTitle.setText(R.string.title_image_viewer);

		btnOperate = (Button) findViewById(R.id.btnOperate);

		ivImageViewer = (ImageViewTouchBase) findViewById(R.id.ivImageViewer);
		ivImageViewer.setRecycler(new ImageViewTouchBase.Recycler() {
			@Override
			public void recycle(Bitmap b) {
				if (!(b == null || b.isRecycled())) {
					if (Constants.DEBUG) {
						Log.d(TAG, "Recycle Bitmap : " + b);
					}
					b.recycle();
				}
			}
		});

		ivImageViewer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mode == Mode.View) {
					updateView();
				}
			}
		});
	}

	private void updateView() {
		if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.DONUT
			&& mode == Mode.View ) {
			// Android 2.0 开始支持多点触摸，所以，2.0以上开启全屏模式
			isFullScreen = !isFullScreen;
		} else {
			isFullScreen = false;
		}

		View llToolbar = findViewById(R.id.llToolbar);
		if (isFullScreen) {
			llToolbar.setVisibility(View.GONE);
		} else {
			llToolbar.setVisibility(View.VISIBLE);
		}
	}

	private void bindEvent() {
		Button back = (Button) this.findViewById(R.id.btnBack);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				goBack();
			}
		});

		ivRotateLeft.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ivImageViewer == null) {
					return;
				}
				ivImageViewer.rotate(-90);
			}
		});

		ivRotateRight.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ivImageViewer == null) {
					return;
				}
				ivImageViewer.rotate(+90);
			}
		});

		ivZoomIn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ivImageViewer == null) {
					return;
				}
				ivImageViewer.zoomIn();
			}
		});

		ivZoomOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (ivImageViewer == null) {
					return;
				}
				ivImageViewer.zoomOut();
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (isInitialized) {
			if (isGif) {
				gifViewer.showAnimation();
			}
		} else {
			initImageData();
		}
	}

	private void initImageData() {
		Uri uri = getIntent().getData();
		if (uri != null) {
			if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
				imagePath = uri.getPath();
			} else {
				imagePath = uri.toString();
			}

			mode = Mode.View;
		} else {
			imagePath = getIntent().getStringExtra("image-path");
			try {
				mode = Mode.valueOf(getIntent().getStringExtra("mode"));
			} catch (Exception e) {
				if (Constants.DEBUG) {
					Log.d(TAG, e.getMessage(), e);
				}
			}
		}

		if (Constants.DEBUG) {
			Log.d(TAG, "Image Path : " + imagePath);
		}

		if (StringUtil.isEmpty(imagePath)) {
			onBackPressed();
			return;
		}

		isGif = FileUtil.isGif(imagePath);
		if (isGif) {
			InputStream inputStream = getInputStreamFromFile(imagePath);
			if (inputStream == null) {
				onBackPressed();
				return;
			}

			gifViewer = new GifView(this, ivImageViewer);
			gifViewer.setGifImageType(GifImageType.SYNC_DECODER);
			gifViewer.setGifImage(inputStream);
			gifViewer.showAnimation();
		} else {
			uri = Uri.fromFile(new File(imagePath));
			Bitmap bitmap = getBitmapFromUri(uri);
			if (bitmap == null) {
				onBackPressed();
				return;
			}

			int rotation = getIntent().getIntExtra("rotation", 0);
			ivImageViewer.rotateTo(rotation);
			ivImageViewer.setImageBitmap(bitmap);
		}

		View.OnClickListener onClickListener = null;
		btnOperate.setVisibility(View.VISIBLE);
		if (mode == Mode.Edit) {
			btnOperate.setText(R.string.btn_delete);
			onClickListener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ImageViewerActivity.this.setResult(Constants.RESULT_CODE_IMAGE_DELETED);
					ImageViewerActivity.this.finish();
				}
			};
		} else {
			btnOperate.setText(R.string.btn_save);
			onClickListener = new ImageViewerSaveClickListener(imagePath);
		}
		btnOperate.setOnClickListener(onClickListener);
		updateView();

		isInitialized = true;
	}

	private InputStream getInputStreamFromFile(String path) {
		InputStream inputStream = null;
		try {
			inputStream = FileUtil.openInputStream(new File(imagePath));
			inputStream = new BufferedInputStream(inputStream, 24576); // 24 * 1024
		} catch (FileNotFoundException e) {
			try {
				int retryCount = 0;
				while (inputStream == null && retryCount < RETRY_COUNT) {
					if (Constants.DEBUG) {
						Log.d(TAG, "Reload Image: " + retryCount + " : " + path);
					}

					Thread.sleep(500);
					inputStream = FileUtil.openInputStream(new File(imagePath));
					inputStream = new BufferedInputStream(inputStream, 24576); // 24 * 1024
					retryCount ++;
				}
			} catch (Exception ee) {
				if (Constants.DEBUG) {
					Log.d(TAG, ee.getMessage(), ee);
				}
			}
		} catch (Exception e) {
			if (Constants.DEBUG) {
				Log.d(TAG, e.getMessage(), e);
			}
		}
		return inputStream;
	}

	private Bitmap getBitmapFromUri(Uri uri) {
		Bitmap bitmap = null;
		try {
			bitmap = ImageUtil.createBitmapFromUri(this, uri.toString(), 1600, 1600);
		} catch (FileNotFoundException e){
			try {
				int retryCount = 0;
				while (bitmap == null && retryCount < RETRY_COUNT) {
					if (Constants.DEBUG) {
						Log.d(TAG, "Reload Image: " + retryCount + " : " + uri.toString());
					}

					Thread.sleep(500);
					bitmap = ImageUtil.createBitmapFromUri(this, uri.toString(), 1600, 1600);
					retryCount ++;
				}
			} catch (Exception ee) {
				if (Constants.DEBUG) {
					Log.d(TAG, ee.getMessage(), ee);
				}
			}
		} catch (Exception e) {
			if (Constants.DEBUG) {
				Log.d(TAG, e.getMessage(), e);
			}
		}
		return bitmap;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (gifViewer != null) {
			gifViewer.showCover();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycle();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ivImageViewer != null
			&& Math.abs(1 - ivImageViewer.getScale()) < 0.01F) {
            setSlideDirection(SlideDirection.RIGHT);
		} else {
			setSlideDirection(SlideDirection.NONE);
		}

		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (ivImageViewer != null) {
			ivImageViewer.onTouchEvent(event);
		}

		return super.onTouchEvent(event);
	}

	private void recycle() {
		isFullScreen = false;
		isGif = false;
		mode = Mode.View;
		imagePath = null;
		if (ivImageViewer != null) {
			ivImageViewer.clear();
		}
		if (gifViewer != null) {
			gifViewer.destroy();
			gifViewer = null;
		}
	}

	@Override
	public void onBackPressed() {
		goBack();
	}

	private void goBack() {
		if (mode == Mode.Edit) {
			Intent intent = new Intent();
			intent.putExtra("rotation", ivImageViewer.getRotation());
			this.setResult(Constants.RESULT_CODE_IMAGE_ROTATED, intent);
		}
		this.finish();
	}

}
