package com.w0rp.androidutils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * This namespace defines utility functions for coercion of types and values.
 *
 * def() coerces a default value for a nullable type.
 * len() coerces a length for a nullable type. (0 for null)
 * empty(x) returns len(x) == 0;
 */
public abstract class Coerce {
    public static <T> Collection<T> def(Collection<T> in) {
        if (in == null) {
            return Collections.emptyList();
        }

        return in;
    }

    public static <T> List<T> def(List<T> in) {
        if (in == null) {
            return Collections.emptyList();
        }

        return in;
    }

    public static <T> Set<T> def(Set<T> in) {
        if (in == null) {
            return Collections.emptySet();
        }

        return in;
    }

    public static <K, V> Map<K, V> def(Map<K, V> in) {
        if (in == null) {
            return Collections.emptyMap();
        }

        return in;
    }

    public static String def(String in) {
        return in == null ? "" : in;
    }

    public static char def (Character in) {
        return in == null ? '\0' : in;
    }

    public static double def(Double in) {
        return in == null ? 0d : in;
    }

    public static float def(Float in) {
        return in == null ? 0f : in;
    }

    public static long def(Long in) {
        return in == null ? 0L : in;
    }

    public static int def(Integer in) {
        return in == null ? 0 : in;
    }

    public static short def (Short in) {
        return in == null ? 0 : in;
    }

    public static byte def (Byte in) {
        return in == null ? 0 : in;
    }

    public static boolean def(Boolean in) {
        return in == null ? false : in;
    }

    public static int len(String in) {
        return in == null ? 0 : in.length();
    }

    public static <T> int len(Collection<T> in) {
        return in == null ? 0 : in.size();
    }

    public static <T> int len(Set<T> in) {
        return in == null ? 0 : in.size();
    }

    public static <K, V> int len(Map<K, V> in) {
        return in == null ? 0 : in.size();
    }

    public static boolean empty(String in) {
        return len(in) == 0;
    }

    public static <T> boolean empty(Collection<T> in) {
        return len(in) == 0;
    }

    public static <T> boolean empty(Set<T> in) {
        return len(in) == 0;
    }

    public static <K, V> boolean empty(Map<K, V> in) {
        return len(in) == 0;
    }
}
