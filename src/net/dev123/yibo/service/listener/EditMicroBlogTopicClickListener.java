package net.dev123.yibo.service.listener;

import net.dev123.yibo.R;
import net.dev123.yibo.widget.EmotionViewController;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class EditMicroBlogTopicClickListener implements OnClickListener {
	private static final String TOPIC_STRING = "##";
	private Activity context = null;
	private EmotionViewController emotionViewController;

	public EditMicroBlogTopicClickListener(Activity context) {
		this.context = context;
		emotionViewController = new EmotionViewController(context);
	}

	@Override
	public void onClick(View v) {
		EditText etText = (EditText)context.findViewById(R.id.etText);
		int currentPos  = etText.getSelectionStart();
		etText.getText().insert(currentPos, TOPIC_STRING);
		try {
		    etText.setSelection(currentPos + 1);
		} catch(Exception e) {}
		if (emotionViewController.getEmotionViewVisibility() == View.VISIBLE) { 
			InputMethodManager imm = (InputMethodManager) 
        		context.getSystemService(context.INPUT_METHOD_SERVICE);
			emotionViewController.hideEmotionView();
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

}
