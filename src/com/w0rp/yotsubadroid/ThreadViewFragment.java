package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONException;

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

import com.w0rp.androidutils.Coerce;
import com.w0rp.androidutils.NetworkFailure;
import com.w0rp.yotsubadroid.ThreadViewAdapter.ThreadInteractor;
import com.w0rp.yotsubadroid.Yot.TBACK;

public final class ThreadViewFragment extends Fragment
implements ThreadInteractor {
    public final class ThreadLoader extends AbstractThreadLoader {
        public ThreadLoader(String boardID, long threadID) {
            super(boardID, threadID);
        }

        @Override
        protected void onReceiveResult(@Nullable List<Post> postList) {
            if (postList != null) {
                if (postList.size() > 0) {
                    // Use the thread's subject for the action bar title.
                    String subject = ChanHTML.rawText(
                        postList.get(0).getSubject()
                    );

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
            }

            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        @Override
        protected void onReceiveFailure(NetworkFailure failure) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            String failureText = null;

            if (failure.getException() instanceof JSONException) {
                failureText = "Error parsing thread, it was probably deleted!";
            } else {
                switch (failure.getResponseCode()) {
                case 404:
                    failureText = "404: Thread missing!";
                break;
                case 408:
                    failureText = "Request timeout, check your connection.";
                break;
                default:
                    failureText = "A network error broke the thread!";
                break;
                }
            }

            Toast.makeText(getActivity(), failureText, Toast.LENGTH_LONG)
            .show();

            getActivity().finish();
        }

        @Override
        protected void useLastResult() {
            // Just stop, we don't need to re-render.
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }

    private final ThreadViewAdapter threadAdapter;
    private @Nullable String currentBoardID;
    private long currentThreadID = 0;
    private long currentPostID = 0;
    private boolean initiallySkipped = false;
    private Map<Long, Integer> postPosMap = Coerce.emptyMap();
    private Stack<Long> postHistory = new Stack<Long>();
    private @Nullable ThreadLoader threadLoader;
    private @Nullable ListView postListView;

    public ThreadViewFragment() {
        threadAdapter = new ThreadViewAdapter(this);
    }

    public void updateThread() {
        if (threadLoader == null) {
            return;
        }

        getActivity().setProgressBarIndeterminateVisibility(true);

        if (threadLoader != null) {
            threadLoader.execute();
        }
    }

    private void openThread(String otherBoardID, long threadID, long postID) {
        Intent intent = new Intent(getActivity(), ThreadViewActivity.class);

        intent.putExtra("boardID", otherBoardID);
        intent.putExtra("threadID", threadID);
        intent.putExtra("postID", postID != 0 ? postID : threadID);

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
        final ListView checkedView = postListView;
        assert checkedView != null;

        TBACK hist = Yot.backHistorySetting();

        if (hist == TBACK.NEVER) {
            return;
        }

        Integer pos = postPosMap.get(newPostID);

        if (pos == null) {
            return;
        }

        if (hist == TBACK.SOMETIMES
        && pos >= checkedView.getFirstVisiblePosition()
        && pos <= checkedView.getLastVisiblePosition()) {
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

        if (postListView != null) {
            postListView.setSelection(pos);
        }
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

        threadLoader = new ThreadLoader(boardID, threadID);

        updateThread();
    }

    @Override
    public View onCreateView(@Nullable LayoutInflater inflater,
    @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        assert inflater != null;

        postListView = (ListView) inflater.inflate(
            R.layout.thread_post_list, container);

        if (postListView != null) {
            postListView.setAdapter(threadAdapter);
        }

        assert postListView != null : "thread_post_list dosen't exist!";

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
    public void onQuotelinkClick(Post originatingPost,
    final @Nullable String boardID, long threadID, long postID) {
        if (postID == 0) {
            return;
        }

        final boolean sameBoard = boardID == null
            || (currentBoardID != null && currentBoardID.equals(boardID));

        if (sameBoard && (threadID == 0 || threadID == currentThreadID)) {
            // The post is in this thread.
            savePosition(originatingPost.getPostNumber(), postID);
            skipToPost(postID);
            return;
        }

        if (threadID != 0) {
            // The post is not in this thread, so open the thread.
            if (boardID == null) {
                assert currentBoardID != null;
                openThread(currentBoardID, threadID, postID);
            } else {
                openThread(boardID, threadID, postID);
            }
        }
    }
}
