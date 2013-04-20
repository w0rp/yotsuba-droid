package com.w0rp.yotsubadroid;

import java.net.URI;
import java.util.List;

import org.json.JSONObject;

import com.w0rp.androidutils.Async;
import com.w0rp.androidutils.Coerce;
import com.w0rp.androidutils.JSON;
import com.w0rp.androidutils.Net;
import com.w0rp.androidutils.SingleHTTPRequestTask;
import com.w0rp.yotsubadroid.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity implements OnItemClickListener {
    private class BoardDownloadTask extends SingleHTTPRequestTask {
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (Coerce.empty(result)) {
                // TODO: Notify of failure here somehow.
                return;
            }

            for (JSONObject boardObj : JSON.objIter(
            JSON.obj(result), "boards")) {
                Board board = Yot.cachedBoard(boardObj.optString("board"));

                board.setTitle(boardObj.optString("title"));
                board.setWorksafe(boardObj.optInt("ws_board") == 1);
                board.setPostsPerPage(boardObj.optInt("per_page"));
                board.setPageCount(boardObj.optInt("pages"));
            }

            sendBroadcast(new Intent(BoardListReceiver.class.getName()));
        }
    }

    private class BoardListReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            populateBoardList();
        }
    }

    private ArrayAdapter<String> boardAdapter;
    private BoardListReceiver boardListReceiver;
    private List<Board> currentBoardList;

    private void populateBoardList() {
        boardAdapter.setNotifyOnChange(false);
        boardAdapter.clear();

        currentBoardList = Yot.visibleBoardList();

        for (Board board : currentBoardList) {
            boardAdapter.add("/" + board.getID() + "/ - " + board.getTitle());
        }

        boardAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boardListReceiver = new BoardListReceiver();
        Async.registerClass(this, boardListReceiver);

        ListView boardListView = (ListView) findViewById(R.id.board_list);
        boardListView.setOnItemClickListener(this);
        boardAdapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1);
        boardListView.setAdapter(boardAdapter);

        new BoardDownloadTask().execute(Net.prepareGet(URI.create(Yot.API_URL
            + "boards.json")));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings: {
            Intent intent = new Intent(this, YotPreferenceActivity.class);
            startActivity(intent);
            return true;
        }
        default:
            return false;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {
        if (currentBoardList == null || position >= currentBoardList.size()) {
            return;
        }

        Board board = currentBoardList.get(position);

        Intent intent = new Intent(this, BoardCatalogActivity.class);
        intent.putExtra("boardID", board.getID());
        startActivity(intent);
    }
}
