package com.w0rp.yotsubadroid;

import com.android.volley.toolbox.NetworkImageView;
import com.w0rp.yotsubadroid.ChanHTML.QuotelinkClickHandler;
import com.w0rp.yotsubadroid.ChanHTML.TextGenerator;

import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ThreadViewAdapter extends PostListAdapter {
    public interface ThreadInteractor extends QuotelinkClickHandler {
        void onImageClick(Post post);
        void onTextCopy(Post post);
    }

    private final ThreadInteractor interactor;

    public ThreadViewAdapter(ThreadInteractor interactor) {
        this.interactor = interactor;
    }

    private void renderSubject(int position, View item, Post post) {
        final TextView txtSubject =
            (TextView) item.findViewById(R.id.post_subject);

        if (txtSubject == null) {
            return;
        }

        if (position == 0) {
            // Always hide the first subject.
            txtSubject.setVisibility(View.GONE);
        } else {
            Util.textOrHide(txtSubject,
                ChanHTML.rawText(post.getSubject()).trim());
        }
    }

    private void renderPosterName(View item, Post post) {
        final TextView txtName =
            (TextView) item.findViewById(R.id.post_poster_name);

        if (txtName == null) {
            return;
        }

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
        final RelativeLayout imageLayout =
            (RelativeLayout) item.findViewById(R.id.post_image_layout);

        if (imageLayout == null) {
            return;
        }

        final NetworkImageView imageView = (NetworkImageView) item
            .findViewById(R.id.catalog_item_image);

        if (imageView == null) {
            return;
        }

        if (post.getFile() != null) {
            // Show layouts which have an image.
            imageLayout.setVisibility(View.VISIBLE);
            loadImage(post, imageView);

            imageView.setOnClickListener(x -> interactor.onImageClick(post));
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

            TextGenerator textGen = new TextGenerator(txtComment, post);

            textGen.setQuotelinkClickHandler(interactor);

            textGen.styleText();
        }
    }

    @Override
    public View getView(int position,
    @Nullable View convertView, @Nullable ViewGroup parent) {
        View item;

        if (convertView == null) {
            assert parent != null;

            LayoutInflater inf = LayoutInflater.from(parent.getContext());

            View inflatedView = inf.inflate(
                R.layout.thread_post, parent, false
            );

            assert inflatedView != null;

            item = inflatedView;
        } else {
            item = convertView;
        }

        final Post post = getItem(position);

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
            @Override public void onClick(@Nullable View v) {
                interactor.onTextCopy(post);
            }
        });

        return item;
    }

}
