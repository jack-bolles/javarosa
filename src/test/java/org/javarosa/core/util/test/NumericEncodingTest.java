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

package org.javarosa.core.util.test;


import org.javarosa.core.util.externalizable.ExtWrapIntEncoding;
import org.javarosa.core.util.externalizable.ExtWrapIntEncodingSmall;
import org.javarosa.core.util.externalizable.ExtWrapIntEncodingUniform;
import org.junit.Test;

public class NumericEncodingTest {

    private void testNumericEncoding (long val, ExtWrapIntEncoding encoding) {
        ExternalizableTest.testExternalizable(encoding.clone(val), encoding, null, null);
    }

    @Test
    public void testIntEncodingUniform () {
        ExtWrapIntEncoding enc = new ExtWrapIntEncodingUniform();

        testNumericEncoding(0, enc);
        testNumericEncoding(-1, enc);
        testNumericEncoding(1, enc);
        testNumericEncoding(-2, enc);

        for (int i = 3; i <= 64; i++) {
            long min = (i < 64 ? -((long)0x01 << (i - 1)) : Long.MIN_VALUE);
            long max = (i < 64 ? ((long)0x01 << (i - 1)) - 1 : Long.MAX_VALUE);

            testNumericEncoding(max - 1, enc);
            testNumericEncoding(max, enc);
            if (i < 64)
                testNumericEncoding(max + 1, enc);
            testNumericEncoding(min + 1, enc);
            testNumericEncoding(min, enc);
            if (i < 64)
                testNumericEncoding(min - 1, enc);
        }
    }

    @Test
    public void testIntEncodingSmall () {
        int[] biases = {0, 1, 30, 128, 254};
        int[] smallTests = {0, 1, 126, 127, 128, 129, 253, 254, 255, 256, -1, -2, -127, -128, -129};
        int[] largeTests = {3750, -3750, 33947015, -33947015, Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MIN_VALUE, Integer.MIN_VALUE + 1};

        for (int i = -1; i < biases.length; i++) {
            int bias;

            if (i == -1) {
                bias = ExtWrapIntEncodingSmall.DEFAULT_BIAS;
            } else {
                bias = biases[i];
                if (bias == ExtWrapIntEncodingSmall.DEFAULT_BIAS)
                    continue;
            }

            ExtWrapIntEncoding enc = new ExtWrapIntEncodingSmall(bias);

            for (int smallTest : smallTests) {
                testNumericEncoding(smallTest, enc);
                if (bias != 0)
                    testNumericEncoding(smallTest - bias, enc);
            }

            for (int largeTest : largeTests) {
                testNumericEncoding(largeTest, enc);
            }
        }
    }
}
