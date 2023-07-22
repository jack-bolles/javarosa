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

package org.javarosa.core.model.instance;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.utils.IInstanceVisitor;
import org.javarosa.core.services.storage.IMetaData;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;


/**
 * This class represents the xform model instance
 */
public class FormInstance extends DataInstance<TreeElement> implements Persistable, IMetaData {

    public static final String STORAGE_KEY = "FORMDATA";

    /**
     * The date that this model was taken and recorded
     */
    private Date dateSaved;

    public String schema;
    public String formVersion;
    public String uiVersion;

    private HashMap<String, Object> namespaces = new HashMap<>();

    /**
     * The root node of this instance. This is the "instance" node that contains the instance's document root.
     * See <a href="https://getodk.github.io/xforms-spec/#instance">...</a>.
     **/
    private TreeElement root = new TreeElement();

    public FormInstance() {

    }

    public FormInstance(TreeElement root) {
        this(root, null);
    }

    /**
     * Creates a new instance using the provided document root.
     */
    public FormInstance(TreeElement root, String id) {
        super(id);
        setID(-1);
        setFormId(-1);
        setRoot(root);
    }

    public TreeElement getBase() {
        return root;
    }

    /**
     * Get this instance's document root which is the root's first child.
     */
    public TreeElement getRoot() {
        if (root.getNumChildren() == 0)
            throw new RuntimeException("root node has no children");

        return root.getChildAt(0);
    }

    /**
     * Sets the root element of this instance's tree. Builds the root node and attaches the root element to it.
     */
    public void setRoot(TreeElement topLevel) {
        root = new TreeElement();
        if (getName() != null) {
            root.setInstanceName(getName());
        }
        if (topLevel != null) {
            root.addChild(topLevel);
        }
    }

    public TreeReference copyNode(TreeReference from, TreeReference to) throws InvalidReferenceException {
        if (!from.isAbsolute()) {
            throw new InvalidReferenceException("Source reference must be absolute for copying", from);
        }

        TreeElement src = resolveReference(from);
        if (src == null) {
            throw new InvalidReferenceException("Null Source reference while attempting to copy node", from);
        }

        return copyNode(src, to).getRef();
    }

    // for making new repeat instances; 'from' and 'to' must be unambiguous
    // references EXCEPT 'to' may be ambiguous at its final step
    // return true is successfully copied, false otherwise
    public TreeElement copyNode(TreeElement src, TreeReference to) throws InvalidReferenceException {
        if (!to.isAbsolute())
            throw new InvalidReferenceException("Destination reference must be absolute for copying", to);

        // strip out dest node info and get dest parent
        String dstName = to.getNameLast();
        int dstMult = to.getMultLast();
        TreeReference toParent = to.getParentRef();

        TreeElement parent = resolveReference(toParent);
        if (parent == null) {
            throw new InvalidReferenceException("Null parent reference whle attempting to copy", toParent);
        }
        if (!parent.isChildable()) {
            throw new InvalidReferenceException("Invalid Parent Node: cannot accept children.", toParent);
        }

        if (dstMult == TreeReference.INDEX_UNBOUND) {
            dstMult = parent.getChildMultiplicity(dstName);
        } else if (parent.getChild(dstName, dstMult) != null) {
            throw new InvalidReferenceException("Destination already exists!", to);
        }

        TreeElement dest = src.deepCopy(false);
        dest.setName(dstName);
        dest.setMult(dstMult);
        parent.addChild(dest);
        return dest;
    }

