package com.w0rp.yotsubadroid;

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

            item = (RelativeLayout) inf.inflate(R.layout.catalog_item, parent,
                false);
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

        TextView txtSubject = (TextView) item
            .findViewById(R.id.catalog_item_subject);

        txtSubject.setText(post.getSubject());

        if (post.getSubject().trim().length() == 0) {
            txtSubject.setVisibility(View.GONE);
        } else {
            txtSubject.setVisibility(View.VISIBLE);
            txtSubject.setText(post.getSubject().trim());
        }

        TextView txtComment = (TextView) item
            .findViewById(R.id.catalog_item_comment);

        if (post.getComment().trim().length() == 0) {
            txtComment.setVisibility(View.GONE);
        } else {
            txtComment.setVisibility(View.VISIBLE);
            txtComment.setText(Comment.summaryText(post.getComment()));
        }

        ImageView imageView = (ImageView) item
            .findViewById(R.id.catalog_item_image);

        loadImage(post, imageView);

        return item;
    }
}
