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

import com.w0rp.androidutils.FileRotator;
import com.w0rp.androidutils.IO;
import com.w0rp.androidutils.JSON;
import com.w0rp.androidutils.SLog;

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

public class Yot extends Application {
    public static final int CAT_ITEM_WIDTH = 400;
    public static final int CAT_ITEM_HEIGHT = 300;
    public static final String API_URL = "https://api.4chan.org/";

    private static Context context;
    private static Map<String, Board> boardMap;
    private static SharedPreferences prefs;
    private static Drawable defCatImage;
    private static FileRotator fileRotator;

    private static void loadBoardMap() {
        boardMap = new HashMap<String, Board>();

        JSONObject boardData = null;

        try {
            boardData = new JSONObject(prefs.getString("boardData", "{}"));
        } catch (JSONException e) { }

        for (String board : JSON.keys(boardData)) {
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
        File cacheDir = context.getExternalCacheDir();

        if (cacheDir == null) {
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

    public static void save() {
        Editor edit = prefs.edit();

        saveBoardMap(edit);

        edit.commit();
    }

    public static boolean nsfwEnabled() {
        return prefs.getBoolean("pref_nsfw", false);
    }

    public static Board cachedBoard(String boardID) {
        Board boardObj = boardMap.get(boardID);

        if (boardObj == null) {
            boardObj = new Board(boardID);
            boardMap.put(boardID, boardObj);
        }

        return boardObj;
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

    public static Drawable defaultCatImage() {
        return defCatImage;
    }

    public static boolean cachedFileExists(String filename) {
        return cachedFile(filename).exists();
    }

    public static Bitmap loadImage(String filename) {
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
            SLog.e(e);
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
        Handler handler = new Handler(context.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
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

        loadBoardMap();

        defCatImage = getResources().getDrawable(
            android.R.drawable.ic_menu_save);

        // Delete all of the saved images when the application starts.
        deleteAllImages();

        // TODO: Get MB size from setting.
        fileRotator = new FileRotator(200 * 1024 * 1024);
    }
}
