package com.w0rp.androidutils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/*
 * This class will collect file objects and rotate the files up to a maximum
 * file size. This maximum size will be exceeded temporarily when a new file is
 * added and that file's size pushes the total count over the maximum. As the
 * maximum file size is hit, files will be removed in a FIFO order.
 */
public class FileRotator {
    private long currentSize = 0;
    private long weakMaxSize = 0;
    private LinkedHashMap<String, Long> sizeMap;

    public FileRotator(long weakMaxSize) {
        this.weakMaxSize = weakMaxSize;

        sizeMap = new LinkedHashMap<String, Long>();
    }

    private void rotate() {
        if (currentSize <= weakMaxSize) {
            // We are within the bounds. Stop here.
            return;
        }

        // We are out of bounds. Remove files until we're in bounds again.
        Iterator<Entry<String, Long>> iter = sizeMap.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<String, Long> entry = iter.next();
            currentSize -= entry.getValue();

            try {
                new File(entry.getKey()).delete();
            }
            catch (Exception e) {
            }

            iter.remove();

            if (currentSize <= weakMaxSize) {
                break;
            }
        }
    }

    synchronized public void add(File file) {
        if (file == null) {
            return;
        }

        String name = file.getAbsolutePath();
        long size = file.length();

        // Add the file.
        Long lastSize = sizeMap.put(name, size);

        if (lastSize != null) {
            // Take away the size of the last file added with that name.
            currentSize -= lastSize;
        }

        // Add the filename and increase the size by the new size value.
        sizeMap.put(name, size);
        currentSize += size;

        rotate();
    }

    synchronized public void remove(File file) {
        String name = file.getAbsolutePath();
        Long size = sizeMap.remove(name);

        if (size != null) {
            // Take away the filesize of the item being removed.
            currentSize -= size;
        }
    }

    synchronized public void setMax(int weakMaxSize) {
        this.weakMaxSize = weakMaxSize;
        rotate();
    }
}
