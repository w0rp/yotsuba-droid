package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.w0rp.androidutils.Coerce;
import com.w0rp.androidutils.Util;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public abstract class PostListAdapter extends BaseAdapter
implements ImageWorker.OnImageReceivedListener {
    private List<Post> postList = Coerce.emptyList();
    private final ThreadPoolExecutor pool;
    private final Map<Long, ImageView> imageMap;

    public PostListAdapter() {
        pool = Util.pool(32, 5, Coerce.notnull(TimeUnit.SECONDS));
        imageMap = new HashMap<Long, ImageView>();
    }

    protected void loadImage(Post post, ImageView imageView) {
        if (post.getFile() == null) {
            // Blank the image when there's nothing to load.
            imageView.setImageDrawable(null);
            return;
        }

        @NonNull ChanFile file = Coerce.notnull(post.getFile());

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

        @Nullable Bitmap bitmap = Yot.loadImage(filename);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageMap.remove(id);
            return;
        }

        if (imageMap.containsKey(id)) {
            // Replace the existing ImageView if we're still loading the image.
            imageMap.put(id, imageView);
            return;
        }

        // Start up a request for the image, saving a reference to this
        // ImageView in the map. We'll load it in later.
        imageMap.put(id, imageView);

        final @NonNull URI url = Coerce.notnull(post.getSmallFileURL());

        ImageWorker worker = new ImageWorker(id, url, filename);
        worker.setOnImageReceivedListener(this);
        pool.execute(worker);
    }

    @Override
    public void onImageReceived(long id, String filename) {
        ImageView imageView = imageMap.get(id);

        if (imageView == null) {
            // Nothing to load the image into!
            return;
        }

        imageView.setImageBitmap(Yot.loadImage(filename));
        imageMap.remove(id);
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
        return Coerce.notnull(postList.get(position));
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
