package com.w0rp.androidutils;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.util.Base64;

public class Net {
    public static class Request {
        public static final int GENERIC_FAILURE = 600;

        private InputStream stream;
        private int responseCode = GENERIC_FAILURE;

        public Request(InputStream stream) {
            this(stream, GENERIC_FAILURE);
        }

        public Request(InputStream stream, int responseCode) {
            if (stream == null) {
                this.stream = IO.emptyInputStream();
            } else {
                this.stream = stream;
            }

            this.responseCode = responseCode;
        }

        /**
         * @return An input stream to download the request with.
         */
        public InputStream getStream() {
            return stream;
        }

        /**
         * @return An HTTP response code.
         */
        public int getResponseCode() {
            return responseCode;
        }

        /**
         * Download the entire request to a string.
         *
         * The InputStream will be automatically closed.
         *
         * @return All of the request data.
         */
        public String download() throws IOException {
            return IO.streamToString(stream);
        }

        /**
         * @return true if the response code is >= 400.
         */
        public boolean failure() {
            return responseCode >= 400;
        }
    }

    public static Request openRequest(HttpUriRequest request) {
        InputStream stream = null;
        int responseCode = Request.GENERIC_FAILURE;

        try {
            HttpResponse response = new DefaultHttpClient().execute(request);
            HttpEntity entity = response.getEntity();
            stream = entity.getContent();
            responseCode = response.getStatusLine().getStatusCode();
        } catch (IOException e) { }

        return new Request(stream, responseCode);
    }

    public static Request openRequest(URI uri) {
        return openRequest(new HttpGet(uri));
    }

    /*
     * @param
     *
     * @return A GET request object.
     */
    public static HttpGet prepareGet(URI uri, Header... headerList) {
        HttpGet request = new HttpGet(uri);

        for (Header header : headerList) {
            request.setHeader(header);
        }

        return request;
    }

    public static HttpGet prepareGet(URI uri) {
        return new HttpGet(uri);
    }

    public static Header authHeader(String username, String password) {
        return new BasicHeader("Authorization", "Basic "
            + base64Auth(username, password));
    }

    public static String base64Auth(String username, String password) {
        return Base64.encodeToString(
            (Coerce.def(username) + ":" + Coerce.def(password)).getBytes(),
            Base64.NO_WRAP);
    }
}
