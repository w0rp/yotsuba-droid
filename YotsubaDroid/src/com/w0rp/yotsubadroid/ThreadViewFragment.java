package com.w0rp.yotsubadroid;

import java.util.List;

import com.w0rp.androidutils.Async;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ThreadViewFragment extends Fragment {
    public class ThreadReceiver extends PostListReceiver {
        @Override
        public void onReceivePostList(List<Post> postList) {
            if (postList.size() > 0) {
                // Use the thread's subject for the action bar title.
                String subject = postList.get(0).getSubject();
                getActivity().getActionBar().setTitle(subject);
            }

            threadAdapter.setPostList(postList);
        }
    }

    private String boardID;
    private long threadID = 0;
    private ThreadViewAdapter threadAdapter;
    private BroadcastReceiver threadReceiver;
    private ThreadLoader threadLoader;

    public ThreadViewFragment() {
        threadAdapter = new ThreadViewAdapter();
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
}
