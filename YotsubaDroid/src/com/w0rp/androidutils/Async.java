package com.w0rp.androidutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

public abstract class Async {
    /**
     * Register a BroadcastReceiver by its class name.
     *
     * @param context
     * @param receiver
     */
    public static void registerClass(Context context, BroadcastReceiver receiver) {
        context.registerReceiver(receiver,
            new IntentFilter(receiver.getClass().getName()));
    }
}
