package com.w0rp.yotsubadroid;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class BoardCatalogAdapter extends PostListAdapter {
    public interface OnThreadSelectedListener {
        void onThreadSelected(long threadID);
    }

    private @Nullable
    OnThreadSelectedListener listener;

    public void setOnThreadSelectedListener(
    OnThreadSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View getView(int position,
    @Nullable View convertView, @Nullable ViewGroup parent) {
        assert parent != null;

        View item;

        if (convertView != null) {
            item = convertView;
        } else {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());

            item = inf.inflate(R.layout.catalog_item, parent, false);

            if (BuildConfig.DEBUG && !(item instanceof RelativeLayout)) {
               throw new AssertionError();
            }
        }

        final Post post = getItem(position);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@Nullable View v) {
                if (listener != null) {
                    listener.onThreadSelected(post.getPostNumber());
                }
            }
        });

        final NetworkImageView imageView = (NetworkImageView) item
            .findViewById(R.id.catalog_item_image);

        if (imageView != null) {
            loadImage(post, imageView);
        }

        final TextView txtSubject = (TextView) item
            .findViewById(R.id.catalog_item_subject);

        if (txtSubject != null) {
            Util.textOrHide(
                txtSubject,
                ChanHTML.rawText(post.getSubject()).trim()
            );
        }

        TextView txtComment = (TextView) item
            .findViewById(R.id.catalog_item_comment);

        if (txtComment != null) {
            Util.textOrHide(
                txtComment,
                ChanHTML.rawText(post.getComment()).trim()
            );
        }

        return item;
    }
}
