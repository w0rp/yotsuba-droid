package com.w0rp.yotsubadroid;

import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BoardActivity extends Activity implements OnItemClickListener {
    private final class BoardListener implements Response.Listener<JSONObject>,
        Response.ErrorListener {
        @Override
        public void onResponse(JSONObject boardJSON) {
            setProgressBarIndeterminateVisibility(false);

            try {
                for (JSONObject boardObj : Util.jsonObjects(boardJSON, "boards")) {
                    Yot.saveBoard(Board.fromChanJSON(boardObj));
                }
            } catch (JSONException e) {
                Log.e(BoardActivity.class.getName(), e.toString());
            }

            populateBoardList();
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    private @Nullable ArrayAdapter<String> boardAdapter;
    private final BoardListener boardListener = new BoardListener();
    private List<Board> currentBoardList = Collections.emptyList();

    private void populateBoardList() {
        if (boardAdapter == null) {
            return;
        }

        boardAdapter.setNotifyOnChange(false);
        boardAdapter.clear();

        currentBoardList = Yot.visibleBoardList();

        for (Board board : currentBoardList) {
            boardAdapter.add("/" + board.getID() + "/ - " + board.getTitle());
        }

        boardAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);

        ListView boardListView = (ListView) findViewById(R.id.board_list);
        boardListView.setOnItemClickListener(this);
        boardAdapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1);
        boardListView.setAdapter(boardAdapter);

        setProgressBarIndeterminateVisibility(true);

        Yot.getRequestQueue().add(new JsonObjectRequest(
            Yot.API_URL + "boards.json",
            null,
            boardListener,
            boardListener
        ));
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
