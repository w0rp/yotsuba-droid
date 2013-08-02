package com.w0rp.yotsubadroid;

import java.net.URI;
import java.text.DateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.net.Uri;

public class Post {
    private static final DateFormat dateFormat =
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private String boardID = "";
    private ChanFile file = null;
    private long time = 0;
    private long postNumber = 0;
    private long replyTo = 0;
    private String posterName = "";
    private String email = "";
    private String subject = "";
    private String comment = "";
    private String tripcode = "";
    private String posterID = "";
    private String capcode = "";
    private String countryCode = "";
    // Thread specific fields
    private int customSpoiler = 0;
    private int replyCount = 0;
    private int imageCount = 0;
    private boolean bumpLimitHit = false;
    private boolean imageLimitHit = false;

    public static Post fromChanJSON(String boardID, JSONObject obj) {
        Post post = new Post(boardID);

        if (obj.optString("filename").length() > 0) {
            try {
                post.setFile(ChanFile.fromChanJSON(obj));
            } catch (JSONException e) { }
        }

        post.setTime(obj.optLong("time"));
        post.setPostNumber(obj.optLong("no"));
        post.setReplyTo(obj.optLong("resto"));
        post.setPosterName(obj.optString("name"));
        post.setEmail(obj.optString("email"));
        post.setSubject(obj.optString("sub"));
        post.setComment(obj.optString("com"));
        post.setTripcode(obj.optString("trip"));
        post.setPosterID(obj.optString("id"));
        post.setCapcode(obj.optString("capcode"));
        post.setCountryCode(obj.optString("country"));

        if (post.isThread()) {
            post.setCustomSpoiler(obj.optInt("custom_spoiler"));
            post.setReplyCount(obj.optInt("replies"));
            post.setImageCount(obj.optInt("images"));
            post.setBumpLimitHit(obj.optInt("bumplimit") == 1);
            post.setImageLimitHit(obj.optInt("imagelimit") == 1);
        }

        return post;
    }

    public static Post fromJSON(JSONObject obj) {
        JSONObject fileObj = obj.optJSONObject("file");

        Post post = new Post(obj.optString("boardID"));

        if (fileObj != null) {
            post.setFile(ChanFile.fromJSON(fileObj));
        }

        post.setTime(obj.optLong("time"));
        post.setPostNumber(obj.optLong("postNumber"));
        post.setReplyTo(obj.optLong("replyTo"));
        post.setPosterName(obj.optString("posterName"));
        post.setEmail(obj.optString("email"));
        post.setSubject(obj.optString("subject"));
        post.setComment(obj.optString("comment"));
        post.setTripcode(obj.optString("tripcode"));
        post.setPosterID(obj.optString("posterID"));
        post.setCapcode(obj.optString("capcode"));
        post.setCountryCode(obj.optString("countryCode"));

        if (post.isThread()) {
            post.setCustomSpoiler(obj.optInt("customSpoiler"));
            post.setReplyCount(obj.optInt("replyCount"));
            post.setImageCount(obj.optInt("imageCount"));
            post.setBumpLimitHit(obj.optBoolean("bumpLimitHit"));
            post.setImageLimitHit(obj.optBoolean("imageLimitHit"));
        }

        return post;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();

        try {
            if (file != null) {
                obj.put("file", file.toJSON());
            }

            obj.put("boardID", boardID);
            obj.put("time", time);
            obj.put("postNumber", postNumber);
            obj.put("replyTo", replyTo);
            obj.put("posterName", posterName);
            obj.put("email", email);
            obj.put("subject", subject);
            obj.put("comment", comment);
            obj.put("tripcode", tripcode);
            obj.put("posterID", posterID);
            obj.put("capcode", capcode);
            obj.put("countryCode", countryCode);

            if (isThread()) {
                obj.put("customSpoiler", customSpoiler);
                obj.put("replyCount", replyCount);
                obj.put("imageCount", imageCount);
                obj.put("bumpLimitHit", bumpLimitHit);
                obj.put("imageLimitHit", imageLimitHit);
            }
        } catch (JSONException e) {
        }

        return obj;
    }

    @SuppressLint("DefaultLocale")
    protected Post(String boardID) {
        this.boardID = boardID.toLowerCase();
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public String getBoardID() {
        return this.boardID;
    }

    public ChanFile getFile() {
        return file;
    }

    protected void setFile(ChanFile file) {
        this.file = file;
    }

    public long getTime() {
        return time;
    }


    protected void setTime(long time) {
        this.time = time;
    }

    public long getPostNumber() {
        return postNumber;
    }

    protected void setPostNumber(long postNumber) {
        this.postNumber = postNumber;
    }

    public String getPosterName() {
        return posterName;
    }

    protected void setPosterName(String posterName) {
        this.posterName = posterName;
    }

    public String getTripcode() {
        return tripcode;
    }

    protected void setTripcode(String tripcode) {
        this.tripcode = tripcode;
    }

    public String getPosterID() {
        return posterID;
    }

    protected void setPosterID(String posterID) {
        this.posterID = posterID;
    }

    public String getCapcode() {
        return capcode;
    }

    protected void setCapcode(String capcode) {
        this.capcode = capcode;
    }

    public long getReplyTo() {
        return replyTo;
    }

    protected void setReplyTo(long replyTo) {
        this.replyTo = replyTo;
    }

    public String getCountryCode() {
        return countryCode;
    }

    protected void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getEmail() {
        return email;
    }

    protected void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    protected void setSubject(String subject) {
        this.subject = subject;
    }

    public String getComment() {
        return comment;
    }

    protected void setComment(String comment) {
        this.comment = comment;
    }

    public int getCustomSpoiler() {
        return customSpoiler;
    }

    protected void setCustomSpoiler(int customSpoiler) {
        this.customSpoiler = customSpoiler;
    }

    public int getReplyCount() {
        return replyCount;
    }

    protected void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getImageCount() {
        return imageCount;
    }

    protected void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public boolean isBumpLimitHit() {
        return bumpLimitHit;
    }

    protected void setBumpLimitHit(boolean bumpLimitHit) {
        this.bumpLimitHit = bumpLimitHit;
    }

    public boolean isImageLimitHit() {
        return imageLimitHit;
    }

    protected void setImageLimitHit(boolean imageLimitHit) {
        this.imageLimitHit = imageLimitHit;
    }

    public boolean isThread() {
        return replyTo > 0;
    }

    public boolean hasFile() {
        return file != null && !file.isDeleted();
    }

    public boolean isModPost() {
        // Looking for this bit of HTML is rather unfortunately the only
        // actually reliable means of determining this.
        return getPosterName().contains("<span class=\"commentpostername\"");
    }

    public URI getSmallFileURL() {
        if (!hasFile()) {
            return null;
        }

        return URI.create("http://thumbs.4chan.org/" + Uri.encode(boardID)
            + "/thumb/" + file.getSmallName());
    }

    public URI getFileURL() {
        if (!hasFile()) {
            return null;
        }

        return URI.create("http://images.4chan.org/" + Uri.encode(boardID)
            + "/src/" + file.getName());
    }

    /**
     * @return The time and date in a locale dependent format.
     */
    public String getFormattedTime() {
        // Date expects milliseconds, not seconds.
        return dateFormat.format(new Date(time * 1000));
    }
}