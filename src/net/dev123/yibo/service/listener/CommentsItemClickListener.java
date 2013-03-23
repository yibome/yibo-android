package net.dev123.yibo.service.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.dev123.mblog.entity.Comment;
import net.dev123.mblog.entity.Status;
import net.dev123.mblog.entity.User;
import net.dev123.yibo.EditCommentActivity;
import net.dev123.yibo.MicroBlogActivity;
import net.dev123.yibo.ProfileActivity;
import net.dev123.yibo.R;
import net.dev123.yibo.YiBoApplication;
import net.dev123.yibo.common.Constants;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.db.LocalComment;
import net.dev123.yibo.service.adapter.CacheAdapter;
import net.dev123.yibo.service.task.DestroyCommentTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;

public class CommentsItemClickListener implements OnItemClickListener {
	private Context context;
	private LocalAccount account;
    private Comment comment;
	private List<String> listItem;
	private boolean isDeleteable = false;
	public CommentsItemClickListener(Context context) {
		this.context = context;
		listItem = new ArrayList<String>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Adapter adapter = parent.getAdapter();
        comment = (Comment)adapter.getItem(position);
        if (comment == null
        	|| (comment instanceof LocalComment
        		&& ((LocalComment)comment).isDivider())) {
        	return;
        }

        if (adapter instanceof HeaderViewListAdapter) {
        	adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        }
        CacheAdapter<Comment> cacheAdapter = (CacheAdapter<Comment>)adapter;

        Dialog dialog = onCreateDialog(cacheAdapter, position);
        if (dialog != null) {
        	dialog.show();
        }
	}

	public Dialog onCreateDialog(final CacheAdapter<Comment> cacheAdapter, final int position) {
		if (comment == null) {
			return null;
		}
		YiBoApplication yibo = (YiBoApplication)context.getApplicationContext();
		account = yibo.getCurrentAccount();

		listItem.clear();
		listItem.add(context.getString(R.string.menu_comment_reply));
		listItem.add(context.getString(R.string.menu_comment_personal_info));
		listItem.add(context.getString(R.string.menu_comment_blog));

		User me = null;
		if (account != null) {
			me = (User) account.getUser();
		}
		if (comment.getUser().equals(me) ||
			(
			  comment.getInReplyToStatus() != null &&
			  comment.getInReplyToStatus().getUser() != null &&
			  comment.getInReplyToStatus().getUser().equals(me)
			)
		) {
			isDeleteable = true;
			listItem.add(context.getString(R.string.menu_comment_destroy));
		}

		Matcher m = Constants.URL_PATTERN.matcher(comment.getText());
		while (m.find()) {
			String url = m.group();
			listItem.add(url);
		}

		String[] selectItems = new String[listItem.size()];
        listItem.toArray(selectItems);

		return new AlertDialog.Builder(context)
            .setTitle(R.string.menu_title_comment)
            .setItems(selectItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
              		Intent intent = new Intent();
            		Bundle bundle = new Bundle();

                    if (which == 0) {
                		bundle.putInt("TYPE", Constants.EDIT_TYPE_RECOMMENT);
                        bundle.putSerializable("COMMENT", comment);
                		intent.putExtras(bundle);

                		intent.setClass(context, EditCommentActivity.class);
                		((Activity)context).startActivity(intent);
                    } else if (which == 1) {
                    	User user = comment.getUser();
                		bundle.putSerializable("USER", user);
                		intent.putExtras(bundle);

                		intent.setClass(context, ProfileActivity.class);
                		((Activity)context).startActivity(intent);
                    } else if (which == 2) {
                		Status status = comment.getInReplyToStatus();
                		bundle.putSerializable("STATUS", status);
                		intent.putExtras(bundle);

                		intent.setClass(context, MicroBlogActivity.class);
                		((Activity)context).startActivity(intent);
                    } else if (which == 3) {
                    	if (isDeleteable()) {
                    		DestroyCommentTask destroyTask = new DestroyCommentTask(cacheAdapter, comment);
                    		destroyTask.execute();
                    	} else {
                        	intent.setAction(Intent.ACTION_VIEW);
            				intent.setData(Uri.parse(getItem(which)));
            				context.startActivity(intent);
                    	}
                    } else {
                    	intent.setAction(Intent.ACTION_VIEW);
        				intent.setData(Uri.parse(getItem(which)));
        				context.startActivity(intent);
                    }
                }
            })
            .create();
	}

	private String getItem(int i) {
		return listItem.get(i);
	}

	private boolean isDeleteable() {
		return isDeleteable;
	}
}
