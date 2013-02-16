package com.w0rp.androidutils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public abstract class IO {
    private static class NullInputStream extends InputStream {
        @Override public int read() { return -1; }
    }

    private static final InputStream nullInputStream = new NullInputStream();

    /**
     * @return An InputStream with no data.
     */
    public static InputStream emptyInputStream() {
        return nullInputStream;
    }

    /**
     * Transfer an InputStream to an OutputStream.
     *
     * Neither stream will be automatically closed.
     */
    public static void stream(InputStream in, OutputStream out)
        throws IOException {
        byte[] buffer = new byte[4096];

        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    /**
     * The InputStream will be automatically closed.
     *
     * @return A string containing all of the data from an InputStream.
     */
    public static String streamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            IO.close(is);
        }

        return sb.toString();
    }

    /**
     * Try to close an object, ignoring exceptions.
     *
     * This method will tolerate null references.
     */
    public static void close(Closeable obj) {
        if (obj == null) {
            return;
        }

        try {
            obj.close();
        } catch (Exception e) {
        }
    }
}
