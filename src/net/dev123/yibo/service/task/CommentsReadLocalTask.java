package net.dev123.yibo.service.task;

import java.util.List;

import net.dev123.commons.Paging;
import net.dev123.commons.util.ListUtil;
import net.dev123.exception.LibException;
import net.dev123.mblog.MicroBlog;
import net.dev123.mblog.entity.Comment;
import net.dev123.yibo.common.GlobalVars;
import net.dev123.yibo.db.LocalAccount;
import net.dev123.yibo.db.LocalComment;
import net.dev123.yibo.service.adapter.CommentUtil;
import net.dev123.yibo.service.adapter.CommentsListAdapter;
import net.dev123.yibo.service.cache.CommentCache;
import net.dev123.yibo.service.cache.wrap.CommentWrap;
import android.os.AsyncTask;

public class CommentsReadLocalTask extends AsyncTask<Comment, Void, Void> {
	private CommentsListAdapter adapter;
	private CommentCache cache;
	
	private Paging<Comment> paging;
	private LocalComment divider;
	List<CommentWrap> listWrap = null;
	List<Comment> listComment = null;
	public CommentsReadLocalTask(CommentsListAdapter adapter, CommentCache cache, LocalComment divider) {
	    this.cache = cache;
	    this.adapter = adapter;
	    this.divider = divider;
	    paging = adapter.getPaging();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		divider.setLoading(true);
	}

	@Override
	protected Void doInBackground(Comment... params) {
		if (params == null ||
			params.length != 2 ||
			!paging.hasNext()
		) {
			return null;
		}
		
		Comment max = params[0];
		Comment since = params[1];		

	    paging.setGlobalMax(max);
		paging.setGlobalSince(since);

		
		if (paging.moveToNext()) {
		    listWrap = cache.read(paging);
		}
        
		if (ListUtil.isEmpty(listWrap)) {
			listComment = getDataFromRemote(max, since);
		}
		
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		divider.setLoading(false);
		if (ListUtil.isEmpty(listWrap) && ListUtil.isEmpty(listComment)) {
			paging.setLastPage(true);
			adapter.notifyDataSetChanged();
			return;
		}
		
        if (ListUtil.isNotEmpty(listWrap)) {
        	cache.remove(cache.size() - 1);
        	cache.addAll(cache.size(), listWrap);
        	adapter.notifyDataSetChanged();
        } else if (ListUtil.isNotEmpty(listComment)) {
        	adapter.addCacheToDivider(divider, listComment);
        }
		
	}

	private List<Comment> getDataFromRemote(Comment max, Comment since) {
		LocalAccount account = adapter.getAccount();
		MicroBlog microBlog = GlobalVars.getMicroBlog(account);
		List<Comment> listComment = null;
		if (microBlog == null) {
			return listComment;
		}
		
		Paging<Comment> paging = new Paging<Comment>(since, max);
		
		if (paging.moveToNext()) {
		    try {
		    	listComment = microBlog.getCommentsToMe(paging);
		    } catch (LibException e) {
			    //resultMsg = e.getDescription();
			    paging.moveToPrevious();
		    } finally {
		    	ListUtil.truncate(listComment, max, since);
		    }
		}

		if (ListUtil.isNotEmpty(listComment) && paging.hasNext()) {
			LocalComment localComment = CommentUtil.createDividerComment(
				listComment, adapter.getAccount());
			listComment.add(localComment);
		}
		
		return listComment;
	}
}
