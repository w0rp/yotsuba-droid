package com.w0rp.yotsubadroid;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.w0rp.androidutils.BasicReceiver;
import com.w0rp.androidutils.JSON;

import android.content.Context;
import android.content.Intent;

public abstract class PostListReceiver extends BasicReceiver {
    public PostListReceiver(Context context) {
        super(context);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        try {
            JSONArray jsonPostList = new JSONArray(
                intent.getStringExtra("jsonPostList"));

            List<Post> postList = new ArrayList<Post>();

            for (JSONObject obj : JSON.objIter(jsonPostList)) {
                postList.add(Post.fromJSON(obj));
            }

            onReceivePostList(postList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public abstract void onReceivePostList(List<Post> postList);

}
