package com.w0rp.yotsubadroid;

import com.w0rp.yotsubadroid.Yot.TBACK;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

public class ThreadViewActivity extends Activity {
    ThreadViewFragment threadFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_view);

        Intent intent = getIntent();

        // Load the board from the intent data.
        String boardID = intent.getStringExtra("boardID");
        long threadID = intent.getLongExtra("threadID", 0);
        long postID = intent.getLongExtra("postID", threadID);

        threadFrag = (ThreadViewFragment) getFragmentManager()
            .findFragmentById(R.id.thread_view_fragment);

        threadFrag.setData(boardID, threadID, postID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_thread_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings: {
            Intent intent = new Intent(this, ThreadPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        default:
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (Yot.backHistorySetting() == TBACK.NEVER || !threadFrag.skipBack()) {
            super.onBackPressed();
        }
    }
}
