package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.w0rp.androidutils.NetworkLoader;

public abstract class AbstractThreadLoader extends NetworkLoader<List<Post>> {
    private String boardID;
    private long threadID;

    public AbstractThreadLoader(String boardID, long threadID) {
        assert boardID != null;

        this.boardID = boardID;
        this.threadID = threadID;
    }

    @Override
    protected final @NonNull URI getURI() {
        return URI.create(Yot.API_URL + Uri.encode(boardID) + "/res/"
                + Long.toString(threadID) + ".json");
    }

    @Override
    protected List<Post> parseData(String data) throws JSONException {
        JSONObject threadObj = new JSONObject(data);
        List<Post> postList = new ArrayList<Post>();

        for (JSONObject postObj : JSON.objIter(threadObj, "posts")) {
            // We know for a fact that this is not null.
            JSONObject checkedPostObj = postObj;

            Post post = Post.fromChanJSON(boardID, checkedPostObj);
            postList.add(post);
        }

        return postList;
    }
}
