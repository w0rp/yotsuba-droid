package com.w0rp.androidutils;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.w0rp.androidutils.Iter.NullIterable;

/*
 * This class provides various utility methods for constructing and
 * deconstructing JSON sequences.
 */
public abstract class JSON {
    public static class JSONArrayIterator implements Iterator<Object> {
        private JSONArray arr;
        private int current = 0;

        public JSONArrayIterator(JSONArray arr) {
            this.arr = arr;
        }

        @Override
        public boolean hasNext() {
            return arr != null && current < this.arr.length();
        }

        @Override
        public Object next() {
            return hasNext() ? this.arr.opt(current++) : null;
        }

        @Override
        public void remove() {
            // TODO: Implement this?
        }
    }

    /**
     * @param arr A JSONArray. null will be tolerated.
     * @return An Iterator through the values of the JSONArray.
     */
    public static Iterable<Object> iter(JSONArray arr) {
        return Iter.cast(Object.class, new JSONArrayIterator(arr));
    }

    /**
     * @param arr A JSONArray. null will be tolerated.
     * @return An Iterator through the JSONObject values of the JSONArray.
     */
    public static Iterable<JSONObject> objIter(JSONArray arr) {
        return Iter.cast(JSONObject.class, iter(arr));
    }

    /**
     * @param obj An object to pull a JSONArray from.
     * @param key The key for the JSONArray.
     * @return An Iterator through the values of the JSONArray.
     */
    public static Iterable<Object> iter(JSONObject obj, String key) {
        return iter(obj != null ? obj.optJSONArray(key) : null);
    }

    /**
     * @param obj An object to pull a JSONArray from.
     * @param key The key for the JSONArray.
     * @return An Iterator through the JSONObject values of the JSONArray.
     */
    public static Iterable<JSONObject> objIter(JSONObject obj, String key) {
        return Iter.cast(JSONObject.class, iter(obj, key));
    }

    /**
     * Produce an iterable through a JSONObject's keys.
     *
     * null will be tolerated in all cases.
     *
     * @return An Iterable iterating through an object's keys.
     */
    public static Iterable<String> keys(JSONObject obj) {
        if (obj == null) {
            return new NullIterable<String>();
        }

        return Iter.cast(String.class, obj.keys());
    }
}
