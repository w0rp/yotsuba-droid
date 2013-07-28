package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.w0rp.androidutils.JSON;
import android.app.Activity;
import android.net.Uri;

public class CatalogLoader extends PostLoader {
    public CatalogLoader(Activity act, String boardID) {
        super(act, boardID);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Class getReceiverClass() {
        return BoardCatalogFragment.CatalogReceiver.class;
    }

    @Override
    protected URI getURI() {
        return URI.create(Yot.API_URL + Uri.encode(getBoardID())
            + "/catalog.json");
    }

    protected List<Post> loadJson(String json) {
        List<Post> postList = new ArrayList<Post>();

        JSONArray arr = null;

        try {
            arr = new JSONArray(json);
        } catch (Exception e) {
            // TODO: handle exception
            return postList;
        }

        for (JSONObject pageObj : JSON.objIter(arr)) {
            for (JSONObject postObj : JSON.objIter(pageObj, "threads")) {
                Post post = Post.fromChanJSON(getBoardID(), postObj);
                postList.add(post);
            }
        }

        return postList;
    }
}
