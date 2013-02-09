package com.w0rp.androidutils;

import android.util.Log;

/*
 * This class makes Android logging easier.
 * 
 * * The log tag is automatically set to "SLog".
 * * Any object can be logged. Usually by calling toString().
 * * null references are logged as "null".
 */
public abstract class SLog {
    public static final String TAG = "SLog";
    
    private static String[] formatList(Object[] msgList) {
        String[] outList = new String[msgList.length];
        
        for (int i = 0; i < msgList.length; i++) {
            if (msgList[i] instanceof Throwable) {
                outList[i] = Log.getStackTraceString((Throwable) msgList[i]);
            } else {
                outList[i] = msgList[i] == null ? "null" 
                    : msgList[i].toString();
            }
        }
        
        return outList;
    }
    
    public static void d(Object... msgList) {
        for (String msg : formatList(msgList)) {
            Log.d(TAG, msg);
        }
    }
    
    public static void e(Object... msgList) {
        for (String msg : formatList(msgList)) {
            Log.e(TAG, msg);
        }
    }
    
    public static void i(Object... msgList) {
        for (String msg : formatList(msgList)) {
            Log.i(TAG, msg);
        }
    }
    
    public static void v(Object... msgList) {
        for (String msg : formatList(msgList)) {
            Log.v(TAG, msg);
        }
    }
    
    public static void w(Object... msgList) {
        for (String msg : formatList(msgList)) {
            Log.w(TAG, msg);
        }
    }
    
    public static void wtf(Object... msgList) {
        for (String msg : formatList(msgList)) {
            Log.wtf(TAG, msg);
        }
    }
}