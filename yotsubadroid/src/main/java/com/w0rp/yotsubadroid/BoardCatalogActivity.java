package com.w0rp.yotsubadroid;

import java.util.List;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.os.Bundle;
import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class BoardCatalogActivity extends Activity {
    private static final Pattern boardPattern = Pattern.compile(
        "/([a-zA-Z0-9_]+)/?(?:catalog)?$"
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_board_catalog);

        final BoardCatalogFragment fragment = findCatalogFragment();

        String boardID = null;

        if (getIntent().getData() != null) {
            List<String> matches = Util.search(
                boardPattern,
                getIntent().getDataString()
            );

            if (matches.size() == 2) {
                boardID = matches.get(1);
            }
        } else {
            boardID = getIntent().getStringExtra("boardID");
        }

        Board board = Yot.boardByID(boardID);

        if (board != null) {
            ActionBar actionBar = getActionBar();

            if (actionBar != null) {
                actionBar.setTitle(board.getTitle());
            }

            if (fragment != null) {
                fragment.setBoardID(board.getID());
            }
        } else {
            Toast.makeText(this, "Unknown board ID!", Toast.LENGTH_LONG)
            .show();
        }
    }

    private @Nullable BoardCatalogFragment findCatalogFragment() {
        return (BoardCatalogFragment) getFragmentManager()
            .findFragmentById(R.id.board_catalog_fragment);
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
            final BoardCatalogFragment fragment =
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
