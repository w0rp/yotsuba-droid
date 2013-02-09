package com.w0rp.yotsubadroid;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class BoardCatalogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_catalog);

        // Load the board from the intent data.
        Board board = Yot.cachedBoard(getIntent().getStringExtra("boardID"));

        getActionBar().setTitle(board.getTitle());

        BoardCatalogFragment frag = (BoardCatalogFragment) getFragmentManager()
            .findFragmentById(R.id.board_catalog_fragment);
        frag.setBoardID(board.getID());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_board_catalog, menu);
        return true;
    }

}
