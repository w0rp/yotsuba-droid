package com.w0rp.androidutils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.IntentFilter;

public abstract class Util {
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
    
    /*
     * Given an iterator of type T, yield an Iterable of type T.
     * 
     * This method will generate null object Iterables when given null iterators.
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
    
    public static String streamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        
        return sb.toString();
    }
    
    public static void stream(InputStream in, OutputStream out) 
    throws IOException {
        byte[] buffer = new byte[4096];
        
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }
    
    public static String def(String in) {
        if (in == null) {
            return "";
        }
        
        return in;
    }
    
    public static boolean empty(String in) {
        return in == null || in.length() == 0;
    }
    
    public static String traceString(Throwable tr) {
        StringWriter sw = new StringWriter();
        tr.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
    public static IntentFilter filter (Object obj) {
        return new IntentFilter(obj.getClass().getName());
    }
    
    /*
     * Try to close an object, ignoring exceptions.
     * 
     * This method will tolerate null references.
     */
    public static void close(Closeable obj) {
        if (obj == null) {
            return;
        }
        
        try {
            obj.close();
        } catch (Exception e) { }
    }
    
    public static String pathClean(String path) {
        return path.replace('/', '_');
    }
    
    public static String join(String sep, String[] strList) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < strList.length; i++) {
            if (strList[i] != null) {
                sb.append(strList[i]);
            }
            
            if (sep != null && sep.length() > 0 &&  i < strList.length - 1) {
                sb.append(sep);
            }
        }
        
        return sb.toString();
    }
    
    public static String join(String sep, List<String> strList) {
        return join(sep, strList.toArray(new String[strList.size()]));
    }
    
    public static ThreadPoolExecutor pool
    (int maxSize, int wait, TimeUnit unit) {
        return new ThreadPoolExecutor(maxSize, maxSize, wait, unit,
            new LinkedBlockingDeque<Runnable>());
    }
}