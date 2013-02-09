package com.w0rp.androidutils;

import android.content.*;

/*
 * This is a BroadcastReceiver which is context-aware, and
 * automatically registers itself.
 *
 * The receiver can be registered and unregistered with
 * the reg() and unreg() methods.
 */
public abstract class BasicReceiver extends BroadcastReceiver {
    private Context context;
    private boolean registered = false;

    public BasicReceiver(Context context) {
        this.context = context;

        reg();
    }

    public void reg() {
        if (registered || context == null) {
            return;
        }

        context.registerReceiver(this, new IntentFilter(this.getClass()
            .getName()));
        registered = true;
    }

    public void unreg() {
        if (!registered || context == null) {
            return;
        }

        context.unregisterReceiver(this);
        registered = false;
    }
}
