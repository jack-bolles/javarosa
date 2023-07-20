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
import org.javarosa.core.model.instance.InvalidReferenceException;
import org.javarosa.core.model.instance.TreeElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to navigate through an xform and appropriately manipulate
 * the FormEntryModel's state.
 */
public class FormEntryController {
    private static final Logger logger = LoggerFactory.getLogger(FormEntryController.class);

    public static final int ANSWER_OK = 0;
    public static final int ANSWER_REQUIRED_BUT_EMPTY = 1;
    public static final int ANSWER_CONSTRAINT_VIOLATED = 2;

    public static final int EVENT_BEGINNING_OF_FORM = 0;
    public static final int EVENT_END_OF_FORM = 1;
    public static final int EVENT_PROMPT_NEW_REPEAT = 2;
    public static final int EVENT_QUESTION = 4;
    public static final int EVENT_GROUP = 8;
    public static final int EVENT_REPEAT = 16;
    public static final int EVENT_REPEAT_JUNCTURE = 32;

    FormEntryModel model;

    private final List<FormEntryFinalizationProcessor> formEntryFinalizationProcessors = new ArrayList<>();

    public FormEntryController(FormEntryModel model) {
        this.model = model;
    }

    public FormEntryModel getModel() {
        return model;
    }

    /**
     * Attempts to save the answer at the given {@link FormIndex} into the instance
     * and returns one of three possible {@code int} attempt result codes:
     * <ul>
     * <li>{@link #ANSWER_OK}
     * <li>{@link #ANSWER_REQUIRED_BUT_EMPTY}
     * <li>{@link #ANSWER_CONSTRAINT_VIOLATED}
     * </ul>
     * <p>
     * Side effects: When it returns {@link #ANSWER_OK}, it mutates
     * the {@link TreeElement} corresponding to the given {@link FormIndex} by
     * setting its value to the given {@link IAnswerData} or by copying an
     * item-set answer if the question is complex.
     *
     * @param index The index of the question/prompt that is being currently evaluated
     * @param data  The data to attempt to answer the question with.
     * @return the attempt's {@code int} result code
     * @throws RuntimeException when the question is complex and it has constraints.
     *                          See inline comments.
     * @see QuestionDef#isComplex()
     */
    public int answerQuestion(FormIndex index, IAnswerData data, boolean midSurvey) {
        QuestionDef q = model.getQuestionPrompt(index).getQuestion();
        if (model.getEvent(index) != FormEntryController.EVENT_QUESTION) {
            throw new RuntimeException("Non-Question object at the form index.");
        }
        TreeElement element = model.getTreeElement(index);
        boolean complexQuestion = q.isComplex();

        if (element.isRequired() && data == null) {
            return ANSWER_REQUIRED_BUT_EMPTY;
        } else if (!complexQuestion && !model.getForm().evaluateConstraint(index.getReference(), data)) {
            return ANSWER_CONSTRAINT_VIOLATED;
        } else if (!complexQuestion) {
            commitAnswer(element, index, data, midSurvey);
            return ANSWER_OK;
        } else {
            try {
                // TODO Design a test that exercises this branch.
                model.getForm().copyItemsetAnswer(q, element, data);
            } catch (InvalidReferenceException ire) {
                logger.error("Error", ire);
                throw new RuntimeException("Invalid reference while copying item-set answer: " + ire.getMessage());
            }
            return ANSWER_OK;
        }
    }


    /**
     * saveAnswer attempts to save the current answer into the data model
     * without doing any constraint checking. Only use this if you know what
     * you're doing. For normal form filling you should always use
     * answerQuestion or answerCurrentQuestion.
     */
    public void saveAnswer(FormIndex index, IAnswerData data, boolean midSurvey) {
        if (model.getEvent(index) != FormEntryController.EVENT_QUESTION) {
            throw new RuntimeException("Non-Question object at the form index.");
        }
        TreeElement element = model.getTreeElement(index);
        commitAnswer(element, index, data, midSurvey);
    }


    private void commitAnswer(TreeElement element, FormIndex index, IAnswerData data, boolean midSurvey) {
        if (data != null || element.getValue() != null) {
            // we should check if the data to be saved is already the same as
            // the data in the model, but we can't (no IAnswerData.equals())
            model.getForm().setValue(data, index.getReference(), element, midSurvey);
        }
    }


    /**
     * Navigates forward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToNextEvent() {
        return stepEvent(true);
    }


    /**
     * Navigates backward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToPreviousEvent() {
        return stepEvent(false);
    }

    public void finalizeFormEntry() {
        model.getForm().postProcessInstance();
        formEntryFinalizationProcessors.forEach(formEntryFinalizationProcessor
                -> formEntryFinalizationProcessor.processForm(model));
    }

    public void addPostProcessor(FormEntryFinalizationProcessor formEntryFinalizationProcessor) {
        formEntryFinalizationProcessors.add(formEntryFinalizationProcessor);
    }

    private int stepEvent(boolean forward) {
        FormIndex index = model.getFormIndex();

        do {
            if (forward) {
                index = model.incrementIndex(index);
            } else {
                index = model.decrementIndex(index);
            }
        } while (index.isInForm() && !model.isIndexRelevant(index));

        return jumpToIndex(index);
    }


    public int jumpToIndex(FormIndex index) {
        model.setQuestionIndex(index);
        return model.getEvent(index);
    }

    public void descendIntoNewRepeat() {
        jumpToIndex(model.getForm().descendIntoRepeat(model.getFormIndex(), -1));
        newRepeat(model.getFormIndex());
    }

    public void newRepeat(FormIndex questionIndex) {
        try {
            model.getForm().createNewRepeat(questionIndex);
        } catch (InvalidReferenceException ire) {
            throw new RuntimeException("Invalid reference while copying item-set answer: " + ire.getMessage());
        }
    }


    /**
     * Creates a new repeated instance of the group referenced by the current
     * FormIndex.
     */
    public void newRepeat() {
        newRepeat(model.getFormIndex());
    }

    public void setLanguage(String language) {
        model.setLanguage(language);
    }

    /**
     * Jump to the prompt to add a new repeat for the repeat the controller is currently in. If the current
     * position in the form is not in a repeat nothing will happen.
     */
    public void jumpToNewRepeatPrompt() {
        FormIndex repeatIndex = getRepeatGroupIndex(getModel().getFormIndex(), getModel().getForm());
        if (repeatIndex == null) {
            return;
        }

        int repeatDepth = repeatIndex.getDepth();

        do {
            stepToNextEvent();
        } while (getModel().getEvent() != EVENT_PROMPT_NEW_REPEAT
            || getModel().getFormIndex().getDepth() != repeatDepth);
    }

    private static FormIndex getRepeatGroupIndex(FormIndex index, FormDef formDef) {
        IFormElement element = formDef.getChild(index);
        if (element instanceof GroupDef && ((GroupDef) element).getRepeat()) {
            return index;
        } else {
            if (index.getPreviousLevel() != null) {
                return getRepeatGroupIndex(index.getPreviousLevel(), formDef);
            } else {
                return null;
            }
        }
    }
}
