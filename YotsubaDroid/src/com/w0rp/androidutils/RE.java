package com.w0rp.androidutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.SparseArray;

/**
 * This module provides a wrapper around Java's regular expression
 * functionality. Strings can be used directly as regular expressions. All
 * strings used as regular expression in this module will have their compiled
 * forms cached. So there is no need to pre-compile regular expressions.
 */
public abstract class RE {
    private static SparseArray<Map<String, Pattern>> reCache =
        new SparseArray<Map<String, Pattern>>();

    /**
     * Compile a regular expression pattern, caching the result.
     *
     * @param regex The regular expression to compile.
     * @param flags The flags to set.
     * @return The compiled, cached regular expression pattern.
     */
    private static Pattern compile(String regex, int flags) {
        Map<String, Pattern> patMap = reCache.get(flags);

        if (patMap == null) {
           patMap = new HashMap<String, Pattern>();
           reCache.put(flags, patMap);
        }

        Pattern pattern = patMap.get(regex);

        if (pattern == null) {
            pattern = Pattern.compile(regex, flags);
            patMap.put(regex, pattern);
        }

        return pattern;
    }

    /**
     * Search for a single match of a regular expression pattern.
     * Return the result in an immutable list.
     *
     * @param regex A regular expression pattern to match with.
     * @param input The input to match, which may be null.
     *
     * @return A list containing capture groups, empty is the match fails.
     */
    public static List<String> search(Pattern regex, CharSequence input) {
        if (input == null) {
            return Collections.emptyList();
        }

        Matcher matcher = regex.matcher(input);

        if (!matcher.find()) {
            return Collections.emptyList();
        }

        final int len = matcher.groupCount() + 1;

        List<String> matchList = new ArrayList<String>(len);

        for (int i = 0; i < len; ++i) {
            matchList.add(matcher.group(i));
        }

        return Collections.unmodifiableList(matchList);
    }

    /**
     * Search for a single match of a regular expression pattern.
     * Return the result in an immutable list.
     *
     * @param regex A regular expression pattern to match with.
     * @param input The input to match, which may be null.
     *
     * @return A list containing capture groups, empty is the match fails.
     */
    public static List<String> search(String regex, CharSequence input) {
        return search(compile(regex, 0), input);
    }

    /**
     * Search for a single match of a regular expression pattern.
     * Return the result in an immutable list.
     *
     * @param regex A regular expression pattern to match with.
     * @param input The input to match, which may be null.
     * @param flags Flags to set when compiling the pattern.
     *
     * @return A list containing capture groups, empty is the match fails.
     */
    public static List<String> search(String regex, CharSequence input,
    int flags) {
        return search(compile(regex, flags), input);
    }

    /**
     * @param regex A regular expression pattern to match with.
     * @param input The input to match, which may be null.
     *
     * @return true if the input contains a match.
     */
    public static boolean contains(Pattern regex, CharSequence input) {
        if (input == null) {
            return false;
        }

        return regex.matcher(input).find();
    }

    /**
     * @param regex A regular expression pattern to match with.
     * @param input The input to match, which may be null.
     *
     * @return true if the input contains a match.
     */
    public static boolean contains(String regex, CharSequence input) {
        return contains(compile(regex, 0), input);
    }

    /**
     * @param regex A regular expression pattern to match with.
     * @param input The input to match, which may be null.
     * @param flags Flags to set when compiling the pattern.
     *
     * @return true if the input contains a match.
     */
    public static boolean contains(String regex, CharSequence input,
    int flags) {
        return contains(compile(regex, flags), input);
    }
}
