package com.w0rp.yotsubadroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

public final class BoardCatalogFragment extends Fragment
implements BoardCatalogAdapter.OnThreadSelectedListener {
    private final class CatalogListener implements
        Response.Listener<JSONArray>,
        Response.ErrorListener
    {
        @Override
        public void onResponse(JSONArray response) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            List<Post> postList = new ArrayList<Post>();

            try {
                for (JSONObject pageObj : Util.jsonObjects(response)) {
                    for (JSONObject postObj : Util.jsonObjects(pageObj, "threads")) {
                        Post post = Post.fromChanJSON(boardID, postObj);
                        postList.add(post);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            catalogAdapter.setPostList(postList);
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            String failureText;

            if (error instanceof ParseError) {
                failureText = "Error parsing board, it may not exist!";
            } else {
                switch (error.networkResponse.statusCode) {
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

            getActivity().finish();
        }
    }

    private @Nullable String boardID;
    private final BoardCatalogAdapter catalogAdapter;
    private final CatalogListener catalogListener = new CatalogListener();

    public BoardCatalogFragment() {
        catalogAdapter = new BoardCatalogAdapter();
        catalogAdapter.setOnThreadSelectedListener(this);
    }

    public void updateCatalog() {
        getActivity().setProgressBarIndeterminateVisibility(true);

        Yot.getRequestQueue().add(new JsonArrayRequest(
            Yot.API_URL + Uri.encode(boardID) + "/catalog.json",
            catalogListener,
            catalogListener
        ));
    }

    @Override
    public View onCreateView(
    @Nullable LayoutInflater inflater, @Nullable ViewGroup container,
    @Nullable Bundle savedInstanceState) {
        assert inflater != null;

        GridView grid = (GridView) inflater.inflate(
            R.layout.catalog_grid, container);

        grid.setAdapter(catalogAdapter);

        return grid;
    }

    public void setBoardID(@Nullable String boardID) {
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
