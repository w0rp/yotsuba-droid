package com.w0rp.yotsubadroid;

import org.eclipse.jdt.annotation.Nullable;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class BoardCatalogActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_board_catalog);

        @Nullable String boardID = getIntent().getStringExtra("boardID");

        if (boardID != null) {
            // Load the board from the intent data.
            Board board = Yot.cachedBoard(boardID);

            getActionBar().setTitle(board.getTitle());

            @Nullable final BoardCatalogFragment fragment =
                findCatalogFragment();

            if (fragment != null) {
                fragment.setBoardID(board.getID());
            } else {
                throw new AssertionError("catalog fragment missing!");
            }
        } else {
            throw new AssertionError("boardID not set!");
        }
    }

    private @Nullable BoardCatalogFragment findCatalogFragment() {
        return (BoardCatalogFragment) getFragmentManager()
            .findFragmentById(R.id.board_catalog_fragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(@Nullable Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_board_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@Nullable MenuItem item) {
        if (item == null) {
            return false;
        }

        switch (item.getItemId()) {
        case R.id.menu_refresh:
            @Nullable final BoardCatalogFragment fragment =
                findCatalogFragment();

            if (fragment != null) {
                fragment.updateCatalog();
                return true;
            }

            return false;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}
