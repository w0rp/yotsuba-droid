package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.w0rp.androidutils.JSON;

import android.app.Activity;
import android.net.Uri;

public class ThreadLoader extends PostLoader {
    private long threadID;

    public ThreadLoader(Activity act, String boardID, long threadID) {
        super(act, boardID);
        
        this.threadID = threadID;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected Class getReceiverClass() {
        return ThreadViewFragment.ThreadReceiver.class;
    }

    @Override
    protected URI getURI() {
        return URI.create(Yot.API_URL + Uri.encode(getBoardID()) 
            + "/res/" + Long.toString(threadID) + ".json");
    }
    
    protected List<Post> loadJson(String json) {
        List<Post> postList = new ArrayList<Post>();
        
        JSONObject threadObj = null;
        
        try {
            threadObj = new JSONObject(json);
        } catch (Exception e) {
            // TODO: handle exception
            return postList;
        }
        
        for (JSONObject postObj : JSON.objList(threadObj, "posts")) {
            Post post = Post.fromChanJSON(getBoardID(), postObj);
            postList.add(post);
        }
        
        return postList;
    }
}
