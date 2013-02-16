package com.w0rp.androidutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class Iter {
    public static class NullIterator<T> implements Iterator<T> {
        @Override public boolean hasNext() { return false; }
        @Override public T next() { return null; }
        @Override public void remove() { }
    }

    public static class IteratorIterable<T> implements Iterable<T> {
        private Iterator<T> iterator;

        public IteratorIterable(Iterator<T> iterator) {
            if (iterator == null) {
                this.iterator = new NullIterator<T>();
            } else {
                this.iterator = iterator;
            }
        }

        @Override
        public Iterator<T> iterator() {
            return iterator;
        }
    }

    /**
     * Given an iterator of type T, yield an Iterable of type T.
     *
     * This method will generate null object Iterables when given null
     * iterators.
     */
    public static <T> Iterable<T> iter(Iterator<T> iterator) {
        return new IteratorIterable<T>(iterator);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Iterable<String> stringIter(Iterator iterator) {
        return new IteratorIterable<String>((Iterator<String>) iterator);
    }

    public static Set<String> stringSet(Iterable<Object> iter) {
        Set<String> set = new HashSet<String>();

        for (Object obj : iter) {
            if (obj instanceof String) {
                set.add((String) obj);
            }
        }

        return set;
    }

    public static List<String> stringList(Iterable<Object> iter) {
        List<String> list = new ArrayList<String>();

        for (Object obj : iter) {
            if (obj instanceof String) {
                list.add((String) obj);
            }
        }

        return list;
    }
}
