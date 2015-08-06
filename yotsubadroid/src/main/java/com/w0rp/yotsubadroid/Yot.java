package com.w0rp.yotsubadroid;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class Yot extends Application {
    public static enum TBACK {
        ALWAYS,
        SOMETIMES,
        NEVER
    }

    public static final String API_URL = "https://a.4cdn.org/";

    private static Map<String, Board> boardMap = Collections.emptyMap();
    private static FileRotator fileRotator = new FileRotator(200 * 1024 * 1024);
    private static @Nullable Context context;
    private static @Nullable SharedPreferences prefs;

    private static void loadBoardMap(SharedPreferences sharedPrefs) {
        boardMap = new HashMap<String, Board>();

        JSONObject boardData = null;

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
            } catch (JSONException e) { }
        }

        edit.putString("boardData", boardData.toString());
    }

    private static File cacheSubdir(String dirname) {
        assert context != null;
        File cacheDir = context.getExternalCacheDir();

        if (cacheDir == null) {
            assert context != null;
            cacheDir = context.getCacheDir();
        }

        File subDir = new File(cacheDir.getPath() + "/" + dirname);
        subDir.mkdirs();

        return subDir;
    }

    private static File cachedFile(String filename) {
        File dir = cacheSubdir("image");
        return new File(dir + "/" + filename);
    }

    /**
     * Save the preferences for the application.
     */
    public static void save() {
        assert prefs != null;

        Editor edit = prefs.edit();

        saveBoardMap(edit);

        edit.commit();
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
        Collections.sort(list, new Comparator<Board>() {
            @Override
            public int compare(Board lhs, Board rhs) {
                return lhs.getID().compareTo(rhs.getID());
            }
        });

        return list;
    }

    public static @NonNull Drawable defaultCatImage() {
        assert context != null;

        return context.getResources().getDrawable(
            android.R.drawable.ic_menu_save);
    }

    public static boolean cachedFileExists(String filename) {
        return cachedFile(filename).exists();
    }

    public static @Nullable Bitmap loadImage(String filename) {
        File inFile = cachedFile(filename);

        if (!inFile.exists()) {
            return null;
        }

        return BitmapFactory.decodeFile(inFile.getPath());
    }

    public static void saveImage(String filename, InputStream in) {
        File outFile = cachedFile(filename);

        try {
            FileOutputStream out = new FileOutputStream(outFile.getPath());
            IO.stream(in, out);
            IO.close(out);
        } catch (Exception e) {
            Log.e(Yot.class.getName(), e.toString());
            return;
        }

        fileRotator.add(outFile);
    }

    public static void deleteAllImages() {
        File cacheDir = cacheSubdir("image");

        for (String filename : cacheDir.list()) {
            new File(cacheDir, filename).delete();
        }
    }

    public static void runOnUiThread(final Runnable runnable) {
        assert context != null;

        @SuppressWarnings("null")
        Handler handler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(@Nullable Message msg) {
                runnable.run();
            }
        };

        handler.sendEmptyMessage(0);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs != null) {
            loadBoardMap(prefs);
        }

        // Delete all of the saved images when the application starts.
        deleteAllImages();
    }
}
