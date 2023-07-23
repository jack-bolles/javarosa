package org.javarosa.model.xform;

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

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IAnswerDataSerializer;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.IInstanceSerializingVisitor;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.core.services.transport.payload.DataPointerPayload;
import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.core.services.transport.payload.MultiMessagePayload;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A visitor-esque class which walks a FormInstance and constructs an XML document
 * containing its instance.
 * <p>
 * The XML node elements are constructed in a depth-first manner, consistent with
 * standard XML document parsing.
 *
 * @author Clayton Sims
 */
public class XFormSerializingVisitor implements IInstanceSerializingVisitor {

    private Document theXmlDoc;

    private IAnswerDataSerializer serializer;

    private TreeReference rootRef;

    private List<IDataPointer> dataPointers;

    private final boolean respectRelevance;

    public XFormSerializingVisitor() {
        this(true);
    }

    private XFormSerializingVisitor(boolean respectRelevance) {
        this.respectRelevance = respectRelevance;
    }

    private static byte[] getUtfBytes(Document doc) {
        KXmlSerializer serializer = new KXmlSerializer();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            serializer.setOutput(bos, UTF_8.name());
            doc.write(serializer);
            serializer.flush();
            return bos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void init() {
        theXmlDoc = null;
        dataPointers = new ArrayList<>(0);
    }

    public byte[] serializeInstance(FormInstance model, FormDef formDef) {
        //LEGACY: Should remove
        init();
        return serializeInstance(model);
    }

    public byte[] serializeInstance(FormInstance model) {
        return serializeInstance(model, new XPathReference("/"));
    }

    public byte[] serializeInstance(FormInstance model, IDataReference ref) {
        init();
        rootRef = FormInstance.unpackReference(ref);
        if (serializer == null) {
            setAnswerDataSerializer(new XFormAnswerDataSerializer());
        }

        model.accept(this);
        if (theXmlDoc != null) {
            return getUtfBytes(theXmlDoc);
        } else {
            return null;
        }
    }

    public IDataPayload createSerializedPayload(FormInstance model) {
        return createSerializedPayload(model, new XPathReference("/"));
    }

    public IDataPayload createSerializedPayload(FormInstance model, IDataReference ref) {
        init();
        rootRef = FormInstance.unpackReference(ref);
        if (serializer == null) {
            setAnswerDataSerializer(new XFormAnswerDataSerializer());
        }
        model.accept(this);
        if (theXmlDoc != null) {
            //TODO: Did this strip necessary data?
            byte[] form = getUtfBytes(theXmlDoc);
            if (dataPointers.isEmpty()) {
                return new ByteArrayPayload(form, null, IDataPayload.PAYLOAD_TYPE_XML);
            }
            MultiMessagePayload payload = new MultiMessagePayload();
            payload.addPayload(new ByteArrayPayload(form, "xml_submission_file", IDataPayload.PAYLOAD_TYPE_XML));
            for (IDataPointer pointer : dataPointers) {
                payload.addPayload(new DataPointerPayload(pointer));
            }
            return payload;
        } else {
            return null;
        }
    }

    public void visit(FormInstance tree) {
        theXmlDoc = new Document();
        //TreeElement root = tree.getRoot();

        TreeElement root = tree.resolveReference(rootRef);

        //For some reason resolveReference won't ever return the root, so we'll
        //catch that case and just start at the root.
        if (root == null) {
            root = tree.getRoot();
        }

        if (root != null) {
            theXmlDoc.addChild(Node.ELEMENT, serializeNode(root));
        }

        Element top = theXmlDoc.getElement(0);

        String[] prefixes = tree.getNamespacePrefixes();
        for (String prefix : prefixes) {
            top.setPrefix(prefix, tree.getNamespaceURI(prefix));
        }
        if (tree.schema != null) {
            top.setNamespace(tree.schema);
            top.setPrefix("", tree.schema);
        }
    }

    private Element serializeNode(TreeElement instanceNode) {
        Element e = new Element(); //don't set anything on this element yet, as it might get overwritten

        //don't serialize template nodes or non-relevant nodes
        if ((respectRelevance && !instanceNode.isRelevant()) || instanceNode.getMult() == TreeReference.INDEX_TEMPLATE) {
            return null;
        }

        if (instanceNode.getValue() != null) {
            Object serializedAnswer;
            try {
                serializedAnswer = serializer.serializeAnswerData(instanceNode.getValue(), instanceNode.getDataType());
            } catch (RuntimeException ex) {
                throw new RuntimeException("Unable to serialize " + instanceNode.getValue().toString() + ". Exception: " + ex);
            }

            if (serializedAnswer instanceof Element) {
                e = (Element) serializedAnswer;
            } else if (serializedAnswer instanceof String) {
                e = new Element();
                e.addChild(Node.TEXT, serializedAnswer);
            } else {
                throw new RuntimeException("Can't handle serialized output for" + instanceNode.getValue().toString() + ", " + serializedAnswer);
            }

            if (serializer.containsExternalData(instanceNode.getValue())) {
                IDataPointer[] pointer = serializer.retrieveExternalDataPointer(instanceNode.getValue());
                Collections.addAll(dataPointers, pointer);
            }
        } else {
            //make sure all children of the same tag name are written en bloc
            List<String> childNames = new ArrayList<>(instanceNode.getNumChildren());
            for (int i = 0; i < instanceNode.getNumChildren(); i++) {
                String childName = instanceNode.getChildAt(i).getName();
                if (!childNames.contains(childName))
                    childNames.add(childName);
            }

            for (String name : childNames) {
                int mult = instanceNode.getChildMultiplicity(name);
                for (int j = 0; j < mult; j++) {
                    Element child = serializeNode(instanceNode.getChild(name, j));
                    if (child != null) {
                        e.addChild(Node.ELEMENT, child);
                    }
                }
            }
        }

        e.setName(instanceNode.getName());

        // add hard-coded attributes
        for (int i = 0; i < instanceNode.getAttributeCount(); i++) {
            String namespace = instanceNode.getAttributeNamespace(i);
            String name = instanceNode.getAttributeName(i);
            String val = instanceNode.getAttributeValue(i);
            // is it legal for getAttributeValue() to return null? playing it safe for now and assuming yes
            if (val == null) {
                val = "";
            }
            e.setAttribute(namespace, name, val);
        }
        if (instanceNode.getNamespace() != null) {
            e.setNamespace(instanceNode.getNamespace());
        }

        return e;
    }

    public void setAnswerDataSerializer(IAnswerDataSerializer ads) {
        serializer = ads;
    }

    public IInstanceSerializingVisitor newInstance() {
        XFormSerializingVisitor modelSerializer = new XFormSerializingVisitor();
        modelSerializer.setAnswerDataSerializer(serializer);
        return modelSerializer;
    }
}
