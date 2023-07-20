/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class CacheTable<K> {
    private static final Logger logger = LoggerFactory.getLogger(CacheTable.class);

    int totalAdditions = 0;

    // Object is actually K...
    private Hashtable<Integer, WeakReference<Object>> currentTable;

    private static final Vector<WeakReference<CacheTable<?>>> caches = new Vector<>(1);

    private static final Thread cleaner = new Thread(new Runnable() {
        public void run() {
            Vector<Integer> toRemove = new Vector<>();
            while(true) {
                try {
                    toRemove.removeAllElements();
                    for (int i = 0; i < caches.size(); ++i) {
                        CacheTable<?> cache = caches.elementAt(i).get();
                        if (cache == null) {
                            toRemove.addElement(DataUtil.integer(i));
                        } else {
                            Hashtable<Integer, WeakReference<Object>> table = cache.currentTable;
                            for (Enumeration<Integer> en = table.keys(); en.hasMoreElements();) {
                                Integer key = en.nextElement();

                                synchronized(cache) {
                                    //See whether or not the cached reference has been cleared by the GC
                                    if (table.get(key).get() == null) {
                                            //If so, remove the entry, it's no longer useful.
                                            table.remove(key);
                                    }
                                }
                            }

                            synchronized(cache) {
                                //See if our current size is 25% the size of the largest size we've been
                                //and compact (clone to a new table) if so, since the table maintains the
                                //largest size it has ever been.
                                //TODO: 50 is a super arbitrary upper bound
                                if(cache.totalAdditions > 50 && cache.totalAdditions - cache.currentTable.size() > (cache.currentTable.size() >> 2) ) {
                                    Hashtable<Integer,WeakReference<Object>> newTable = new Hashtable<>(cache.currentTable.size());
                                    for (Enumeration<Integer> en = table.keys(); en.hasMoreElements();) {
                                        Integer key = en.nextElement();
                                        newTable.put(key, cache.currentTable.get(key));
                                    }
                                    cache.currentTable = newTable;
                                    cache.totalAdditions = cache.currentTable.size();
                                }
                            }

                        }
                    }
                    for (int id = toRemove.size() - 1; id >= 0; --id) {
                        caches.removeElementAt(toRemove.elementAt(id));
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        logger.error("Error", e);
                    }
                } catch (Exception e) {
                    logger.error("Error", e);
                }
            }

        }
    });

    private static void registerCache(CacheTable<?> table) {
        caches.addElement(new WeakReference<>(table));
        synchronized(cleaner) {
            if(!cleaner.isAlive()) {
                cleaner.start();
            }
        }
    }

    public CacheTable() {
        super();
        currentTable = new Hashtable<>();
        registerCache(this);
    }

    public K intern(K k) {
        synchronized(this) {
            int hash = k.hashCode();
            K nk = retrieve(hash);
            if(nk == null) {
                register(hash, k);
                return k;
            }
            if(k.equals(nk)) {
                return nk;
            } else {
                //Collision. We should deal with this better for interning (and not manually caching) tables.
            }
            return k;
        }
    }


    public K retrieve(int key) {
        synchronized(this) {
            if(!currentTable.containsKey(DataUtil.integer(key))) { return null; }
            K retVal = (K)currentTable.get(DataUtil.integer(key)).get();
            if(retVal == null) { currentTable.remove(DataUtil.integer(key)); }
            return retVal;
        }
    }

    public void register(int key, K item) {
        synchronized(this) {
            currentTable.put(DataUtil.integer(key), new WeakReference<>(item));
            totalAdditions++;
        }
    }
}
