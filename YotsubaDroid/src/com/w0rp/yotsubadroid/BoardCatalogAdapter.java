package com.w0rp.yotsubadroid;

import com.w0rp.androidutils.Util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BoardCatalogAdapter extends PostListAdapter {
    public interface OnThreadSelectedListener {
        void onThreadSelected(long threadID);
    }

    private OnThreadSelectedListener listener;

    public void setOnThreadSelectedListener(OnThreadSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;

        if (item == null) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());

            item = inf.inflate(R.layout.catalog_item, parent, false);

            assert item instanceof RelativeLayout;
        }

        final Post post = (Post) getItem(position);

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onThreadSelected(post.getPostNumber());
                }
            }
        });

        ImageView imageView = (ImageView) item
            .findViewById(R.id.catalog_item_image);

        // null the image right away so we can hide it quickly.
        imageView.setImageDrawable(null);

        TextView txtSubject = (TextView) item
            .findViewById(R.id.catalog_item_subject);

        Util.textOrHide(txtSubject, ChanHTML.rawText(post.getSubject()).trim());

        TextView txtComment = (TextView) item
            .findViewById(R.id.catalog_item_comment);

        Util.textOrHide(txtComment, ChanHTML.rawText(post.getComment()).trim());

        loadImage(post, imageView);

        return item;
    }
}