    public Date getDateSaved() {
        return dateSaved;
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.model.instance.FormInstanceAdapter#addNamespace(java.lang.String, java.lang.String)
     */
    public void addNamespace(String prefix, String URI) {
        namespaces.put(prefix, URI);
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.model.instance.FormInstanceAdapter#getNamespacePrefixes()
     */
    public String[] getNamespacePrefixes() {
        String[] prefixes = new String[namespaces.size()];
        int i = 0;
        for (String key : namespaces.keySet()) {
            prefixes[i] = key;
            ++i;
        }
        return prefixes;
    }

    /* (non-Javadoc)
     * @see org.javarosa.core.model.instance.FormInstanceAdapter#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix) {
        return (String) namespaces.get(prefix);
    }


    public FormInstance clone() {
        FormInstance cloned = new FormInstance(getRoot().deepCopy(true));

        cloned.setID(getID());
        cloned.setFormId(getFormId());
        cloned.setName(getName());
        cloned.setDateSaved(getDateSaved());
        cloned.schema = schema;
        cloned.formVersion = formVersion;
        cloned.uiVersion = uiVersion;
        cloned.namespaces = new HashMap<>();
        for (String key : namespaces.keySet()) {
            cloned.namespaces.put(key, namespaces.get(key));
        }

        return cloned;
    }

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);
        schema = (String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf);
        dateSaved = (Date) ExtUtil.read(in, new ExtWrapNullable(Date.class), pf);
        namespaces = (HashMap<String, Object>) ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
        setRoot((TreeElement) ExtUtil.read(in, TreeElement.class, pf));
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
        ExtUtil.write(out, new ExtWrapNullable(schema));
        ExtUtil.write(out, new ExtWrapNullable(dateSaved));
        ExtUtil.write(out, new ExtWrapMap(namespaces));

        ExtUtil.write(out, getRoot());
    }


    public void setDateSaved(Date dateSaved) {
        this.dateSaved = dateSaved;
    }

    public void copyItemsetNode(TreeElement copyNode, TreeReference destRef, FormDef f)
            throws InvalidReferenceException {
        TreeElement templateNode = getTemplate(destRef);
        TreeElement newNode = copyNode(templateNode, destRef);
        newNode.populateTemplate(copyNode, f);
    }

    public void accept(IInstanceVisitor visitor) {
        visitor.visit(this);

        if (visitor instanceof ITreeVisitor) {
            root.accept((ITreeVisitor) visitor);
        }

    }


    // determine if nodes are homogeneous, meaning their descendant structure is 'identical' for repeat purposes
    // identical means all children match, and the children's children match, and so on
    // repeatable children are ignored; as they do not have to exist in the same quantity for nodes to be homogeneous
    // however, the child repeatable nodes MUST be verified amongst themselves for homogeneity later
    // this function ignores the names of the two nodes
    public static boolean isHomogeneous(TreeElement a, TreeElement b) {
        if (a.isLeaf() && b.isLeaf()) {
            return true;
        } else if (a.isChildable() && b.isChildable()) {
            // verify that every (non-repeatable) node in a exists in b and vice
            // versa
            for (int k = 0; k < 2; k++) {
                TreeElement n1 = (k == 0 ? a : b);
                TreeElement n2 = (k == 0 ? b : a);

                for (int i = 0; i < n1.getNumChildren(); i++) {
                    TreeElement child1 = n1.getChildAt(i);
                    if (child1.isRepeatable())
                        continue;
                    TreeElement child2 = n2.getChild(child1.getName(), 0);
                    if (child2 == null)
                        return false;
                    if (child2.isRepeatable())
                        throw new RuntimeException("shouldn't happen");
                }
            }

            // compare children
            for (int i = 0; i < a.getNumChildren(); i++) {
                TreeElement childA = a.getChildAt(i);
                if (childA.isRepeatable())
                    continue;
                TreeElement childB = b.getChild(childA.getName(), 0);
                if (!isHomogeneous(childA, childB))
                    return false;
            }

            return true;
        } else {
            return false;
        }
    }

    public void initialize(InstanceInitializationFactory initializer, String instanceId) {
        setInstanceId(instanceId);
        root.setInstanceName(instanceId);
    }

    private static final String META_XMLNS = "XMLNS";
    private static final String META_ID = "instance_id";

    public String[] getMetaDataFields() {
        return new String[]{META_XMLNS, META_ID};
    }

    public HashMap<String, Object> getMetaData() {
        HashMap<String, Object> data = new HashMap<>();
        for (String key : getMetaDataFields()) {
            data.put(key, getMetaData(key));
        }
        return data;
    }

    public Object getMetaData(String fieldName) {
        if (META_XMLNS.equals(fieldName)) {
            return ExtUtil.emptyIfNull(schema);
        } else if (META_ID.equals(fieldName)) {
            return ExtUtil.emptyIfNull(getInstanceId());
        }
        throw new IllegalArgumentException("No metadata field " + fieldName + " in the form instance storage system");
    }
}
