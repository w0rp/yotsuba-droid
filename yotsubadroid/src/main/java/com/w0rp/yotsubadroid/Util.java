package com.w0rp.yotsubadroid;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Util {
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
}
