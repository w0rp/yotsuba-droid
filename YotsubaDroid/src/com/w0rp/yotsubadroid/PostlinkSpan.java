package com.w0rp.yotsubadroid;

import android.text.style.ClickableSpan;
import android.view.View;

public class PostlinkSpan extends ClickableSpan {
    public static interface OnPostLinkClickListener {
        public void onPostLinkClick(View view, String boardID, String postID);
    }

    private OnPostLinkClickListener listener;
    private String boardID;
    private String postID;

    public PostlinkSpan(String boardID, String postID) {
        this.boardID = boardID;
        this.postID = postID;
    }

    public void setOnPostLinkClickListener(OnPostLinkClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View view) {
        if (this.listener != null) {
            this.listener.onPostLinkClick(view, this.boardID, this.postID);
        }
    }

}
