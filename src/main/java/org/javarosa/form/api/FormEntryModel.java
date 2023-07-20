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

package org.javarosa.form.api;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.Extras;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.javarosa.core.model.instance.TreeReference.INDEX_REPEAT_JUNCTURE;
import static org.javarosa.form.api.FormEntryController.EVENT_BEGINNING_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_END_OF_FORM;
import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_PROMPT_NEW_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT_JUNCTURE;

/**
 * The data model used during form entry. Represents the current state of the
 * form and provides access to the objects required by the view and the
 * controller.
 */
public class FormEntryModel {
    private final FormDef form;
    private FormIndex currentFormIndex;

    /**
     * One of "REPEAT_STRUCTURE_" in this class's static types,
     * represents what abstract structure repeat events should
     * be broadcast as.
     */
    private final int repeatStructure;

    /**
     * Repeats should be a prompted linear set of questions, either
     * with a fixed set of repetitions, or a prompt for creating a
     * new one.
     */
    public static final int REPEAT_STRUCTURE_LINEAR = 1;

    /**
     * Repeats should be a custom juncture point with centralized
     * "Create/Remove/Interact" hub.
     */
    public static final int REPEAT_STRUCTURE_NON_LINEAR = 2;

    private final Extras<Object> extras = new Extras<>();

    public FormEntryModel(FormDef form) {
        this(form, REPEAT_STRUCTURE_LINEAR);
    }

