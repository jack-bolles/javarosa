package org.javarosa.core.model.instance;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AbstractTreeElement<T extends AbstractTreeElement> {

    boolean isLeaf();

    boolean isChildable();

    String getInstanceName();

    @Nullable
    T getFirstChild(String name);

    @Nullable
    T getFirstChild(String namespace, String name);

    @Nullable
    T getChild(String name, int multiplicity);

    /**
     *
     * Get all the child nodes of this element, with specific name
     *
     * @param name
     * @return
     */
    List<T> getChildrenWithName(String name);

    boolean hasChildren();

    int getNumChildren();

    T getChildAt(int i);

    boolean isRepeatable();

    boolean isAttribute();

    int getChildMultiplicity(String name);

    /**
     * Visitor pattern acceptance method.
     *
     * @param visitor
     *            The visitor traveling this tree
     */
    void accept(ITreeVisitor visitor);

    /**
     * Returns the number of attributes of this element.
     */
    int getAttributeCount();

    /**
     * get namespace of attribute at 'index' in the list
     *
     * @param index
     * @return String
     */
    String getAttributeNamespace(int index);

    /**
     * get name of attribute at 'index' in the list
     *
     * @param index
     * @return String
     */
    String getAttributeName(int index);

    /**
     * get value of attribute at 'index' in the list
     *
     * @param index
     * @return String
     */
    String getAttributeValue(int index);

    /**
     * Retrieves the TreeElement representing the attribute at
     * the provided namespace and name, or null if none exists.
     *
     * If 'null' is provided for the namespace, it will match the first
     * attribute with the matching name.
     *
     * @return TreeElement
     */
    T getAttribute(String namespace, String name);

    /**
     * get value of attribute with namespace:name' in the list
     *
     * @return String
     */
    String getAttributeValue(String namespace, String name);

    //return the tree reference that corresponds to this tree element
    TreeReference getRef();

    int getDepth();

    String getName();

    int getMult();

    //Support?
    AbstractTreeElement getParent();

    @Nullable
    IAnswerData getValue();

    int getDataType();

    void clearCaches();

    boolean isRelevant();

    String getNamespace();

    /**
     * TODO: Worst method name ever. Don't use this unless you know what's up.
     *
     * @param name
     * @param mult
     * @param predicates possibly list of predicates to be evaluated. predicates will be removed from list if they are
     * able to be evaluated
     * @param evalContext
     * @return
     */
    List<TreeReference> tryBatchChildFetch(String name, int mult, List<XPathExpression> predicates, EvaluationContext evalContext);
}