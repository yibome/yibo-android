package net.dev123.yibo.widget;

import java.util.regex.Pattern;

import net.dev123.commons.ServiceProvider;
import net.dev123.mblog.FeaturePatternUtils;
import net.dev123.yibo.common.Constants;
import android.content.Context;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;

public class RichTextView extends TextView {

	//平台
	private ServiceProvider provider = ServiceProvider.Sina;

	public RichTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RichTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean getDefaultEditable() {
	    return false;
	}

	@Override
	public void setText(CharSequence text, BufferType type) {

	    Spannable s;
        if (text instanceof Spannable) {
            s = (Spannable) text;
        } else {
        	if (text == null) {
        		text = "";
        	}
            s = Spannable.Factory.getInstance().newSpannable(text);
        }

        //email
        Linkify.addLinks(s, Linkify.EMAIL_ADDRESSES);

        //metion
        Pattern mentionPattern = FeaturePatternUtils.getMentionPattern(provider);
        if (mentionPattern != null) {
        	Linkify.addLinks(s, mentionPattern, Constants.URI_PERSONAL_INFO.toString());
        }

        //topic
        Pattern topicPattern = FeaturePatternUtils.getTopicPattern(provider);
        if (topicPattern != null) {
            Linkify.addLinks(s, topicPattern, Constants.URI_TOPIC.toString());
        }

        //url
        Pattern urlPattern = FeaturePatternUtils.getUrlPattern(provider);
        if (urlPattern != null) {
        	Linkify.addLinks(s, urlPattern, "http://");
        }

        text = s;

        super.setText(text, type);

        if (this.getLinksClickable()) {
        	setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

	public ServiceProvider getProvider() {
		return provider;
	}

	public void setProvider(ServiceProvider provider) {
		this.provider = provider;
	}

}
