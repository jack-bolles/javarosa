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

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class QuestionDataGroupTests {

    StringData stringData;
    IntegerData integerData;

    IDataReference stringReference;

    IDataReference integerReference;

    TreeElement stringElement;
    TreeElement intElement;

    TreeElement group;

    @Before
    public void setUp() throws Exception {

        stringData = new StringData("Answer Value");
        integerData = new IntegerData(4);

        stringReference = new IDataReference() {
            String reference = "stringValue";

            public Object getReference() {
                return reference;
            }

            public void setReference(Object reference) {
                this.reference = (String)reference;
            }

            public void readExternal(DataInputStream in, PrototypeFactory pf) {}

            public void writeExternal(DataOutputStream out) {}
        };

        integerReference = new IDataReference() {
            Integer intReference = 15;

            public Object getReference() {
                return intReference;
            }

            public void setReference(Object reference) {
                this.intReference = (Integer)reference;
            }

            public void readExternal(DataInputStream in, PrototypeFactory pf) {}

            public void writeExternal(DataOutputStream out) {}
        };

        intElement  = new TreeElement("intElement");
        intElement.setValue(integerData);

        String stringElementName = "String Data Element";
        stringElement = new TreeElement(stringElementName);
        stringElement.setValue(stringData);

        String groupName = "TestGroup";
        group = new TreeElement(groupName);
    }
    @Test
    public void testIsLeaf() {
        assertTrue("A Group with no children should report being a leaf", group.isLeaf());
        group.addChild(stringElement);
        assertFalse("A Group with children should not report being a leaf", group.isLeaf());
    }

    @Test
    public void testGetName() {
        String name = "TestGroup";
        assertEquals("Question Data Group did not properly get its name", group.getName(), name);
        group.addChild(stringElement);
        assertEquals("Question Data Group's name was changed improperly", group.getName(), name);
    }

    @Test
    public void testSetName() {
        String name = "TestGroup";
        group = new TreeElement(name);
        String newName = "TestGroupNew";
        group.setName(newName);
        assertEquals("Question Data Group did not properly get its name", group.getName(), newName);
    }

    private static class MutableBoolean {
        private boolean bool;

        public MutableBoolean(boolean bool) {
            this.bool = bool;
        }

        void setValue(boolean bool) {
            this.bool = bool;
        }

        boolean getValue() {
            return bool;
        }
    }

    @Test
    public void testAcceptsVisitor() {
        final MutableBoolean visitorAccepted = new MutableBoolean(false);
        final MutableBoolean dispatchedWrong = new MutableBoolean(false);
        ITreeVisitor sampleVisitor = new ITreeVisitor() {

            public void visit(FormInstance tree) {
                dispatchedWrong.setValue(true);

            }
            public void visit(AbstractTreeElement element) {
                visitorAccepted.setValue(true);
            }
        };

        stringElement.accept(sampleVisitor);
        assertTrue("The visitor's visit method was not called correctly by the QuestionDataElement",visitorAccepted.getValue());
        assertFalse("The visitor was dispatched incorrectly by the QuestionDataElement", dispatchedWrong.getValue());
    }

    @Test
    public void testAddLeafChild() {
        boolean added = false;
        try {
            group.addChild(stringElement);
            group.getChildAt(0);
            assertEquals("Added element was not in Question Data Group's children!", group.getChildAt(0), stringElement);
        }
        catch(RuntimeException e) {
            if(!added) {
                fail("Group did not report success adding a valid child");
            }
        }

        try {
            TreeElement leafGroup = new TreeElement("leaf group");
            group.addChild(leafGroup);
            assertEquals("Added element was not in Question Data Group's children", group.getChildAt(1), leafGroup);
        }
        catch (RuntimeException e) {
            if(!added) {
                fail("Group did not report success adding a valid child");
            }
        }
    }

    @Test
    public void testAddTreeChild() {
        TreeElement subElement = new TreeElement("SubElement");
        subElement.addChild(stringElement);
        subElement.addChild(intElement);
    }
}
