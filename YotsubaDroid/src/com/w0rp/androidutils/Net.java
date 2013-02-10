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
    public static InputStream openRequest(HttpUriRequest request)
        throws IOException {
        HttpResponse response = null;

        response = new DefaultHttpClient().execute(request);

        HttpEntity entity = response.getEntity();
        InputStream stream = entity.getContent();

        return stream;
    }

    public static InputStream openRequest(URI uri) throws IOException {
        return openRequest(new HttpGet(uri));
    }

    public static String download(HttpUriRequest request) throws IOException {
        InputStream is = openRequest(request);

        try {
            return Util.streamToString(is);
        } finally {
            Util.close(is);
        }
    }

    public static String download(URI uri) throws IOException {
        return download(new HttpGet(uri));
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
