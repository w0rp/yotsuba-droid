package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.volley.toolbox.NetworkImageView;

public abstract class PostListAdapter extends BaseAdapter {
    private List<Post> postList = Collections.emptyList();

    protected void loadImage(Post post, NetworkImageView imageView) {
        if (post.getFile() == null) {
            // Blank the image when there's nothing to load.
            imageView.setImageDrawable(null);
            return;
        }

        ChanFile file = post.getFile();

        if (file.isDeleted()) {
            // Use the filedeleted image when files have been deleted.
            imageView.setImageResource(R.drawable.filedeleted);
            return;
        } else if (file.isSpoiler()) {
            // Hide spoiler images.
            imageView.setImageResource(R.drawable.spoiler);
            return;
        } else {
            // Set to blank while it loads.
            imageView.setImageDrawable(null);
        }

        final long id = post.getPostNumber();
        final String filename = file.getSmallName();

        // TODO: Use an ImageLoader here.
        imageView.setImageUrl(post.getSmallFileURL().toString(), Yot.getImageLoader());
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public Post getItem(int position) {
        // We know this is not null from the checks in setPostList.
        return postList.get(position);
    }

    public void setPostList(List<Post> postList) {
        // Make sure we don't pass in a list containing a null post here,
        // so our null analysis is correct.
        if (BuildConfig.DEBUG) {
            for (Post post : postList) {
                if (post == null) {
                    throw new AssertionError(
                        "A null Post was passed in the postList!"
                    );
                }
            }
        }

        this.postList = postList;

        notifyDataSetChanged();
    }

}
