/**
 *
 */
package org.javarosa.core.services.storage.util;

import org.javarosa.core.services.storage.EntityFilter;
import org.javarosa.core.services.storage.IMetaData;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.util.DataUtil;
import org.javarosa.core.util.InvalidIndexException;
import org.javarosa.core.util.externalizable.Externalizable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;

public class DummyIndexedStorageUtility<T extends Persistable> implements IStorageUtilityIndexed<T> {

    private final Hashtable<String, Hashtable<Object, ArrayList<Integer>>> meta;

    private final Hashtable<Integer, T> data;

    int curCount;

    public DummyIndexedStorageUtility() {
        meta = new Hashtable<>();
        data = new Hashtable<>();
        curCount = 0;
    }

    @Override
    public List<Integer> getIDsForValue(String fieldName, Object value) {
        if(meta.get(fieldName) == null || meta.get(fieldName).get(value) == null) {
            return new ArrayList<>(0);
        }
        return meta.get(fieldName).get(value);
    }

    public T getRecordForValue(String fieldName, Object value) throws NoSuchElementException, InvalidIndexException {

        if(meta.get(fieldName) == null) {
            throw new NoSuchElementException("No record matching meta index " + fieldName + " with value " + value);
        }

        ArrayList<Integer> matches = meta.get(fieldName).get(value);

        if(matches == null || matches.size() == 0) {
            throw new NoSuchElementException("No record matching meta index " + fieldName + " with value " + value);
        }
        if(matches.size() > 1) {
            throw new InvalidIndexException("Multiple records matching meta index " + fieldName + " with value " + value, fieldName);
        }

        return data.get(matches.get(0));
    }

    public int add(T e) throws StorageFullException {
        data.put(DataUtil.integer(curCount),e);

        //This is not a legit pair of operations;
        curCount++;

        syncMeta();

        return curCount - 1;
    }

    public void close() {
        // TODO Auto-generated method stub

    }

    public void destroy() {
        // TODO Auto-generated method stub

    }

    public boolean exists(int id) {
        return data.containsKey(DataUtil.integer(id));
    }

    public Object getAccessLock() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getNumRecords() {
        return data.size();
    }

    public int getRecordSize(int id) {
        //serialize and test blah blah.
        return 0;
    }

    public int getTotalSize() {
        //serialize and test blah blah.
        return 0;
    }

    public boolean isEmpty() {
        return data.size() > 0;
    }

    public IStorageIterator<T> iterate() {
        //We should really find a way to invalidate old iterators first here
        return new DummyStorageIterator<>(data);
    }

    public T read(int id) {
        return data.get(DataUtil.integer(id));
    }

    public byte[] readBytes(int id) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             DataOutputStream dataOutputStream = new DataOutputStream(stream)) {
            data.get(DataUtil.integer(id)).writeExternal(dataOutputStream);
            return stream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't serialize data to return to readBytes");
        }
    }

    public void remove(int id) {
        data.remove(DataUtil.integer(id));

        syncMeta();
    }

    public void remove(Persistable p) {
        this.read(p.getID());
    }

    public void removeAll() {
        data.clear();

        meta.clear();
    }

    public List<Integer> removeAll(EntityFilter ef) {
       ArrayList<Integer> removed = new ArrayList<>();
        for(Enumeration<Integer> en = data.keys(); en.hasMoreElements() ;) {
            Integer i = en.nextElement();
            switch(ef.preFilter(i,null)) {
            case EntityFilter.PREFILTER_INCLUDE:
                removed.add(i);
                break;
            case EntityFilter.PREFILTER_EXCLUDE:
                continue;
            }
            if(ef.matches(data.get(i))) {
                removed.add(i);
            }
        }
        for(Integer i : removed) {
            data.remove(i);
        }

        syncMeta();

        return removed;
    }

    public void repack() {
        //Unecessary!
    }

    public void repair() {
        //Unecessary!
    }

    public void update(int id, T e) throws StorageFullException {
        data.put(DataUtil.integer(id), e);
        syncMeta();
    }

    public void write(Persistable p) throws StorageFullException {
        if(p.getID() != -1) {
            this.data.put(DataUtil.integer(p.getID()), (T)p);
            syncMeta();
        } else {
            p.setID(curCount);
            this.add((T)p);
        }
    }

    private void syncMeta() {
        meta.clear();
        for(Enumeration<Integer> en = data.keys(); en.hasMoreElements() ; ) {
            Integer i = en.nextElement();
            Externalizable e = data.get(i);

            if( e instanceof IMetaData ) {

                IMetaData m = (IMetaData)e;
                for(String key : m.getMetaDataFields()) {
                    if(!meta.containsKey(key)) {
                        meta.put(key, new Hashtable<>());
                    }
                }
                for(String key : dynamicIndices) {
                    if(!meta.containsKey(key)) {
                        meta.put(key, new Hashtable<>());
                    }
                }
                for(Enumeration<String> keys = meta.keys() ; keys.hasMoreElements();) {
                    String key = keys.nextElement();

                    Object value = m.getMetaData(key);

                    Hashtable<Object,ArrayList<Integer>> records = meta.get(key);

                    if(!records.containsKey(value)) {
                        records.put(value, new ArrayList<>(1));
                    }
                    ArrayList<Integer> indices = records.get(value);
                    if(!indices.contains(i)) {
                        records.get(value).add(i);
                    }
                }
            }
        }
    }


    public void setReadOnly() {
        //TODO: This should have a clear contract.
    }

    ArrayList<String> dynamicIndices = new ArrayList<>(0);

    public void registerIndex(String filterIndex) {
        dynamicIndices.add(filterIndex);
    }
}
