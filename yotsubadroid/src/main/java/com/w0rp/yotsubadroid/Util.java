package com.w0rp.yotsubadroid;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Util {
    public static class IteratorIterable<T> implements Iterable<T> {
        final Iterator<T> iterator;

        public IteratorIterable(@NonNull Iterator<T> iterator) {
            this.iterator = iterator;
        }

        @Override
        public @NonNull Iterator<T> iterator() {
            return iterator;
        }
    }

    private static abstract class JSONArrayIterator<T> implements Iterator<T> {
        JSONArray array;
        int index = 0;

        @Override
        public final void remove() {
            ++index;
        }

        @Override
        public final boolean hasNext() {
            return index < array.length();
        }
    }

    public static class JSONArrayJOIterator
    extends JSONArrayIterator<JSONObject> {
        @Override
        public @Nullable JSONObject next() {
            return array.optJSONObject(index);
        }
    }

    public static <T> Iterable<T> iter(@NonNull Iterator<T> iterator) {
        return new IteratorIterable<T>(iterator);
    }

    public static Iterable<String> iter(@NonNull JSONObject obj) {
        return new IteratorIterable<String>(obj.keys());
    }

    /**
     * Given a JSONArray, return an Iterable through the JSON objects in the
     * array.
     *
     * @param array The array to iterate through.
     * @return An Iterable through the objects in the array.
     */
    public static Iterable<JSONObject> jsonObjects(@NonNull JSONArray array) {
        JSONArrayJOIterator iterator = new JSONArrayJOIterator();
        iterator.array = array;

        return iter(iterator);
    }

    /**
     * Given a JSON object and a key, return an iterable through JSON objects
     * at that key.
     *
     * @param obj The JSON object.
     * @param key The key for the array.
     * @return An iterable through the objects.
     * @throws JSONException if there is not array at the given key.
     */
    public static @NonNull Iterable<JSONObject>
    jsonObjects(@NonNull JSONObject obj, String key) throws JSONException {
        return jsonObjects(obj.getJSONArray(key));
    }

    /**
     * Collect regex matches into an array.
     *
     * @param matcher A regex matcher object.
     * @return The array of matches.
     */
    public static @NonNull
    ArrayList<String> collect(@NonNull Matcher matcher) {
        ArrayList<String> result = new ArrayList<String>(matcher.groupCount());

        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }

    /**
     * Apply regex to text and return the matches collected in a list.
     *
     * @param pattern The regular expression object.
     * @param text The text to search.
     *
     * @return The array of matches.
     */
    public static @NonNull
    ArrayList<String> search(
        @NonNull Pattern pattern,
        @NonNull CharSequence text
    ) {
       return Util.collect(pattern.matcher(text));
    }


    /**
     * Show some text in a TextView if the text is non-empty.
     * Otherwise, hide the TextView.
     */
    public static void textOrHide(@NonNull TextView textView, @Nullable String
    text) {
        if (text == null || text.length() == 0) {
            textView.setVisibility(View.GONE);
            textView.setText("");
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        }
    }

    /**
     * Given any object, test if it is null, and immediately throw
     * a NullPointerException if it is null.
     *
     * This method can be used to make code complain about the source of
     * null references earlier, rather the later.
     *
     * @param possiblyNull Any object.
     * @throws NullPointerException Thrown if the object is null.
     * @return The object provided.
     */
    public static <T> T assumeNotNull(@Nullable T possiblyNull)
    throws NullPointerException {
        if (possiblyNull == null) {
            throw new NullPointerException();
        }

        return possiblyNull;
    }
}
