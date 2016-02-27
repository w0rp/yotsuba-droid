package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.w0rp.yotsubadroid.ThreadViewAdapter.ThreadInteractor;
import com.w0rp.yotsubadroid.Yot.TBACK;

public final class ThreadViewFragment extends Fragment
implements ThreadInteractor {
    private final class ThreadListener implements
        Response.Listener<JSONObject>,
        Response.ErrorListener
    {
        @Override
        public void onResponse(JSONObject response) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            List<Post> postList = new ArrayList<Post>();

            try {
                for (JSONObject postObj : Util.jsonObjects(response, "posts")) {
                    Post post = Post.fromChanJSON(currentBoardID, postObj);
                    postList.add(post);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (postList.size() > 0) {
                // Use the thread's subject for the action bar title.
                String subject = ChanHTML.rawText(
                    postList.get(0).getSubject()
                );

                ActionBar actionBar = getActivity().getActionBar();

                if (actionBar != null) {
                    actionBar.setTitle(subject);
                }
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

        @Override
        public void onErrorResponse(VolleyError error) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            String failureText;

            if (error instanceof ParseError) {
                failureText = "Error parsing thread, it was probably deleted!";
            } else {
                switch (error.networkResponse.statusCode) {
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
    }

    private final ThreadViewAdapter threadAdapter;
    private final ThreadListener threadListener = new ThreadListener();
    private @Nullable String currentBoardID;
    private long currentThreadID = 0;
    private long currentPostID = 0;
    private boolean initiallySkipped = false;
    private Map<Long, Integer> postPosMap = Collections.emptyMap();
    private final Stack<Long> postHistory = new Stack<Long>();
    private @Nullable ListView postListView;

    public ThreadViewFragment() {
        threadAdapter = new ThreadViewAdapter(this);
    }

    public void updateThread() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        new JsonObjectRequest(
            Yot.API_URL + Uri.encode(currentBoardID) + "/res/"
                + Long.toString(currentThreadID) + ".json",
            null,
            threadListener,
            threadListener
        );
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
