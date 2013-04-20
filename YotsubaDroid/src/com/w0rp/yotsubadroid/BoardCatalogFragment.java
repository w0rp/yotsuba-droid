package com.w0rp.yotsubadroid;

import java.util.List;

import com.w0rp.androidutils.Async;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;

public class BoardCatalogFragment extends Fragment
implements BoardCatalogAdapter.OnThreadSelectedListener {
    public static final int CAT_ITEM_WIDTH = 200;
    public static final int CAT_ITEM_HEIGHT = 200;

    public class CatalogReceiver extends PostListReceiver {
        @Override
        public void onReceivePostList(List<Post> postList) {
            catalogAdapter.setPostList(postList);
        }
    }

    private String boardID;
    private BoardCatalogAdapter catalogAdapter;
    private BroadcastReceiver catalogReceiver;
    private CatalogLoader catalogLoader;

    public BoardCatalogFragment() {
        catalogAdapter = new BoardCatalogAdapter();
        catalogAdapter.setOnThreadSelectedListener(this);
    }

    private void updateCatalog() {
        if (catalogLoader != null) {
            catalogLoader.cancel(true);
        }

        catalogLoader = new CatalogLoader(getActivity(), boardID);
        catalogLoader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        catalogReceiver = new CatalogReceiver();
        Async.registerClass(getActivity(), catalogReceiver);

        GridView grid = new GridView(getActivity());

        grid.setNumColumns(-1);
        grid.setColumnWidth(Yot.CAT_ITEM_WIDTH);
        grid.setAdapter(catalogAdapter);

        grid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));

        return grid;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(catalogReceiver);
    }

    public void setBoardID(String boardID) {
        this.boardID = boardID;

        updateCatalog();
    }

    @Override
    public void onThreadSelected(long threadID) {
        Intent intent = new Intent(getActivity(), ThreadViewActivity.class);

        intent.putExtra("boardID", boardID);
        intent.putExtra("threadID", threadID);

        getActivity().startActivity(intent);
    }
}
