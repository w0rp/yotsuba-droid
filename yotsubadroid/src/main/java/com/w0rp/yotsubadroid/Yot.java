package com.w0rp.yotsubadroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class Yot extends Application {
    public enum TBACK {
        ALWAYS,
        SOMETIMES,
        NEVER
    }

    public static final String API_URL = "https://a.4cdn.org/";

    private static Map<String, Board> boardMap = Collections.emptyMap();
    private static @Nullable RequestQueue requestQueue;
    private static @Nullable ImageLoader imageLoader;
    private static @Nullable Context context;
    private static @Nullable SharedPreferences prefs;

    private static void loadBoardMap(SharedPreferences sharedPrefs) {
        boardMap = new HashMap<String, Board>();

        JSONObject boardData;

        try {
            boardData = new JSONObject(sharedPrefs.getString("boardData", "{}"));
        } catch (JSONException e) {
            Log.e(Yot.class.getName(), "Parsing boardData JSON failed!");

            return;
        }

        for (String board : Util.iter(boardData)) {
            JSONObject obj = boardData.optJSONObject(board);

            if (obj == null) {
                continue;
            }

            boardMap.put(board, Board.fromJSON(obj));
        }
    }

    private static void saveBoardMap(Editor edit) {
        JSONObject boardData = new JSONObject();

        for (Board board : boardMap.values()) {
            try {
                boardData.put(board.getID(), board.toJSON());
            } catch (JSONException ignored) { }
        }

        edit.putString("boardData", boardData.toString());
    }

    /**
     * Save the preferences for the application.
     */
    public static void save() {
        assert prefs != null;

        Editor edit = prefs.edit();

        saveBoardMap(edit);

        edit.apply();
    }

    /**
     * @return true if non worksafe boards are enabled.
     */
    public static boolean nsfwEnabled() {
        assert prefs != null;

        return prefs.getBoolean("pref_nsfw", false);
    }

    public static TBACK backHistorySetting() {
        assert prefs != null;

        try {
            return Enum.valueOf(
                TBACK.class,
                prefs.getString("pref_thread_back", "ALWAYS")
            );
        } catch (Exception e) {
            return TBACK.ALWAYS;
        }
    }

    public static @Nullable Board boardByID(@Nullable String boardID) {
        return boardMap.get(boardID);
    }

    public static void saveBoard(Board board) {
        boardMap.put(board.getID(), board);
    }

    public static List<Board> visibleBoardList() {
        List<Board> list = new ArrayList<Board>();

        // Add in the boards, filtered.
        for (Board board : boardMap.values()) {
            if (board.isWorksafe() || nsfwEnabled()) {
                list.add(board);
            }
        }

        // Sort the boards by their short names.
        Collections.sort(list, (lhs, rhs) ->{
            return lhs.getID().compareTo(rhs.getID());
        });

        return list;
    }

    public static @NonNull Drawable defaultCatImage() {
        assert context != null;

        Drawable image = context.getResources().getDrawable(
            android.R.drawable.ic_menu_save
        );

        if (image == null) {
            throw new NullPointerException();
        }

        return image;
    }

    public static RequestQueue getRequestQueue() {
        assert requestQueue != null;

        return requestQueue;
    }

    public static ImageLoader getImageLoader() {
        assert imageLoader != null;

        return imageLoader;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs != null) {
            loadBoardMap(prefs);
        }

        requestQueue = Volley.newRequestQueue(this);

        imageLoader = new ImageLoader(requestQueue, new ImageCache());
    }
}
