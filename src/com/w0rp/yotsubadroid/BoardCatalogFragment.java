package com.w0rp.yotsubadroid;

import java.util.List;

import org.json.JSONException;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.w0rp.androidutils.NetworkFailure;

public final class BoardCatalogFragment extends Fragment
implements BoardCatalogAdapter.OnThreadSelectedListener {
    public final class CatalogLoader extends AbstractCatalogLoader {
        public CatalogLoader(String boardID) {
            super(boardID);
        }

        @Override
        protected void onReceiveResult(List<Post> postList) {
            getActivity().setProgressBarIndeterminateVisibility(false);
            catalogAdapter.setPostList(postList);
        }

        @Override
        public void onReceiveFailure(NetworkFailure failure) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            String failureText = null;

            if (failure.getException() instanceof JSONException) {
                failureText = "An error occured when parsing the board JSON!";
            } else {
                switch (failure.getResponseCode()) {
                case 404:
                    failureText = "404: Board missing!";
                break;
                case 408:
                    failureText = "Request timeout, check your connection.";
                break;
                default:
                    failureText = "A network error broke the catalog!";
                break;
                }
            }

            Toast.makeText(getActivity(), failureText, Toast.LENGTH_LONG)
            .show();
        }

        @Override
        protected void useLastResult() {
            // Just stop, we don't need to re-render.
            getActivity().setProgressBarIndeterminateVisibility(false);
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
        if (catalogLoader == null) {
            return;
        }

        getActivity().setProgressBarIndeterminateVisibility(true);
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
        catalogLoader = new CatalogLoader(boardID);

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
