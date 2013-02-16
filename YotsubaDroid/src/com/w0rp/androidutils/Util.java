package com.w0rp.androidutils;

import java.io.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.IntentFilter;

public abstract class Util {
    public static String traceString(Throwable tr) {
        StringWriter sw = new StringWriter();
        tr.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static IntentFilter filter(Object obj) {
        return new IntentFilter(obj.getClass().getName());
    }

    public static String pathClean(String path) {
        return path.replace('/', '_');
    }

    public static String join(String sep, String[] strList) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < strList.length; i++) {
            if (strList[i] != null) {
                sb.append(strList[i]);
            }

            if (sep != null && sep.length() > 0 && i < strList.length - 1) {
                sb.append(sep);
            }
        }

        return sb.toString();
    }

    public static String join(String sep, List<String> strList) {
        return join(sep, strList.toArray(new String[strList.size()]));
    }

    public static ThreadPoolExecutor pool(int maxSize, int wait,
        TimeUnit unit) {
        return new ThreadPoolExecutor(maxSize, maxSize, wait, unit,
            new LinkedBlockingDeque<Runnable>());
    }
}
