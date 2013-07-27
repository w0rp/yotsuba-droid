package com.w0rp.yotsubadroid;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class BoardCatalogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_board_catalog);

        // Load the board from the intent data.
        Board board = Yot.cachedBoard(getIntent().getStringExtra("boardID"));

        getActionBar().setTitle(board.getTitle());

        findCatalogFragment().setBoardID(board.getID());
    }

    private BoardCatalogFragment findCatalogFragment() {
        return (BoardCatalogFragment) getFragmentManager()
            .findFragmentById(R.id.board_catalog_fragment);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            findCatalogFragment().updateCatalog();

            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
