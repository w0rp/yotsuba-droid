package com.w0rp.yotsubadroid;

import com.w0rp.androidutils.SpanBuilder;
import com.w0rp.androidutils.Util;
import com.w0rp.yotsubadroid.ChanHTML.TextGenerator;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThreadViewAdapter extends PostListAdapter {
    public static interface ThreadInteractor {
        public void onImageClick(Post post);
        public void onTextCopy(Post post);
    }

    private final ThreadInteractor interactor;

    public ThreadViewAdapter(ThreadInteractor interactor) {
        this.interactor = interactor;
    }

    private void renderSubject(int position, View item, Post post) {
        TextView txtSubject = (TextView) item.findViewById(R.id.post_subject);

        if (position == 0) {
            // Always hide the first subject.
            txtSubject.setVisibility(View.GONE);
        } else {
            Util.textOrHide(txtSubject,
                ChanHTML.rawText(post.getSubject()).trim());
        }
    }

    private void renderPosterName(View item, Post post) {
        TextView txtName = (TextView) item.findViewById(R.id.post_poster_name);
        String name = ChanHTML.rawText(post.getPosterName());

        if (post.isModPost()) {
            // Moderator poster names are in red.
            SpanBuilder sb = new SpanBuilder();
            sb.append(name, SpanBuilder.fg(255, 0, 0));
            txtName.setText(sb.span());
        } else {
            txtName.setText(name);
        }
    }

    private void renderImage(View item, final Post post) {
        RelativeLayout imageLayout =
            (RelativeLayout) item.findViewById(R.id.post_image_layout);

        ImageView imageView = (ImageView) item.findViewById(R.id.post_image);

        if (post.getFile() != null) {
            // Show layouts which have an image.
            imageLayout.setVisibility(View.VISIBLE);
            loadImage(post, imageView);

            imageView.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View v) {
                    if (interactor != null) {
                        interactor.onImageClick(post);
                    }
                }
            });
        } else {
            // Hide layouts which do not have an image.
            imageLayout.setVisibility(View.GONE);
        }
    }

    private void renderComment(View item, final Post post) {
        TextView txtComment = (TextView) item.findViewById(R.id.post_comment);

        if (post.getComment().isEmpty()) {
            // Hide text fields when there's no comment.
            txtComment.setVisibility(View.GONE);
        } else {
            // Fill the fields with formatted text otherwise.
            txtComment.setVisibility(View.VISIBLE);
            // We need to set this so ClickableSpans will work.
            txtComment.setMovementMethod(LinkMovementMethod.getInstance());
            new TextGenerator(txtComment, post.getComment()).styleText();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;

        if (item == null) {
            LayoutInflater inf = LayoutInflater.from(parent.getContext());

            item = inf.inflate(R.layout.thread_post, parent, false);
        }

        final Post post = (Post) getItem(position);

        renderSubject(position, item, post);
        renderPosterName(item, post);

        // Load the timestamp.
        TextView txtTimestamp = (TextView) item.findViewById(R.id.post_date);
        txtTimestamp.setText(post.getFormattedTime());

        // Load the post number.
        TextView txtNumber = (TextView) item.findViewById(R.id.post_number);
        txtNumber.setText("#" + Long.toString(post.getPostNumber()));

        renderImage(item, post);
        renderComment(item, post);

        // Defer the copy button click handling.
        View btnCopy = item.findViewById(R.id.post_copy_icon);

        btnCopy.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (interactor != null) {
                    interactor.onTextCopy(post);
                }
            }
        });

        return item;
    }

}
