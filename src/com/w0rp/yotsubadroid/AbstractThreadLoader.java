package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.w0rp.androidutils.JSON;

import android.net.Uri;

public abstract class AbstractThreadLoader extends PostLoader {
    private String boardID;
    private long threadID;

    public AbstractThreadLoader(String boardID, long threadID) {
        assert boardID != null;

        this.boardID = boardID;
        this.threadID = threadID;
    }

    @Override
    protected final URI getURI() {
        return URI.create(Yot.API_URL + Uri.encode(boardID) + "/res/"
            + Long.toString(threadID) + ".json");
    }

    @Override
    protected final List<Post> loadJson(String json) throws JSONException {
        JSONObject threadObj = new JSONObject(json);
        List<Post> postList = new ArrayList<Post>();

        for (JSONObject postObj : JSON.objIter(threadObj, "posts")) {
            Post post = Post.fromChanJSON(boardID, postObj);
            postList.add(post);
        }

        return postList;
    }
}
