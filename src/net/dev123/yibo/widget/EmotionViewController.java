package net.dev123.yibo.widget;

import net.dev123.yibo.R;
import net.dev123.yibo.service.adapter.EmotionsGridAdapter;
import net.dev123.yibo.service.listener.EditMicroBlogEmotionItemClickListener;
import android.app.Activity;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

/**
 * @author Weiping Ye
 * @version 创建时间：2011-10-10 上午12:54:05
 **/
public class EmotionViewController {
	private LinearLayout llEmotion;
	private GridView emotionGrid;
	public EmotionViewController(Activity activity) {
		this.emotionGrid = (GridView) activity.findViewById(R.id.emotionGrid);
		this.llEmotion = (LinearLayout) activity.findViewById(R.id.llEmotion);
	}

	public void showEmotionView() {
		emotionGrid.setVisibility(View.VISIBLE);
		llEmotion.setVisibility(View.VISIBLE);
	}
	
	public void hideEmotionView() {
		emotionGrid.setVisibility(View.GONE);
		llEmotion.setVisibility(View.GONE);
	}
	
	public int reverseEmotionView() {
		if (llEmotion.getVisibility() == View.GONE) {
			showEmotionView();
			return View.VISIBLE;
		} else {
			hideEmotionView();
			return View.GONE;
		}
	}
	
	public void setEmotionGridViewAdapter(EmotionsGridAdapter adapter) {
		emotionGrid.setAdapter(adapter);
	}
	
	public void setEmotionGridViewOnItemClickListener(EditMicroBlogEmotionItemClickListener listener) {
		emotionGrid.setOnItemClickListener(listener);
	}
	
	public int getEmotionViewVisibility() {
		return llEmotion.getVisibility();
	}
}
