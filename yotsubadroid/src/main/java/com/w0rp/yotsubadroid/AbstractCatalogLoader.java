package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.w0rp.androidutils.NetworkLoader;


public abstract class AbstractCatalogLoader extends NetworkLoader<List<Post>> {
    private final String boardID;

    public AbstractCatalogLoader(String boardID) {
        this.boardID = boardID;
    }

    @Override
    protected final @NonNull URI getURI() {
        return
            URI.create(Yot.API_URL + Uri.encode(boardID) + "/catalog.json");
    }

    @Override
    protected final List<Post> parseData(@NonNull String data)
    throws JSONException {
        JSONArray arr = new JSONArray(data);
        List<Post> postList = new ArrayList<Post>();

        for (JSONObject pageObj : JSON.objIter(arr)) {
            for (JSONObject postObj : JSON.objIter(pageObj, "threads")) {
                // We know for a fact that this is not null.
                JSONObject checkedPostObj = postObj;

                Post post = Post.fromChanJSON(boardID, checkedPostObj);
                postList.add(post);
            }
        }

        return postList;
    }
}
