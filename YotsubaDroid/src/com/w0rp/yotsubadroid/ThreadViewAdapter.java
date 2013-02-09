package com.w0rp.yotsubadroid;

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
            
            item = (RelativeLayout) inf.inflate(
                R.layout.thread_post, parent, false);
        }
        
        final Post post = (Post) getItem(position);
        
        ImageView imageView = (ImageView) item.findViewById(R.id.post_image);
        loadImage(post, imageView);
        
        TextView comment = (TextView) item.findViewById(R.id.post_comment);
        comment.setText(Comment.fullText(post.getComment()));
        
        return item;
    }

}
