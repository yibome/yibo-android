package net.dev123.yibo.service.listener;

import net.dev123.yibo.EditRetweetActivity;
import net.dev123.yibo.widget.EmotionViewController;
import android.view.View;

/**
 * @author Weiping Ye
 * @version 创建时间：2011-8-30 下午5:38:24
 **/
public class EditRetweetEmotionClickListener extends
		EditMicroBlogEmotionClickListener {
	private EditRetweetActivity editRetweetActivity;
	private EmotionViewController emotionViewController;

	public EditRetweetEmotionClickListener(EditRetweetActivity context) {
		super(context);
		this.editRetweetActivity = context;
		emotionViewController = new EmotionViewController(context);
	}
	
	@Override
	public void onClick(View v) {
		if (emotionViewController.getEmotionViewVisibility() == View.VISIBLE) {
			editRetweetActivity.displayOptions(true);
		} else {
			editRetweetActivity.displayOptions(false);
		}
		
		//基类表情控制
        super.onClick(v);
	}
}
