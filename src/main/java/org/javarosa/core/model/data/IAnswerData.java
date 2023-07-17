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

package org.javarosa.core.model.data;

import org.javarosa.core.model.DataType;
import org.javarosa.core.util.externalizable.Externalizable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;

import static org.javarosa.core.model.DataType.BOOLEAN;
import static org.javarosa.core.model.DataType.CHOICE;
import static org.javarosa.core.model.DataType.DATE;
import static org.javarosa.core.model.DataType.DATE_TIME;
import static org.javarosa.core.model.DataType.GEOPOINT;
import static org.javarosa.core.model.DataType.GEOSHAPE;
import static org.javarosa.core.model.DataType.GEOTRACE;
import static org.javarosa.core.model.DataType.INTEGER;
import static org.javarosa.core.model.DataType.LONG;
import static org.javarosa.core.model.DataType.MULTIPLE_ITEMS;
import static org.javarosa.core.model.DataType.TEXT;
import static org.javarosa.core.model.DataType.TIME;
import static org.javarosa.core.model.utils.DateUtils.localDateFrom;
import static org.javarosa.core.model.utils.DateUtils.localTimeFrom;

/**
 * An IAnswerData object represents an answer to a question
 * posed to a user.
 * <p>
 * IAnswerData objects should never in any circumstances contain
 * a null data value. In cases of empty or non-existent responses,
 * the IAnswerData reference should itself be null.
 *
 * @author Drew Roos
 */
public interface IAnswerData extends Externalizable {
    /**
     * convert the data object returned by the xpath expression into an IAnswerData suitable for
     * storage in the FormInstance.
     * DataType is the primary; some vals can be coerced into a specific type.
     * @see org.javarosa.core.model.data.AnswerDataTest in Test module
     */
    static IAnswerData wrapData(Object val, int intDataType) {
        //droos 1/29/10: we need to come up with a consistent rule for whether the resulting data is determined
        //by the type of the instance node, or the type of the expression result. right now it's a mix and a mess
        //note a caveat with going solely by instance node type is that untyped nodes default to string!

        //for now, these are the rules:
        // if node type == bool, convert to boolean (for numbers, zero = f, non-zero = t; empty string = f, all other datatypes -> error)
        // if numeric data, convert to int if node type is int OR data is an integer; else convert to double
        // if string data or date data, keep as is
        // if NaN or empty string, null

        final DataType dataType = DataType.from(intDataType);

        if (BOOLEAN == dataType || val instanceof Boolean) {
            //ctsims: We should really be using the boolean datatype for real, it's
            //necessary for backend calculations and XSD compliance

            boolean b;

            if (val instanceof Boolean) {
                b = (Boolean) val;
            } else if (val instanceof Double) {
                double d = (Double) val;
                b = Math.abs(d) > 1.0e-12 && !Double.isNaN(d);
            } else if (val instanceof String) {
                String s = (String) val;
                if(s.trim().isEmpty()) return null; //TODO - is this a bug? Preserving behaviour til I learn more.
                b = s.length() > 0;
            } else {
                throw new RuntimeException("unrecognized data representation while trying to convert to BOOLEAN");
            }
            return new BooleanData(b);

        } else if (val instanceof Double) {
            if (((Double) val).isNaN()) return null;

            double d = (Double) val;
            long l = (long) d;
            boolean isIntegral = Math.abs(d - l) < 1.0e-9;
            if (INTEGER == dataType || (isIntegral && (Integer.MAX_VALUE >= l) && (Integer.MIN_VALUE <= l))) {
                return new IntegerData((int) d);
            } else if (LONG == dataType || isIntegral) {
                return new LongData((long) d);
            } else {
                return new DecimalData(d);
            }
        } else if (dataType == GEOPOINT) {
            return new GeoPointData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == GEOSHAPE) {
            return new GeoShapeData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == GEOTRACE) {
            return new GeoTraceData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == CHOICE) {
            return new SelectOneData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == MULTIPLE_ITEMS) {
            return new MultipleItemsData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == TIME) {
            if (val instanceof Date) {
                return new TimeData(localTimeFrom((Date) val));
            } else if (val instanceof LocalTime) {
                return new TimeData((LocalTime) val);
            } else
                throw new UnsupportedOperationException("Use java.time.LocalTime instead of " + val.getClass().getName());
        } else if (val instanceof LocalTime) { //TODO - sweeper 'if'... should have been picked up by 'datatype' == DATE
            //TODO - monitoring to see if this is ever the case
            return new TimeData((LocalTime) val);

        } else if (dataType == DATE) {
            if (val instanceof Date) {
                return new DateData(localDateFrom((Date) val));
            } else if (val instanceof LocalDate) {
                return new DateData((LocalDate) val);
            } else
                throw new UnsupportedOperationException("Use java.time.LocalDate instead of " + val.getClass().getName());
        } else if (val instanceof LocalDate) { //TODO - sweeper 'if'... should have been picked up by 'datatype' == DATE
            //TODO - monitoring to see if this is ever the case
            return new DateData((LocalDate) val);

        } else if (val instanceof Date) {
            if (dataType == DATE_TIME) return new DateTimeData((Date) val);
            else if (dataType == TEXT)  //TODO - is this a bug or bad tests?
                return new DateTimeData((Date) val);
            else
                throw new UnsupportedOperationException("could not match data type with Date: " + dataType.name() + " with value: " + val);

        } else if (val instanceof String) {
            if(((String)val).trim().isEmpty()) {
                System.out.println("dataType = " + dataType);
                return null; //TODO - is this a bug? Preserving behaviour til I learn more.
            }
            return new StringData((String) val);
        } else {
            throw new RuntimeException("unrecognized data type in 'calculate' expression: " + val.getClass().getName() + " with value: " + val + " for  Date: " + dataType.name());
        }
    }

    /**
     * @param o the value of this answerdata object. Cannot be null.
     *          Null Data will not overwrite existing values.
     * @throws NullPointerException if o is null
     */
    void setValue(Object o); //can't be null - TODO- this is not enforced in implementations

    /**
     * @return The value of this answer, will never
     * be null - TODO- this is not enforced in implementations
     */
    @NotNull
    Object getValue();       //will never be null

    /**
     * @return Gets a string representation of this
     * answer
     */
    String getDisplayText();

    IAnswerData clone();

    /**
     * Data types can be uncast if they are expected to be used
     * in different contexts. This allows, for instance, select
     * values to be generated by casting other types or vic-a-versa.
     *
     * @return An uncast representation of this answer which can
     * be used in a different context.
     */
    UncastData uncast();

    /**
     * Casts the provided data into this data type.
     *
     * @param data An uncast data value which is compatible
     *             with this data type
     * @return An instance of the instance's data type
     * which contains that value
     * @throws IllegalArgumentException If the uncast data is
     *                                  not in a compatible format
     */
    IAnswerData cast(UncastData data) throws IllegalArgumentException;
}
