package com.w0rp.yotsubadroid;

import java.util.List;
import java.util.regex.Pattern;

import com.w0rp.yotsubadroid.Yot.TBACK;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class ThreadViewActivity extends Activity {
    @Nullable ThreadViewFragment threadFrag;

    private static Pattern threadPattern = Pattern.compile(
        "/([a-zA-Z0-9_]+)/thread/(\\d+)"
    );

    private static Pattern postPattern = Pattern.compile("#[pq](\\d+)");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_thread_view);

        threadFrag = (ThreadViewFragment) getFragmentManager()
            .findFragmentById(R.id.thread_view_fragment);

        if (getIntent().getData() != null) {
            List<String> threadMatches = Util.search(
                threadPattern,
                getIntent().getDataString()
            );

            if (threadMatches.size() == 3) {
                @NonNull String boardID = threadMatches.get(1);
                long threadID = Long.parseLong(threadMatches.get(2));

                long postID = 0;

                List<String> postMatches = Util.search(
                    postPattern,
                    getIntent().getDataString()
                );

                if (postMatches.size() == 2) {
                    // Include the optional post ID, if set.
                    postID = Long.parseLong(postMatches.get(1));
                }

                if (threadFrag != null) {
                    threadFrag.setData(boardID, threadID, postID);
                }
            }
        } else {
            // Load the board from the intent data.
            @Nullable String boardID = getIntent().getStringExtra("boardID");

            assert boardID != null;

            long threadID = getIntent().getLongExtra("threadID", 0);
            long postID = getIntent().getLongExtra("postID", threadID);

            if (threadFrag != null) {
                threadFrag.setData(boardID, threadID, postID);
            }
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
