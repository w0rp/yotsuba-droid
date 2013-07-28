package com.w0rp.yotsubadroid;

import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

public class BoardCatalogFragment extends Fragment
implements BoardCatalogAdapter.OnThreadSelectedListener {
    public static final int CAT_ITEM_WIDTH = 200;
    public static final int CAT_ITEM_HEIGHT = 200;

    public class CatalogLoader extends AbstractCatalogLoader {
        public CatalogLoader(String boardID) {
            super(boardID);
        }

        @Override
        public void onReceivePostList(List<Post> postList) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            catalogAdapter.setPostList(postList);
        }

        @Override
        public void onReceiveFailure(FailureType failureType) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            String failureText = null;

            switch (failureType) {
            case BAD_JSON:
                failureText = "An error occured when parsing the board JSON!";
            break;
            case NETWORK_FAILURE:
                failureText = "A network error broke the catalog!";
            break;
            }

            Toast.makeText(getActivity(), failureText, Toast.LENGTH_LONG)
            .show();
        }
    }

    private String boardID;
    private BoardCatalogAdapter catalogAdapter;
    private CatalogLoader catalogLoader;

    public BoardCatalogFragment() {
        catalogAdapter = new BoardCatalogAdapter();
        catalogAdapter.setOnThreadSelectedListener(this);
    }

    public void updateCatalog() {
        if (catalogLoader != null) {
            catalogLoader.cancel(true);
        }

        getActivity().setProgressBarIndeterminateVisibility(true);

        catalogLoader = new CatalogLoader(boardID);
        catalogLoader.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        GridView grid = (GridView) inflater.inflate(
            R.layout.catalog_grid, container);

        grid.setAdapter(catalogAdapter);

        return grid;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
