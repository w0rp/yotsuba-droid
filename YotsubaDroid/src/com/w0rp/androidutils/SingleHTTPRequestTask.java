package com.w0rp.androidutils;

import java.io.IOException;

import org.apache.http.client.methods.HttpUriRequest;

import android.os.AsyncTask;

/**
 * This class provided a convenient way of dealing with a single HTTP request
 * with an AsyncTask. The response value for this task will be a string
 * containing the request data. When a request fails, this string will be null.
 */
public abstract class SingleHTTPRequestTask
extends AsyncTask<HttpUriRequest, Void, String> {
    @Override
    protected String doInBackground(HttpUriRequest... requestList) {
        assert(requestList.length == 1);

        Net.Request request = Net.openRequest(requestList[0]);

        if (!request.failure()) {
            try {
                return request.download();
            } catch (IOException e) { }
        }

        return null;
    }
}