    /**
     * Creates a new entry model for the form with the appropriate
     * repeat structure
     *
     * @param form
     * @param repeatStructure The structure of repeats (the repeat signals which should
     *                        be sent during form entry)
     * @throws IllegalArgumentException If repeatStructure is not valid
     */
    public FormEntryModel(FormDef form, int repeatStructure) {
        this.form = form;
        if (REPEAT_STRUCTURE_LINEAR != repeatStructure && REPEAT_STRUCTURE_NON_LINEAR != repeatStructure) {
            throw new IllegalArgumentException(repeatStructure + ": does not correspond to a valid repeat structure");
        }
        //We need to see if there are any guessed repeat counts in the form, which prevents
        //us from being able to use the new repeat style
        //Unfortunately this is probably (A) slow and (B) might overflow the stack. It's not the only
        //recursive walk of the form, though, so (B) isn't really relevant
        if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure && containsRepeatGuesses(form)) {
            repeatStructure = REPEAT_STRUCTURE_LINEAR;
        }
        this.repeatStructure = repeatStructure;
        currentFormIndex = FormIndex.createBeginningOfFormIndex();
    }

    /**
     * Given a FormIndex, returns the event this FormIndex represents.
     *
     * @see FormEntryController
     */
    public int getEvent(FormIndex index) {
        if (index.isBeginningOfFormIndex()) {
            return EVENT_BEGINNING_OF_FORM;
        } else if (index.isEndOfFormIndex()) {
            return EVENT_END_OF_FORM;
        }

        IFormElement element = form.getChild(index);
        if (element instanceof GroupDef) {
            if (((GroupDef) element).getRepeat()) {
                if (REPEAT_STRUCTURE_NON_LINEAR != repeatStructure
                        && null == form.getMainInstance().resolveReference(form.getChildInstanceRef(index))) {
                    return EVENT_PROMPT_NEW_REPEAT;
                } else if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure && INDEX_REPEAT_JUNCTURE == index.getElementMultiplicity()) {
                    return EVENT_REPEAT_JUNCTURE;
                } else {
                    return EVENT_REPEAT;
                }
            } else {
                return EVENT_GROUP;
            }
        } else {
            return EVENT_QUESTION;
        }
    }

    /**
     * @param index
     * @return
     */
    protected TreeElement getTreeElement(FormIndex index) {
        return form.getMainInstance().resolveReference(index.getReference());
    }


    /**
     * @return the event for the current FormIndex
     * @see FormEntryController
     */
    public int getEvent() {
        return getEvent(currentFormIndex);
    }


    /**
     * @return Form title
     */
    public String getFormTitle() {
        return form.getTitle();
    }


    /**
     * @param index
     * @return Returns the FormEntryPrompt for the specified FormIndex if the
     * index represents a question.
     */
    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
        if (form.getChild(index) instanceof QuestionDef) {
            return new FormEntryPrompt(form, index);
        } else {
            throw new RuntimeException(
                "Invalid query for Question prompt. Non-Question object at the form index");
        }
    }


    /**
     * @return Returns the FormEntryPrompt for the current FormIndex if the
     * index represents a question.
     */
    public FormEntryPrompt getQuestionPrompt() {
        return getQuestionPrompt(currentFormIndex);
    }


    /**
     * When you have a non-question event, a CaptionPrompt will have all the
     * information needed to display to the user.
     *
     * @param index
     * @return Returns the FormEntryCaption for the given FormIndex if is not a
     * question.
     */
    public FormEntryCaption getCaptionPrompt(FormIndex index) {
        return new FormEntryCaption(form, index);
    }


    /**
     * When you have a non-question event, a CaptionPrompt will have all the
     * information needed to display to the user.
     *
     * @return Returns the FormEntryCaption for the current FormIndex if is not
     * a question.
     */
    public FormEntryCaption getCaptionPrompt() {
        return getCaptionPrompt(currentFormIndex);
    }


    public FormIndex getFormIndex() {
        return currentFormIndex;
    }

    protected void setLanguage(String language) {
        if (null != form.getLocalizer()) {
            form.getLocalizer().setLocale(language);
        }
    }

    public String getLanguage() {
        return form.getLocalizer().getLocale();
    }

    public void setQuestionIndex(FormIndex index) {
        if (!currentFormIndex.equals(index)) {
            // See if a hint exists that says we should have a model for this
            // already
            createModelIfNecessary(index);
            currentFormIndex = index;
        }
    }

    public FormDef getForm() {
        return form;
    }


    /**
     * Returns a hierarchical list of FormEntryCaption objects for the given
     * FormIndex
     *
     * @param index
     * @return list of FormEntryCaptions in hierarchical order
     */
    public FormEntryCaption[] getCaptionHierarchy(FormIndex index) {
        List<FormEntryCaption> captions = new ArrayList<>();
        FormIndex remaining = index;
        while (null != remaining) {
            remaining = remaining.getNextLevel();
            FormIndex localIndex = index.diff(remaining);
            IFormElement element = form.getChild(localIndex);
            if (null != element) {
                FormEntryCaption caption = null;
                if (element instanceof GroupDef)
                    caption = new FormEntryCaption(getForm(), localIndex);
                else if (element instanceof QuestionDef)
                    caption = new FormEntryPrompt(getForm(), localIndex);

                if (null != caption) {
                    captions.add(caption);
                }
            }
        }
        FormEntryCaption[] captionArray = new FormEntryCaption[captions.size()];
        return captions.toArray(captionArray);
    }

    /**
     * Returns a hierarchical list of FormEntryCaption objects for the current
     * FormIndex
     *
     * @return array of FormEntryCaptions in hierarchical order
     */
    public FormEntryCaption[] getCaptionHierarchy() {
        return getCaptionHierarchy(currentFormIndex);
    }


    public boolean isIndexReadonly(FormIndex index) {
        if (index.isBeginningOfFormIndex() || index.isEndOfFormIndex())
            return true;

        TreeReference ref = form.getChildInstanceRef(index);
        boolean isAskNewRepeat = (EVENT_PROMPT_NEW_REPEAT == getEvent(index) ||
                EVENT_REPEAT_JUNCTURE == getEvent(index));

        if (isAskNewRepeat) {
            return false;
        } else {
            TreeElement node = form.getMainInstance().resolveReference(ref);
            return !node.isEnabled();
        }
    }

    /**
     * Determine if the current FormIndex is relevant. Only relevant indexes
     * should be returned when filling out a form.
     *
     * @param index
     * @return true if current element at FormIndex is relevant
     */
    public boolean isIndexRelevant(FormIndex index) {
        TreeReference ref = form.getChildInstanceRef(index);
        boolean isAskNewRepeat = (EVENT_PROMPT_NEW_REPEAT == getEvent(index));
        boolean isRepeatJuncture = (EVENT_REPEAT_JUNCTURE == getEvent(index));

        if (isAskNewRepeat) {
            if (!form.canCreateRepeat(ref, index)) {
                return false;
            }

            return form.isRepeatRelevant(ref);
        } else if (isRepeatJuncture) {
            //repeat junctures are still relevant if no new repeat can be created; that option
            //is simply missing from the menu
            return form.isRepeatRelevant(ref);
        } else {
            TreeElement node = form.getMainInstance().resolveReference(ref);
            return null != node && node.isRelevant();
        }
    }


    /**
     * Determine if the current FormIndex is relevant. Only relevant indexes
     * should be returned when filling out a form.
     *
     * @return true if current element at FormIndex is relevant
     */
    public boolean isIndexRelevant() {
        return isIndexRelevant(currentFormIndex);
    }


    /**
     * For the current index: Checks whether the index represents a node which
     * should exist given a non-interactive repeat, along with a count for that
     * repeat which is beneath the dynamic level specified.
     * <p>
     * If this index does represent such a node, the new model for the repeat is
     * created behind the scenes and the index for the initial question is
     * returned.
     * <p>
     * Note: This method will not prevent the addition of new repeat elements in
     * the interface, it will merely use the xforms repeat hint to create new
     * nodes that are assumed to exist
     *
     * @param index The index to be evaluated as to whether the underlying model is
     *              hinted to exist
     */
    private void createModelIfNecessary(FormIndex index) {
        if (index.isInForm()) {
            IFormElement e = getForm().getChild(index);
            if (e instanceof GroupDef) {
                GroupDef g = (GroupDef) e;
                if (g.getRepeat() && null != g.getCountReference()) {
                    // Lu Gram: repeat count XPath needs to be contextualized for nested repeat groups
                    TreeReference countRef = FormInstance.unpackReference(g.getCountReference());
                    TreeReference contextualized = countRef.contextualize(index.getReference());
                    IAnswerData count = getForm().getMainInstance().resolveReference(contextualized).getValue();
                    if (null != count) {
                        long fullcount = (Integer) count.getValue();
                        TreeReference ref = getForm().getChildInstanceRef(index);
                        TreeElement element = getForm().getMainInstance().resolveReference(ref);
                        if (null == element) {
                            if (index.getTerminal().getInstanceIndex() < fullcount) {
                                try {
                                    getForm().createNewRepeat(index);
                                } catch (InvalidReferenceException ire) {
                                    throw new RuntimeException("Invalid Reference while creating new repeat!" + ire.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public boolean isIndexCompoundContainer() {
        return isIndexCompoundContainer(getFormIndex());
    }

    public boolean isIndexCompoundContainer(FormIndex index) {
        FormEntryCaption caption = getCaptionPrompt(index);
        return EVENT_GROUP == getEvent(index) &&
                null != caption.getAppearanceHint() &&
            "full".equals(caption.getAppearanceHint().toLowerCase(Locale.ENGLISH));
    }

    public boolean isIndexCompoundElement() {
        return isIndexCompoundElement(getFormIndex());
    }

    public boolean isIndexCompoundElement(FormIndex index) {
        //Can't be a subquestion if it's not even a question!
        if (EVENT_QUESTION != getEvent(index)) {
            return false;
        }

        //get the set of nested groups that this question is in.
        FormEntryCaption[] captions = getCaptionHierarchy(index);
        for (FormEntryCaption caption : captions) {

            //If one of this question's parents is a group, this question is inside of it.
            if (isIndexCompoundContainer(caption.getIndex())) {
                return true;
            }
        }
        return false;
    }

    public FormIndex[] getCompoundIndices() {
        return getCompoundIndices(getFormIndex());
    }

    public FormIndex[] getCompoundIndices(FormIndex container) {
        //ArrayLists are a no-go for J2ME
        List<FormIndex> indices = new ArrayList<>();
        FormIndex walker = incrementIndex(container);
        while (FormIndex.isSubElement(container, walker)) {
            if (isIndexRelevant(walker)) {
                indices.add(walker);
            }
            walker = incrementIndex(walker);
        }
        FormIndex[] array = new FormIndex[indices.size()];
        for (int i = 0; i < indices.size(); ++i) {
            array[i] = indices.get(i);
        }
        return array;
    }


    public FormIndex incrementIndex(FormIndex index) {
        return incrementIndex(index, true);
    }

    public FormIndex incrementIndex(FormIndex index, boolean descend) {
        List<Integer> indexes = new ArrayList<>();
        List<Integer> multiplicities = new ArrayList<>();
        List<IFormElement> elements = new ArrayList<>();

        if (index.isEndOfFormIndex()) {
            return index;
        } else if (index.isBeginningOfFormIndex()) {
            if (null == form.getChildren() || form.getChildren().isEmpty()) {
                return FormIndex.createEndOfFormIndex();
            }
        } else {
            form.collapseIndex(index, indexes, multiplicities, elements);
        }

        incrementHelper(indexes, multiplicities, elements, descend);

        if (indexes.isEmpty()) {
            return FormIndex.createEndOfFormIndex();
        } else {
            return form.buildIndex(indexes, multiplicities, elements);
        }
    }

    private void incrementHelper(List<Integer> indexes, List<Integer> multiplicities, List<IFormElement> elements, boolean descend) {
        int i = indexes.size() - 1;
        boolean exitRepeat = false; //if exiting a repetition? (i.e., go to next repetition instead of one level up)

        if (i == -1 || elements.get(i) instanceof GroupDef) {
            // current index is group or repeat or the top-level form

            if (i >= 0) {
                // find out whether we're on a repeat, and if so, whether the
                // specified instance actually exists
                GroupDef group = (GroupDef) elements.get(i);
                if (group.getRepeat()) {
                    if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure) {
                        if (INDEX_REPEAT_JUNCTURE == multiplicities.get(multiplicities.size() - 1)) {
                            descend = false;
                            exitRepeat = true;
                        }
                    } else {
                        if (null == form.getMainInstance().resolveReference(form.getChildInstanceRef(elements, multiplicities))) {
                            descend = false; // repeat instance does not exist; do not descend into it
                            exitRepeat = true;
                        }
                    }
                }
            }

            if (descend) {
                IFormElement ife = (i == -1) ? null : elements.get(i);
                if ((-1 == i)
                        || ((null != ife)
                        && null != ife.getChildren()
                        && !ife.getChildren().isEmpty())) {
                    indexes.add(0);
                    multiplicities.add(0);
                    elements.add((i == -1 ? form : elements.get(i)).getChild(0));

                    if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure) {
                        if (elements.get(elements.size() - 1) instanceof GroupDef && ((GroupDef) elements.get(elements.size() - 1)).getRepeat()) {
                            multiplicities.set(multiplicities.size() - 1, INDEX_REPEAT_JUNCTURE);
                        }
                    }
                    return;
                }
            }
        }

        while (i >= 0) {
            // if on repeat, increment to next repeat EXCEPT when we're on a
            // repeat instance that does not exist and was not created
            // (repeat-not-existing can only happen at lowest level; exitRepeat
            // will be true)
            if (!exitRepeat && elements.get(i) instanceof GroupDef && ((GroupDef) elements.get(i)).getRepeat()) {
                if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure) {
                    multiplicities.set(i, INDEX_REPEAT_JUNCTURE);
                } else {
                    multiplicities.set(i, multiplicities.get(i) + 1);
                }
                return;
            }

            IFormElement parent = (i == 0 ? form : elements.get(i - 1));
            int curIndex = indexes.get(i);

            // increment to the next element on the current level
            if (curIndex + 1 >= parent.getChildren().size()) {
                // at the end of the current level; move up one level and start
                // over
                indexes.remove(i);
                multiplicities.remove(i);
                elements.remove(i);
                i--;
                exitRepeat = false;
            } else {
                indexes.set(i, curIndex + 1);
                multiplicities.set(i, 0);
                elements.set(i, parent.getChild(curIndex + 1));

                if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure) {
                    if (elements.get(elements.size() - 1) instanceof GroupDef && ((GroupDef) elements.get(elements.size() - 1)).getRepeat()) {
                        multiplicities.set(multiplicities.size() - 1, INDEX_REPEAT_JUNCTURE);
                    }
                }
                return;
            }
        }
    }

    public FormIndex decrementIndex(FormIndex index) {
        List<Integer> indexes = new ArrayList<>();
        List<Integer> multiplicities = new ArrayList<>();
        List<IFormElement> elements = new ArrayList<>();

        if (index.isBeginningOfFormIndex()) {
            return index;
        } else if (index.isEndOfFormIndex()) {
            if (null == form.getChildren() || form.getChildren().isEmpty()) {
                return FormIndex.createBeginningOfFormIndex();
            }
        } else {
            form.collapseIndex(index, indexes, multiplicities, elements);
        }

        decrementHelper(indexes, multiplicities, elements);

        if (indexes.isEmpty()) {
            return FormIndex.createBeginningOfFormIndex();
        } else {
            return form.buildIndex(indexes, multiplicities, elements);
        }
    }

    public Extras<Object> getExtras() {
        return extras;
    }

    private void decrementHelper(List<Integer> indexes, List<Integer> multiplicities, List<IFormElement> elements) {
        int i = indexes.size() - 1;

        if (i != -1) {
            int curIndex = indexes.get(i);
            int curMult = multiplicities.get(i);

            if (REPEAT_STRUCTURE_NON_LINEAR == repeatStructure
                    && elements.get(elements.size() - 1) instanceof GroupDef && ((GroupDef) elements.get(elements.size() - 1)).getRepeat()
                    && INDEX_REPEAT_JUNCTURE != multiplicities.get(multiplicities.size() - 1)) {
                multiplicities.set(i, INDEX_REPEAT_JUNCTURE);
                return;
            } else if (REPEAT_STRUCTURE_NON_LINEAR != repeatStructure && curMult > 0) {
                multiplicities.set(i, curMult - 1);
            } else if (curIndex > 0) {
                // set node to previous element
                indexes.set(i, curIndex - 1);
                multiplicities.set(i, 0);
                elements.set(i, (0 == i ? form : elements.get(i - 1)).getChild(curIndex - 1));

                if (setRepeatNextMultiplicity(elements, multiplicities))
                    return;
            } else {
                // at absolute beginning of current level; index to parent
                indexes.remove(i);
                multiplicities.remove(i);
                elements.remove(i);
                return;
            }
        }

        IFormElement element = (i < 0 ? form : elements.get(i));
        while (!(element instanceof QuestionDef)) {
            if (null == element.getChildren() || element.getChildren().isEmpty()) {
                //if there are no children we just return the current index (the group itself)
                return;
            }
            int subIndex = element.getChildren().size() - 1;
            element = element.getChild(subIndex);

            indexes.add(subIndex);
            multiplicities.add(0);
            elements.add(element);

            if (setRepeatNextMultiplicity(elements, multiplicities))
                return;
        }
    }

    private boolean setRepeatNextMultiplicity(List<IFormElement> elements, List<Integer> multiplicities) {
        // find out if node is repeatable
        TreeReference nodeRef = form.getChildInstanceRef(elements, multiplicities);
        TreeElement node = form.getMainInstance().resolveReference(nodeRef);
        if (null != node && !node.isRepeatable()) {
            return false;
        }
        // node == null if there are no instances of the repeat
        IFormElement lastElement = elements.get(elements.size() - 1);
        if (lastElement instanceof GroupDef
                && !((GroupDef) lastElement).getRepeat() ) {
            return false; // It's a regular group inside a repeatable group. This case takes place when the nested group doesn't have the ref attribute.
        }

        int mult = 0; // no repeats; next is 0
        if (null != node) {
            String name = node.getName();
            TreeElement parentNode = form.getMainInstance().resolveReference(nodeRef.getParentRef());
            mult = parentNode.getChildMultiplicity(name);
        }
        multiplicities.set(multiplicities.size() - 1, REPEAT_STRUCTURE_NON_LINEAR == repeatStructure ? INDEX_REPEAT_JUNCTURE : mult);
        return true;
    }

    /**
     * This method does a recursive check of whether there are any repeat guesses
     * in the element or its subtree. This is a necessary step when initializing
     * the model to be able to identify whether new repeats can be used.
     *
     * @param parent The form element to begin checking
     * @return true if the element or any of its descendants is a repeat
     * which has a count guess, false otherwise.
     */
    private boolean containsRepeatGuesses(IFormElement parent) {
        if (parent instanceof GroupDef) {
            GroupDef g = (GroupDef) parent;
            if (g.getRepeat()
                    && null != g.getCountReference()) {
                return true;
            }
        }

        List<IFormElement> children = parent.getChildren();
        if (null == children) {
            return false;
        }
        for (IFormElement child : children) {
            if (containsRepeatGuesses(child)) {
                return true;
            }
        }
        return false;
    }
}
