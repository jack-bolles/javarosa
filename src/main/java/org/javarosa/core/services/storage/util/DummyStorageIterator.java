/**
 * 
 */
package org.javarosa.core.services.storage.util;

import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.DataUtil;

import java.util.Enumeration;
import java.util.Hashtable;

public class DummyStorageIterator<T extends Persistable> implements IStorageIterator<T> {
    Hashtable<Integer, T> data;
    int count;
    Integer[] keys;


    public DummyStorageIterator(Hashtable<Integer, T> data) {
        this.data = data;
        keys = new Integer[data.size()];
        int i = 0;
        for(Enumeration<Integer> en = data.keys() ;en.hasMoreElements();) {
            keys[i] = en.nextElement();
            ++i;
        }
        count = 0;
    }

    public boolean hasMore() {
        return count < keys.length;
    }

    public int nextID() {
        count++;
        return keys[count - 1];
    }

    public T nextRecord() {
        return data.get(DataUtil.integer(nextID()));
    }

    public int numRecords() {
        return data.size();
    }

    public int peekID() {
        return keys[count];
    }
}
