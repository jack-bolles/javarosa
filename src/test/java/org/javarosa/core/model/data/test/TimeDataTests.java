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

package org.javarosa.core.model.data.test;


import org.javarosa.core.model.data.TimeData;
import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TimeDataTests {

    @Test
    public void testTimeCanBeSetOnceAndOnlyOnce() {
        LocalTime now = LocalTime.of(10, 15);
        LocalTime earlier = LocalTime.of(9, 15);

        TimeData data = new TimeData();
        data.setValue(now);
        assertEquals(now, data.getValue());

        try {
            data.setValue(now);
            fail("Don't even try, even if the same value");
        } catch (IllegalArgumentException e) {
            //as expected
        }
        try {
            data.setValue(earlier);
            fail();
        } catch (IllegalArgumentException e) {
            //as expected
        }

        //prove nothing's changed
        assertEquals(now, data.getValue());
    }


    @Test(expected = NullPointerException.class)
    public void testNullData() {
        new TimeData((LocalTime) null);
    }
}
