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

package org.javarosa.core.model.instance.test;


import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QuestionDataElementTests {
    private final String stringElementName = "String Data Element";

    private StringData stringData;
    private IntegerData integerData;
    private TreeElement stringElement;
    private TreeElement intElement;

    @Before
    public void setUp() throws Exception {

        stringData = new StringData("Answer Value");
        integerData = new IntegerData(4);

        intElement  = new TreeElement("intElement");
        intElement.setValue(integerData);

        stringElement = new TreeElement(stringElementName);
        stringElement.setValue(stringData);

    }
    @Test
    public void testIsLeaf() {
        assertTrue("Question Data Element returned negative for being a leaf",stringElement.isLeaf());
    }

    @Test
    public void testGetName() {
        assertEquals("Question Data Element 'string' did not properly get its name", stringElement.getName(), stringElementName);
    }

    @Test
    public void testSetName() {
        String newName = "New Name";
        stringElement.setName(newName);

        assertEquals("Question Data Element 'string' did not properly set its name", stringElement.getName(), newName);
    }

    @Test
    public void testGetValue() {
        IAnswerData data = stringElement.getValue();
        assertEquals("Question Data Element did not return the correct value",data,stringData);
    }

    @Test
    public void testSetValue() {
        stringElement.setValue(integerData);
        assertEquals("Question Data Element did not set value correctly",stringElement.getValue(),integerData);

        try {
            stringElement.setValue(null);
        } catch(Exception e) {
            fail("Question Data Element did not allow for its value to be set as null");
        }

        assertNull("Question Data Element did not return a null value correctly", stringElement.getValue());

    }

    private static class MutableBoolean {
        private boolean bool;

        MutableBoolean(boolean bool) {
            this.bool = bool;
        }

        boolean getValue() {
            return bool;
        }
    }

    @Test
    public void testAcceptsVisitor() {
        MutableBoolean visitorAccepted = new MutableBoolean(false);
        MutableBoolean dispatchedWrong = new MutableBoolean(false);
        ITreeVisitor sampleVisitor = new ITreeVisitor() {

            public void visit(FormInstance tree) {
                dispatchedWrong.bool = true;
            }
            public void visit(AbstractTreeElement element) {
                visitorAccepted.bool = true;
            }
        };

        stringElement.accept(sampleVisitor);
        assertTrue("The visitor's visit method was not called correctly by the QuestionDataElement",visitorAccepted.getValue());

        assertFalse("The visitor was dispatched incorrectly by the QuestionDataElement", dispatchedWrong.getValue());
    }

    @Test
    public void testSuperclassMethods() {
        //stringElement should not have a root at this point.

        //TODO: Implement tests for the 'attribute' system.
    }
}