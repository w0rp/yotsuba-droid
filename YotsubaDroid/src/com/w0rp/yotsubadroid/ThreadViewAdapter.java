package com.w0rp.yotsubadroid;

import com.w0rp.androidutils.Util;

import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThreadViewAdapter extends PostListAdapter {
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;

        if (item == null) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());

            item = inf.inflate(R.layout.thread_post, parent, false);
        }

        final Post post = (Post) getItem(position);

        // Load the subject.
        TextView txtSubject = (TextView) item.findViewById(R.id.post_subject);

        if (position == 0) {
            // Always hide the first subject.
            txtSubject.setVisibility(View.GONE);
        } else {
            Util.textOrHide(txtSubject, post.getSubject().trim());
        }

        // Load the poster name.
        TextView txtName = (TextView) item.findViewById(R.id.post_poster_name);
        // TODO: Replace with name span.
        txtName.setText(post.getPosterName());

        // Load the timestamp.
        TextView txtTimestamp = (TextView) item.findViewById(R.id.post_date);
        txtTimestamp.setText(post.getFormattedTime());

        // Load the post number.
        TextView txtNumber = (TextView) item.findViewById(R.id.post_number);
        txtNumber.setText("#" + Long.toString(post.getPostNumber()));

        RelativeLayout imageLayout =
            (RelativeLayout) item.findViewById(R.id.post_image_layout);

        ImageView imageView = (ImageView) item.findViewById(R.id.post_image);

        if (post.getFile() != null) {
            // Show layouts which have an image.
            imageLayout.setVisibility(View.VISIBLE);
            loadImage(post, imageView);
        } else {
            // Hide layouts which do not have an image.
            imageLayout.setVisibility(View.GONE);
        }

        // Load the comment.
        TextView txtComment = (TextView) item.findViewById(R.id.post_comment);

        if (post.getComment().isEmpty()) {
            // Hide text fields when there's no comment.
            txtComment.setVisibility(View.GONE);
        } else {
            // Fill the fields with formatted text otherwise.
            txtComment.setVisibility(View.VISIBLE);
            txtComment.setText(Comment.fullText(post.getComment()));
            Linkify.addLinks(txtComment, Linkify.WEB_URLS);
        }

        return item;
    }

}
