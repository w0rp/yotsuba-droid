package com.w0rp.yotsubadroid;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.json.JSONException;

import com.w0rp.androidutils.Net;
import android.os.AsyncTask;

public abstract class PostLoader extends AsyncTask<Void, Void, List<Post>> {
    public static enum FailureType {
        NETWORK_FAILURE,
        BAD_JSON
    }

    private FailureType failure = null;

    @Override
    protected List<Post> doInBackground(Void... params) {
        // TODO: Implement cancellation.

        try {
            String catalogJson = Net.openRequest(getURI()).download();

            return loadJson(catalogJson);
        } catch (IOException e) {
            failure = FailureType.NETWORK_FAILURE;
            return null;
        } catch (JSONException e) {
            failure = FailureType.BAD_JSON;
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Post> postList) {
        super.onPostExecute(postList);

        if (failure == null) {
            onReceivePostList(postList);
        } else {
            onReceiveFailure(FailureType.NETWORK_FAILURE);
        }
    }

    protected abstract URI getURI();
    protected abstract List<Post> loadJson(String json) throws JSONException;
    public abstract void onReceivePostList(List<Post> postList);
    public abstract void onReceiveFailure(FailureType failureType);
}
