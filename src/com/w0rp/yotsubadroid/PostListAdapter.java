package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.w0rp.androidutils.Util;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public abstract class PostListAdapter extends BaseAdapter
implements ImageWorker.OnImageReceivedListener {
    private List<Post> postList;
    private ThreadPoolExecutor pool;
    private Map<Long, ImageView> imageMap;

    public PostListAdapter() {
        pool = Util.pool(32, 5, TimeUnit.SECONDS);
        imageMap = new HashMap<Long, ImageView>();
    }

    protected void loadImage(Post post, ImageView imageView) {
        // Load the thumbnail image from disk.
        if (post.getFile() != null) {
            if (post.getFile().isDeleted()) {
                // Use the filedeleted image when files have been deleted.
                imageView.setImageResource(R.drawable.filedeleted);
                return;
            } else if (post.getFile().isSpoiler()) {
                // Hide spoiler images.
                imageView.setImageResource(R.drawable.spoiler);
                return;
            } else {
                // Set to blank while it loads.
                imageView.setImageDrawable(null);
            }
        } else {
            // Blank the image when there's nothing to load.
            imageView.setImageDrawable(null);
            return;
        }

        final long id = post.getPostNumber();
        final String filename = post.getFile().getSmallName();

        Bitmap bitmap = Yot.loadImage(filename);

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

        final URI url = post.getSmallFileURL();

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
        return postList == null ? 0 : postList.size();
    }

    @Override
    public Object getItem(int position) {
        return this.postList.get(position);
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;

        notifyDataSetChanged();
    }

}
