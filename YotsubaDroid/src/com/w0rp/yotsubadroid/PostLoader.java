package com.w0rp.yotsubadroid;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.json.JSONArray;

import com.w0rp.androidutils.Net;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

public abstract class PostLoader extends AsyncTask<Void, Void, List<Post>> {
    private Activity act;
    private String boardID;

    public PostLoader(Activity act, String boardID) {
        this.act = act;
        this.boardID = boardID;
    }

    public String getBoardID() {
        return this.boardID;
    }

    @Override
    protected List<Post> doInBackground(Void... params) {
        if (boardID == null) {
            return null;
        }

        // TODO: Implement cancellation.

        String catalogJson = null;

        try {
            catalogJson = Net.openRequest(getURI()).download();
        } catch (IOException e) {
            return null;
        }

        return loadJson(catalogJson);
    }

    @Override
    protected void onPostExecute(List<Post> postList) {
        super.onPostExecute(postList);

        Intent intent = new Intent(getReceiverClass().getName());

        if (postList != null) {
            JSONArray jsonPostList = new JSONArray();

            for (Post post : postList) {
                jsonPostList.put(post.toJSON());
            }

            intent.putExtra("jsonPostList", jsonPostList.toString());
        }

        act.sendBroadcast(intent);
    }

    @SuppressWarnings("rawtypes")
    protected abstract Class getReceiverClass();

    protected abstract URI getURI();

    protected abstract List<Post> loadJson(String json);
}
