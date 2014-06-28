package com.w0rp.yotsubadroid;

import org.eclipse.jdt.annotation.Nullable;

import com.w0rp.yotsubadroid.Yot.TBACK;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class ThreadViewActivity extends Activity {
    @Nullable ThreadViewFragment threadFrag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_thread_view);

        Intent intent = getIntent();

        threadFrag = (ThreadViewFragment) getFragmentManager()
            .findFragmentById(R.id.thread_view_fragment);

        // Load the board from the intent data.
        @Nullable String boardID = intent.getStringExtra("boardID");

        assert boardID != null;

        long threadID = intent.getLongExtra("threadID", 0);
        long postID = intent.getLongExtra("postID", threadID);

        if (threadFrag != null) {
            threadFrag.setData(boardID, threadID, postID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@Nullable Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_thread_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@Nullable MenuItem item) {
        if (item == null) {
            return false;
        }

        switch (item.getItemId()) {
        case R.id.menu_settings: {
            Intent intent = new Intent(this, ThreadPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        case R.id.menu_refresh:
            if (threadFrag != null) {
                threadFrag.updateThread();
            }

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (Yot.backHistorySetting() == TBACK.NEVER
        || threadFrag == null
        || (threadFrag != null && !threadFrag.skipBack())) {
            super.onBackPressed();
        }
    }
}
