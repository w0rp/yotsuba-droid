package com.w0rp.yotsubadroid;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;

import com.w0rp.androidutils.IO;
import com.w0rp.androidutils.Net;
import android.os.AsyncTask;

public abstract class PostLoader {
    public static enum FailureType {
        LOCATION_MISSING,
        REQUEST_TIMEOUT,
        GENERIC_NETWORK_FAILURE,
        BAD_JSON
    }

    private final class Task extends AsyncTask<Void, Void, List<Post>> {
        private FailureType failure = null;

        private HttpGet prepareGet() {
            if (lastModifiedString == null) {
                // Don't set the If-Modified-Since header when we can't.
                return Net.prepareGet(getURI());
            }

            // The If-Modified-Since header is sent with the request.
            return Net.prepareGet(getURI(),
                new BasicHeader("If-Modified-Since", lastModifiedString));
        }

        @Override
        protected final List<Post> doInBackground(Void... params) {
            List<Post> postList = null;
            Header lastModified = null;

            try {
                HttpResponse response = new DefaultHttpClient().execute(
                    prepareGet());

                int responseCode = response.getStatusLine().getStatusCode();

                lastModified = response.getFirstHeader("Last-Modified");

                if (responseCode > 400) {
                    // Handle network failure.
                    switch (responseCode) {
                    case 404:
                        failure = FailureType.LOCATION_MISSING;
                    break;
                    case 408:
                        failure = FailureType.REQUEST_TIMEOUT;
                    break;
                    default:
                        failure = FailureType.GENERIC_NETWORK_FAILURE;
                    break;
                    }

                    return null;
                }

                if (responseCode == 304) {
                    // The post list hasn't been modified, so stop here.
                    return null;
                }

                postList = loadJson(IO.streamToString(
                    response.getEntity().getContent()));
            } catch (IOException e) {
                failure = FailureType.GENERIC_NETWORK_FAILURE;
                return null;
            } catch (JSONException e) {
                failure = FailureType.BAD_JSON;
                return null;
            }

            if (lastModified != null) {
                // Set the last modified string, as set by the server exactly.
                lastModifiedString = lastModified.getValue();
            }

            return postList;
        }

        @Override
        protected final void onPostExecute(List<Post> postList) {
            super.onPostExecute(postList);

            if (failure == null) {
                if (postList != null) {
                    onReceivePostList(postList);
                } else {
                    useLastPostList();
                }
            } else {
                onReceiveFailure(failure);
            }

            currentTask = null;
        }
    }

    private Task currentTask = null;
    private String lastModifiedString;

    /**
     * Execute an asynchronous task for loading the post list.
     *
     * If a task is in progress, it will be replaced with a new task.
     */
    public final void execute() {
        if (currentTask != null) {
            currentTask.cancel(true);
        }

        currentTask = new Task();
        currentTask.execute();
    }

    /**
     * @return The URI to request the post list with.
     */
    protected abstract URI getURI();

    /**
     * Given some JSON, create the post list.
     *
     * @param json The JSON to load the post list with.
     * @return The post list.
     * @throws JSONException If the JSON is invalid.
     */
    protected abstract List<Post> loadJson(String json) throws JSONException;

    /**
     * This method is called when a post list is received.
     *
     * @param postList The post list.
     */
    public abstract void onReceivePostList(List<Post> postList);

    /**
     * This method is called when the request ends with an unchanged post list.
     */
    public abstract void useLastPostList();

    /**
     * This method is called when loading a post list fails.
     *
     * @param failureType The reason why loading failed.
     */
    public abstract void onReceiveFailure(FailureType failureType);
}
