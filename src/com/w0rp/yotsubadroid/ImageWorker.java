package com.w0rp.yotsubadroid;

import java.net.URI;

import org.apache.http.client.methods.HttpGet;
import org.eclipse.jdt.annotation.Nullable;

import com.w0rp.androidutils.Net;

public class ImageWorker implements Runnable {
    public interface OnImageReceivedListener {
        void onImageReceived(long id, String filename);
    }

    private long id;
    private URI url;
    private String filename;
    private @Nullable OnImageReceivedListener listener;

    public ImageWorker(long id, URI url, String filename) {
        this.id = id;
        this.url = url;
        this.filename = filename;
    }

    public void setOnImageReceivedListener(
    @Nullable OnImageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        // Just load the image without If-Modified-Since headers.
        // We'll check that we have the file before we even start the request.
        Net.Response response = Net.openRequest(new HttpGet(url));

        if (response.failure()) {
            // TODO: Deal with failure.
            return;
        }

        Yot.saveImage(filename, response.getStream());

        if (listener != null) {
            Yot.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onImageReceived(id, filename);
                }
            });
        }
    }
}
