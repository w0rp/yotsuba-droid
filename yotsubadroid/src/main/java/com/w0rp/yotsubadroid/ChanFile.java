package com.w0rp.yotsubadroid;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import com.w0rp.androidutils.JSON;

public class ChanFile {
    private String prename = "";
    private String origPrename = "";
    private String extension = "";
    private boolean spoiler = false;
    private boolean deleted = false;
    private int width = 0;
    private int height = 0;
    private int smallWidth = 0;
    private int smallHeight = 0;
    private String md5 = "";
    private int size = 0;

    public static ChanFile fromChanJSON(JSONObject obj) throws JSONException {
        ChanFile file = new ChanFile();

        file.setPrename(obj.getString("tim"));
        file.setOrigPrename(obj.getString("filename"));
        file.setExtension(obj.getString("ext"));
        file.setSpoiler(obj.optInt("spoiler") == 1);
        file.setDeleted(obj.optInt("filedeleted") == 1);
        file.setWidth(obj.optInt("w"));
        file.setHeight(obj.optInt("h"));
        file.setSmallWidth(obj.optInt("tn_w"));
        file.setSmallHeight(obj.optInt("tn_h"));
        file.setMD5(obj.optString("md5"));
        file.setSize(obj.optInt("fsize"));

        return file;
    }

    public static ChanFile fromJSON(JSONObject obj) {
        ChanFile file = new ChanFile();

        file.setPrename(obj.optString("prename"));
        file.setOrigPrename(obj.optString("origPrename"));
        file.setExtension(obj.optString("extension"));
        file.setSpoiler(obj.optBoolean("spoiler"));
        file.setDeleted(obj.optBoolean("deleted"));
        file.setWidth(obj.optInt("width"));
        file.setHeight(obj.optInt("height"));
        file.setSmallWidth(obj.optInt("smallWidth"));
        file.setSmallHeight(obj.optInt("smallHeight"));
        file.setMD5(obj.optString("md5"));
        file.setSize(obj.optInt("size"));

        return file;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            obj.put("prename", prename);
            obj.put("origPrename", origPrename);
            obj.put("extension", extension);
            obj.put("deleted", deleted);
            obj.put("spoiler", spoiler);
            obj.put("width", width);
            obj.put("height", height);
            obj.put("smallWidth", smallWidth);
            obj.put("smallHeight", smallHeight);
            obj.put("md5", md5);
            obj.put("size", size);
        } catch (JSONException e) {
        }

        return obj;
    }

    @Override
    public @Nullable String toString() {
        return toJSON().toString();
    }

    public boolean isSpoiler() {
        return spoiler;
    }

    protected void setSpoiler(boolean spoiler) {
        this.spoiler = spoiler;
    }

    public int getWidth() {
        return width;
    }

    protected void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    protected void setHeight(int height) {
        this.height = height;
    }

    public int getSmallWidth() {
        return smallWidth;
    }

    protected void setSmallWidth(int smallWidth) {
        this.smallWidth = smallWidth;
    }

    public int getSmallHeight() {
        return smallHeight;
    }

    protected void setSmallHeight(int smallHeight) {
        this.smallHeight = smallHeight;
    }

    public String getMD5() {
        return md5;
    }

    protected void setMD5(String md5) {
        this.md5 = md5;
    }

    public int getSize() {
        return size;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    public boolean isDeleted() {
        return deleted;
    }

    protected void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getPrename() {
        return prename;
    }

    protected void setPrename(String preName) {
        this.prename = preName;
    }

    public String getOrigPrename() {
        return origPrename;
    }

    protected void setOrigPrename(String origPreName) {
        this.origPrename = origPreName;
    }

    public String getExtension() {
        return extension;
    }

    protected void setExtension(String extension) {
        this.extension = extension;
    }

    public String getName() {
        return this.prename + this.extension;
    }

    public String getOrigname() {
        return this.origPrename + this.extension;
    }

    public String getSmallName() {
        return this.prename + "s.jpg";
    }
}
