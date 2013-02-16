package com.w0rp.androidutils;

import java.util.Iterator;

public abstract class Iter {
    public static class NullIterator<T> implements Iterator<T> {
        @Override public boolean hasNext() { return false; }
        @Override public T next() { return null; }
        @Override public void remove() { }
    }

    public static class NullIterable<T> implements Iterable<T> {
        @Override
        public Iterator<T> iterator() {
            return new NullIterator<T>();
        }
    }

    private static class CastIterator<T> implements Iterator<T> {
        private Class<T> cls;
        private Iterator<?> inIterator;
        private T current;

        public CastIterator(Class<T> cls, Iterator<?> in) {
            assert(cls != null);

            this.cls = cls;
            inIterator = in;
        }

        @Override
        public boolean hasNext() {
            if (current != null) {
                // Avoid looking for the next value when we're waiting
                // for next to be called.
                return true;
            }

            if (inIterator == null) {
                return false;
            }

            // Search for the next valid instance.
            while (inIterator.hasNext()) {
                Object obj = (Object) inIterator.next();

                if (cls.isInstance(obj)) {
                    current = cls.cast(obj);
                    return true;
                }
            }

            return false;
        }

        @Override
        public T next() {
            if (hasNext() && current != null) {
                // First dispose of our reference.
                T next = current;
                current = null;
                // Then return it.
                return next;
            }

            return null;
        }

        @Override
        public void remove() {
            // Delegate remove to the original iterator.
            if (inIterator != null) {
                inIterator.remove();
            }
        }
    }

    private static class CastIterable<T> implements Iterable<T> {
        private Class<T> cls;
        private Iterable<?> inIterable;

        public CastIterable(Class<T> cls, Iterable<?> in) {
            assert(cls != null);

            this.cls = cls;
            inIterable = in;
        }

        @Override
        public Iterator<T> iterator() {
            if (inIterable == null) {
                return new CastIterator<T>(cls, null);
            }

            return new CastIterator<T>(cls, inIterable.iterator());
        }
    }

    /**
     * Given an Iterator of an type, this class will create an
     * iterable returning an iterator running through only valid instances
     * of type T.
     */
    private static class IteratorIterable<T> implements Iterable<T> {
        private Class<T> cls;
        private Iterator<?> iterator;

        public IteratorIterable(Class<T> cls, Iterator<?> iterator) {
            assert(cls != null);

            this.cls = cls;
            this.iterator = iterator;
        }

        @Override
        public Iterator<T> iterator() {
            return new CastIterator<T>(cls, iterator);
        }
    }

    /**
     * Given an Iterable, return an Iterable iterating through the values
     * in the original Iterable, only where the objects are valid instances
     * of type T.
     *
     * null will be tolerated in all cases.
     *
     * @param cls The class to cast the obejcts with.
     * @param in The original Iterable
     * @return A new Iterable yielding only valid instances of type T.
     */
    public static <T> Iterable<T> cast(Class<T> cls, Iterable<?> in) {
        return new CastIterable<T>(cls, in);
    }

    /**
     * Given an iterator of any type, yield an Iterable iterating through
     * only objects which are valid instances of type T.
     *
     * null will be tolerated in all cases.
     */
    public static <T> Iterable<T> iter(Class<T> cls, Iterator<?> iterator) {
        return new IteratorIterable<T>(cls, iterator);
    }
}
