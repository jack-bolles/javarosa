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

package org.javarosa.xform.util.test;


import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Note that this is just a start and doesn't cover direct comparisons
 * for most values.
 *
 * @author Clayton Sims
 */
public class XFormSimpleAnswerDataSerializerTest {

    final String stringDataValue = "String Data Value";
    final Integer integerDataValue = 5;

    StringData stringData;
    IntegerData integerData;

    TreeElement stringElement = new TreeElement();
    TreeElement intElement = new TreeElement();

    XFormAnswerDataSerializer serializer;


    @Before
    public void setUp() throws Exception {
        stringData = new StringData(stringDataValue);
        stringElement.setValue(stringData);

        integerData = new IntegerData(integerDataValue);
        intElement.setValue(integerData);

        serializer = new XFormAnswerDataSerializer();
    }

    @Test
    public void testString() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer String", serializer.canSerialize(stringElement.getValue()));
        Object answerData = serializer.serializeAnswerData(stringData);
        assertNotNull("Serializer returns Null for valid String Data", answerData);
        assertEquals("Serializer returns incorrect string serialization", answerData, stringDataValue);
    }

    @Test
    public void testInteger() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer Integer", serializer.canSerialize(intElement.getValue()));
        Object answerData = serializer.serializeAnswerData(integerData);
        assertNotNull("Serializer returns Null for valid Integer Data", answerData);
        //assertEquals("Serializer returns incorrect Integer serialization", answerData, integerDataValue);
    }

    @Test
    public void testSelect() {
        //No select tests yet.
    }
}
