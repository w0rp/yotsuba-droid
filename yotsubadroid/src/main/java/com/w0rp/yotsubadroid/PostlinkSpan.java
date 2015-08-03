package com.w0rp.yotsubadroid;

import android.support.annotation.Nullable;
import android.text.style.ClickableSpan;
import android.view.View;

public class PostlinkSpan extends ClickableSpan {
    public static interface OnPostLinkClickListener {
        public void onPostLinkClick(@Nullable View view, String boardID, String postID);
    }

    private @Nullable OnPostLinkClickListener listener;
    private String boardID;
    private String postID;

    public PostlinkSpan(String boardID, String postID) {
        this.boardID = boardID;
        this.postID = postID;
    }

    public void setOnPostLinkClickListener(
    @Nullable OnPostLinkClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(@Nullable View view) {
        if (this.listener != null) {
            this.listener.onPostLinkClick(view, this.boardID, this.postID);
        }
    }

}
