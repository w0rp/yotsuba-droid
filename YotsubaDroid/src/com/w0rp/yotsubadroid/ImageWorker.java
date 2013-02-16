package com.w0rp.yotsubadroid;

import java.net.URI;

import com.w0rp.androidutils.Net;

public class ImageWorker implements Runnable {
    public interface OnImageReceivedListener {
        void onImageReceived(long id, String filename);
    }

    private long id;
    private URI url;
    private String filename;
    private OnImageReceivedListener listener;

    public ImageWorker(long id, URI url, String filename) {
        this.id = id;
        this.url = url;
        this.filename = filename;
    }

    public void setOnImageReceivedListener(OnImageReceivedListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        Net.Request request = Net.openRequest(url);

        if (request.failure()) {
            // TODO: Deal with failure.
            return;
        }

        Yot.saveImage(filename, request.getStream());

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
