package com.w0rp.yotsubadroid;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class ThreadViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_view);
        
        Intent intent = getIntent();
        
        // Load the board from the intent data.
        Board board = Yot.cachedBoard(intent.getStringExtra("boardID"));
        long threadID = intent.getLongExtra("threadID", 0);
        
        getActionBar().setTitle(board.getTitle() + " - " + threadID);
        
        ThreadViewFragment frag = (ThreadViewFragment) getFragmentManager()
            .findFragmentById(R.id.thread_view_fragment);
        
        frag.setData(board.getID(), threadID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_thread_view, menu);
        return true;
    }

}
