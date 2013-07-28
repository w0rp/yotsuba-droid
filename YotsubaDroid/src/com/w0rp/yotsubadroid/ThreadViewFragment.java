package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.w0rp.yotsubadroid.ThreadViewAdapter.ThreadInteractor;
import com.w0rp.yotsubadroid.Yot.TBACK;

import android.app.Fragment;
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
    public class ThreadLoader extends AbstractThreadLoader {
        public ThreadLoader(String boardID, long threadID) {
            super(boardID, threadID);
        }

        @Override
        public void onReceivePostList(List<Post> postList) {
            if (postList.size() > 0) {
                // Use the thread's subject for the action bar title.
                String subject = ChanHTML.rawText(postList.get(0).getSubject());
                getActivity().getActionBar().setTitle(subject);
            }

            postPosMap = new HashMap<Long, Integer>();

            // Save the posts positions for later.
            for (int i = 0; i < postList.size(); ++i) {
                postPosMap.put(postList.get(i).getPostNumber(), i);
            }

            threadAdapter.setPostList(postList);

            if (!initiallySkipped) {
                initiallySkipped = true;

                if (currentThreadID != currentPostID) {
                    skipToPost(currentPostID);
                }
            }

            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onReceiveFailure(FailureType failureType) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            String failureText = null;

            switch (failureType) {
            case BAD_JSON:
                failureText = "An error occured when parsing the thread JSON!";
            break;
            case NETWORK_FAILURE:
                failureText = "A network error broke the thread!";
            break;
            }

            Toast.makeText(getActivity(), failureText, Toast.LENGTH_LONG)
            .show();
        }
    }

    private final ThreadViewAdapter threadAdapter;
    private String currentBoardID;
    private long currentThreadID = 0;
    private long currentPostID = 0;
    private boolean initiallySkipped = false;
    private Map<Long, Integer> postPosMap = Collections.emptyMap();
    private Stack<Long> postHistory = new Stack<Long>();
    private ThreadLoader threadLoader;
    private ListView postListView;

    public ThreadViewFragment() {
        threadAdapter = new ThreadViewAdapter(this);
    }

    public void updateThread() {
        if (threadLoader != null) {
            threadLoader.cancel(true);
        }

        getActivity().setProgressBarIndeterminateVisibility(true);

        // TODO: Reuse instance here to ease implementation of last modified?
        threadLoader = new ThreadLoader(currentBoardID, currentThreadID);
        threadLoader.execute();
    }

    private void openThread(String otherBoardID, long threadID, long postID) {
        Intent intent = new Intent(getActivity(), ThreadViewActivity.class);

        intent.putExtra("boardID", otherBoardID);
        intent.putExtra("threadID", threadID);
        intent.putExtra("postID", postID);

        getActivity().startActivity(intent);
    }

    /**
     * Save the position for a post for the back button history.
     *
     * Nothing will be done if the new post is already visible.
     *
     * @param postID The post we are moving from.
     * @param newPostID The post we are moving to.
     */
    private void savePosition(long postID, long newPostID) {
        TBACK hist = Yot.backHistorySetting();

        if (hist == TBACK.NEVER) {
            return;
        }

        Integer pos = postPosMap.get(newPostID);

        if (pos == null) {
            return;
        }

        if (hist == TBACK.SOMETIMES
        && pos >= postListView.getFirstVisiblePosition()
        && pos <= postListView.getLastVisiblePosition()) {
            // The position we're moving to is already visible.
            // Let's not save the something for the back button history.
            return;
        }

        if (!postHistory.empty() && postHistory.lastElement() == postID) {
            // Don't save history for the same thing more than one time
            // in a row.
            return;
        }

        postHistory.add(postID);
    }

    private void skipToPost(long postID) {
        Integer pos = postPosMap.get(postID);

        if (pos == null) {
            return;
        }

        postListView.setSelection(pos);
        //postListView.smoothScrollToPosition(pos);
    }

    public boolean skipBack() {
        if (postHistory.empty()) {
            return false;
        }

        skipToPost(postHistory.pop());
        return true;
    }

    public void setData(String boardID, long threadID, long postID) {
        this.currentBoardID = boardID;
        this.currentThreadID = threadID;
        this.currentPostID = postID;

        // TODO: Use last modified logic in PostLoader.
        updateThread();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        postListView = (ListView) inflater.inflate(
            R.layout.thread_post_list, container);
        postListView.setAdapter(threadAdapter);

        return postListView;
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
    public void onQuotelinkClick(Post originatingPost, String boardID,
    long threadID, long postID) {
        if (threadID == 0 || postID == 0) {
            return;
        }

        if ((boardID == null || this.currentBoardID.equals(boardID))
        && threadID == this.currentThreadID) {
            // The post is in this thread.
            savePosition(originatingPost.getPostNumber(), postID);
            skipToPost(postID);
            return;
        }

        if (boardID == null) {
            boardID = this.currentBoardID;
        }

        // The post is not in this thread, so open the thread.
        openThread(boardID, threadID, postID);
    }
}
