package com.w0rp.yotsubadroid;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import com.w0rp.androidutils.JSON;

public class Board {
    /**
     * Create a board object from JSON taken from 4chan.
     *
     * @param obj The 4chan JSON object.
     * @return A new board object.
     */
    public static Board fromChanJSON(@NonNull JSONObject obj) {
        Board board = new Board(obj.optString("board"));

        board.setTitle(obj.optString("title"));
        board.setWorksafe(obj.optInt("ws_board") == 1);
        board.setPostsPerPage(obj.optInt("per_page"));
        board.setPageCount(obj.optInt("pages"));

        return board;
    }

    /**
     * Create a board object from our own JSON representation.
     *
     * @param obj Our JSON representation of a board.
     * @return A new board object.
     */
    public static Board fromJSON(JSONObject obj) {
        Board board = new Board(obj.optString("id"));

        board.setTitle(obj.optString("title"));
        board.setWorksafe(obj.optBoolean("worksafe", false));
        board.setPostsPerPage(obj.optInt("postsPerPage", 0));
        board.setPageCount(obj.optInt("pageCount", 0));

        return board;
    }

    private String id = "";
    private String title = "";
    private boolean worksafe = false;
    private int postsPerPage = 0;
    private int pageCount = 0;

    @SuppressLint("DefaultLocale")
    public Board(String id) {
        this.id = id.toLowerCase();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("id", id);
            obj.put("title", title);
            obj.put("worksafe", worksafe);
            obj.put("postsPerPage", postsPerPage);
            obj.put("pageCount", pageCount);
        } catch (JSONException e) {
        }

        return obj;
    }

    @Override
    public @Nullable String toString() {
        return toJSON().toString();
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isWorksafe() {
        return worksafe;
    }

    public void setWorksafe(boolean worksafe) {
        this.worksafe = worksafe;
    }

    public int getPostsPerPage() {
        return postsPerPage;
    }

    public void setPostsPerPage(int postsPerPage) {
        this.postsPerPage = postsPerPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
