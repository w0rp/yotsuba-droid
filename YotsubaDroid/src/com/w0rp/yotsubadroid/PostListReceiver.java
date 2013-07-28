package com.w0rp.yotsubadroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.w0rp.androidutils.JSON;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class PostListReceiver extends BroadcastReceiver {
    public static enum FailureType {
        NETWORK_FAILURE,
        BAD_JSON
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        String jsonPostListString = intent.getStringExtra("jsonPostList");

        if (jsonPostListString == null) {
            onReceiveFailure(FailureType.NETWORK_FAILURE);
        }

        try {
            JSONArray jsonPostList = new JSONArray(jsonPostListString);

            List<Post> postList = new ArrayList<Post>();

            for (JSONObject obj : JSON.objIter(jsonPostList)) {
                postList.add(Post.fromJSON(obj));
            }

            onReceivePostList(postList);
        } catch (JSONException e) {
            e.printStackTrace();

            onReceiveFailure(FailureType.BAD_JSON);
        }
    }

    public abstract void onReceivePostList(List<Post> postList);
    public abstract void onReceiveFailure(FailureType failureType);
}
