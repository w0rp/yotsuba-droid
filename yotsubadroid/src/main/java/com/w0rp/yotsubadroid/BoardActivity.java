package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.w0rp.androidutils.Async;
import com.w0rp.androidutils.Net;
import com.w0rp.androidutils.SingleHTTPRequestTask;
import com.w0rp.yotsubadroid.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BoardActivity extends Activity implements OnItemClickListener {
    private static final URI BOARD_JSON_URL = URI.create(
        Yot.API_URL + "boards.json"
    );

    private class BoardDownloadTask extends SingleHTTPRequestTask {
        @Override
        protected void onPostExecute(@Nullable String result) {
            super.onPostExecute(result);

            if (result == null || result.length() == 0) {
                // TODO: Notify of failure here somehow.

                setProgressBarIndeterminateVisibility(false);

                return;
            }

            JSONObject boardJSON = null;

            try {
                boardJSON = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            for (JSONObject boardObj : JSON.objIter(boardJSON, "boards")) {
                Yot.saveBoard(Board.fromChanJSON(boardObj));
            }

            sendBroadcast(new Intent(BoardListReceiver.class.getName()));
        }
    }

    private class BoardListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(
        @Nullable Context context, @Nullable Intent intent) {
            setProgressBarIndeterminateVisibility(false);
            populateBoardList();
        }
    }

    private @Nullable ArrayAdapter<String> boardAdapter;
    private @Nullable BoardListReceiver boardListReceiver;
    private List<Board> currentBoardList = Collections.emptyList();

    private void populateBoardList() {
        final ArrayAdapter<String> checkedAdapter = boardAdapter;

        if (checkedAdapter == null) {
            return;
        }

        checkedAdapter.setNotifyOnChange(false);
        checkedAdapter.clear();

        currentBoardList = Yot.visibleBoardList();

        for (Board board : currentBoardList) {
            checkedAdapter.add("/" + board.getID() + "/ - " + board.getTitle());
        }

        checkedAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        boardListReceiver = new BoardListReceiver();
        Async.registerClass(this, boardListReceiver);

        ListView boardListView = (ListView) findViewById(R.id.board_list);
        boardListView.setOnItemClickListener(this);
        boardAdapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1);
        boardListView.setAdapter(boardAdapter);

        setProgressBarIndeterminateVisibility(true);

        new BoardDownloadTask().execute(Net.prepareGet(BOARD_JSON_URL));
    }

    @Override
    protected void onResume() {
        super.onResume();

        populateBoardList();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Yot.save();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(boardListReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(@Nullable Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@Nullable MenuItem item) {
        if (item == null) {
            return false;
        }

        switch (item.getItemId()) {
        case R.id.menu_settings: {
            Intent intent = new Intent(this, BoardPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        default:
            return false;
        }
    }

    @Override
    public void onItemClick(
    @Nullable AdapterView<?> parent, @Nullable View view, int position,
        long id) {
        if (position >= currentBoardList.size()) {
            return;
        }

        Board board = currentBoardList.get(position);

        Intent intent = new Intent(this, BoardCatalogActivity.class);
        intent.putExtra("boardID", board.getID());
        startActivity(intent);
    }
}
