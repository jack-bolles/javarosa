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

package org.javarosa.core.model.util.restorable;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.services.storage.Persistable;

import java.util.Date;

public class RestoreUtils {
    public static final String RECORD_ID_TAG = "rec-id";

    public static IXFormyFactory xfFact;

    public static TreeReference ref(String refStr) {
        return xfFact.ref(refStr);
    }

    public static TreeReference topRef(FormInstance dm) {
        return ref("/" + dm.getRoot().getName());
    }

    public static TreeReference childRef(String childPath, TreeReference parentRef) {
        return ref(childPath).parent(parentRef);
    }

    public static int getDataType(Object o) {
        int dataType = -1;
        if (o instanceof String) {
            dataType = Constants.DATATYPE_TEXT;
        } else if (o instanceof Integer) {
            dataType = Constants.DATATYPE_INTEGER;
        } else if (o instanceof Long) {
            dataType = Constants.DATATYPE_LONG;
        } else if (o instanceof Float || o instanceof Double) {
            dataType = Constants.DATATYPE_DECIMAL;
        } else if (o instanceof Date) {
            dataType = Constants.DATATYPE_DATE;
        } else if (o instanceof Boolean) {
            dataType = Constants.DATATYPE_BOOLEAN; //booleans are serialized as a literal 't'/'f'
        } else if (o instanceof MultipleItemsData) {
            dataType = Constants.DATATYPE_MULTIPLE_ITEMS;
        }
        return dataType;
    }

    //used for incoming data
    public static int getDataType(Class c) {
        int dataType;
        if (c == String.class) {
            dataType = Constants.DATATYPE_TEXT;
        } else if (c == Integer.class) {
            dataType = Constants.DATATYPE_INTEGER;
        } else if (c == Long.class) {
            dataType = Constants.DATATYPE_LONG;
        } else if (c == Float.class || c == Double.class) {
            dataType = Constants.DATATYPE_DECIMAL;
        } else if (c == Date.class) {
            dataType = Constants.DATATYPE_DATE;
            //Clayton Sims - Jun 16, 2009 - How are we handling Date v. Time v. DateTime?
        } else if (c == Boolean.class) {
            dataType = Constants.DATATYPE_TEXT; //booleans are serialized as a literal 't'/'f'
        } else {
            throw new RuntimeException("Can't handle data type " + c.getName());
        }

        return dataType;
    }

    public static Object getValue(String xpath, FormInstance tree) {
        return getValue(xpath, topRef(tree), tree);
    }

    public static Object getValue(String xpath, TreeReference context, FormInstance tree) {
        TreeElement node = tree.resolveReference(ref(xpath).contextualize(context));
        if (node == null) {
            throw new RuntimeException("Could not find node [" + xpath + "] when parsing saved instance!");
        }

        if (node.isRelevant()) {
            IAnswerData val = node.getValue();
            return (val == null ? null : val.getValue());
        } else {
            return null;
        }
    }

    public static void applyDataType(FormInstance dm, String path, TreeReference parent, Class type) {
        applyDataType(dm, path, parent, getDataType(type));
    }

    public static void applyDataType(FormInstance dm, String path, TreeReference parent, int dataType) {
        TreeReference ref = childRef(path, parent);
        new EvaluationContext(dm)
                .expandReference(ref)
                .stream()
                .map(dm::resolveReference)
                .forEach(e -> e.setDataType(dataType));
    }

    public static void templateData(Restorable r, FormInstance dm, TreeReference parent) {
        if (parent == null) {
            parent = topRef(dm);
            applyDataType(dm, "timestamp", parent, Date.class);
        }

        if (r instanceof Persistable) {
            applyDataType(dm, RECORD_ID_TAG, parent, Integer.class);
        }

        r.templateData(dm, parent);
    }

    public static FormInstance receive(byte[] payload, Class restorableType) {
        return xfFact.parseRestore(payload, restorableType);
    }
}
