package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.List;

import com.w0rp.androidutils.Async;
import com.w0rp.androidutils.SLog;
import com.w0rp.yotsubadroid.ThreadViewAdapter.ThreadInteractor;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class ThreadViewFragment extends Fragment implements ThreadInteractor {
    public class ThreadReceiver extends PostListReceiver {
        @Override
        public void onReceivePostList(List<Post> postList) {
            if (postList.size() > 0) {
                // Use the thread's subject for the action bar title.
                String subject = ChanHTML.rawText(postList.get(0).getSubject());
                getActivity().getActionBar().setTitle(subject);
            }

            // TODO: Save a mapping from post ID to index.

            threadAdapter.setPostList(postList);
        }
    }

    private final ThreadViewAdapter threadAdapter;
    private String boardID;
    private long threadID = 0;
    private BroadcastReceiver threadReceiver;
    private ThreadLoader threadLoader;

    public ThreadViewFragment() {
        threadAdapter = new ThreadViewAdapter(this);
    }

    private void updateThread() {
        if (threadLoader != null) {
            threadLoader.cancel(true);
        }

        // TODO: Reuse instance here to ease implementation of last modified?
        threadLoader = new ThreadLoader(getActivity(), boardID, threadID);
        threadLoader.execute();
    }

    public void setData(String boardID, long threadID) {
        this.boardID = boardID;
        this.threadID = threadID;

        // TODO: Use last modified logic in PostLoader.
        updateThread();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        threadReceiver = new ThreadReceiver();
        Async.registerClass(getActivity(), threadReceiver);

        // TODO: Save a reference to this ListView so we can skip through it.

        ListView listView = (ListView) inflater.inflate(
            R.layout.thread_post_list, container);
        listView.setAdapter(threadAdapter);

        return listView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(threadReceiver);
    }

    @Override
    public void onImageClick(Post post) {
        Context context = getActivity();
        URI fileURL = post.getFileURL();

        if (context == null || fileURL == null) {
            return;
        }

        // Open the image in some image URL handler.
        context.startActivity(new Intent(Intent.ACTION_VIEW,
            Uri.parse(fileURL.toString())));
    }

    @Override
    public void onTextCopy(Post post) {
        Context context = getActivity();
        String comment = post.getComment();

        if (context == null || comment.isEmpty()) {
            return;
        }

        ClipboardManager clip = (ClipboardManager)
            context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Copy the 4chan comment to the clipboard.
        clip.setPrimaryClip(ClipData.newPlainText("4chan comment",
            ChanHTML.rawText(post.getComment())));

        // Show a little toast telling the user what just happened.
        Toast toast = Toast.makeText(context.getApplicationContext(),
            "Comment copied to clipboard.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    @Override
    public void onQuotelinkClick(String board, long threadID, long postID) {
        // TODO: Skip to item in list by checking the saved map.
        SLog.i(board, threadID, postID);
    }
}
