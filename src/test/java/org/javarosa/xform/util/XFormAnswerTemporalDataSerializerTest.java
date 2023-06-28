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

package org.javarosa.xform.util;


import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.TreeElement;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.utils.DateUtils.localDateFrom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XFormAnswerTemporalDataSerializerTest {

    final Date utilDateDataValue = new Date();
    final LocalDate localDateDataValue = LocalDate.now();
    final Date timeDataValue = new Date();

    DateData utilDateData;
    DateData dateData;
    TimeData timeData;

    TreeElement utilDateElement = new TreeElement();
    TreeElement dateElement = new TreeElement();
    TreeElement timeElement = new TreeElement();

    XFormAnswerDataSerializer serializer;


    @Before
    public void setUp() throws Exception {
        utilDateData = new DateData(localDateFrom(utilDateDataValue));
        utilDateElement.setValue(utilDateData);

        dateData = new DateData(localDateDataValue);
        dateElement.setValue(dateData);

        timeData = new TimeData(timeDataValue);
        timeElement.setValue(timeData);

        serializer = new XFormAnswerDataSerializer();
    }

    @Test
    public void testDate() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer Date", serializer.canSerialize(dateElement.getValue()));
        Object answerData = serializer.serializeAnswerData(dateData);
        assertNotNull("Serializer returns Null for valid Date Data", answerData);
    }

    @Test
    public void testDateAndLocalDateValuesProduceTheSameSerialisedFormatForDateData() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer Date", serializer.canSerialize(utilDateElement.getValue()));
        Object dateanswerData = serializer.serializeAnswerData(utilDateData);
        Object localdateanswerData = serializer.serializeAnswerData(dateData);
        assertEquals(dateanswerData, localdateanswerData);
    }

    @Test
    public void canReserialiseDateDataUsingLocalDateFromSerialisedDate_RegressionTesting() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer Date", serializer.canSerialize(dateElement.getValue()));
        DateData data = new DateData().cast(utilDateData.uncast());
        assertEquals(dateData, data);
    }

    @Test
    public void testLocalDate() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer Date", serializer.canSerialize(dateElement.getValue()));
        UncastData uncast = dateData.uncast();
        String answerData = (String) serializer.serializeAnswerData(dateData);
        assertEquals(uncast.getValue(), answerData);
    }

    @Test
    public void testTime() {
        assertTrue("Serializer Incorrectly Reports Inability to Serializer Time", serializer.canSerialize(timeElement.getValue()));
        Object answerData = serializer.serializeAnswerData(timeData);
        assertNotNull("Serializer returns Null for valid Time Data", answerData);
    }
}
