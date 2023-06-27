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

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateUtils.DATE_TIME_SPLIT_REGEX;
import static org.javarosa.core.model.utils.StringUtils.split;

/**
 * A response to a question requesting a Date Value
 * @author Drew Roos
 *
 */
public class DateData implements IAnswerData {
    @NotNull
    public static DateData dataFrom(String dateString) {
        return new DateData(parseDate(dateString));
    }

    /**
     * extracts only date part of ISO string; ignores time and offset pieces
     * returns a Date at the startOfTheDay in the System's default ZoneId
     */
    static Date parseDate(String str) {
        LocalDate localDate = localDateFromString(str);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate localDateFromString(String str) {
        String dateString = str.split(DATE_TIME_SPLIT_REGEX)[0];
        List<String> pieces = split(dateString, "-", false);
        if (pieces.size() != 3) throw new IllegalArgumentException("Wrong number of fields to parse date: " + dateString);

        return LocalDate.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2)));
    }

    private Date d;

    /**
     * Empty Constructor, necessary for dynamic construction during deserialization.
     * Shouldn't be used otherwise.
     */
    public DateData() {

    }

    public DateData (Date d) {
        setValue(d);
    }

    @Override
    public IAnswerData clone () {
        return new DateData(new Date(d.getTime()));
    }

    @Override
    public void setValue (Object o) {
        //Should not ever be possible to set this to a null value
        if(o == null) {
            throw new NullPointerException("Attempt to set an IAnswerData class to null.");
        }

        //make a copy of the date passed in. TODO - poor man's immutability?
        d = DateUtils.roundDate((Date)o);
    }

    @Override
    public @NotNull Object getValue () {
        return new Date(d.getTime());
    }

    @Override
    public String getDisplayText () {
        return HUMAN_READABLE_SHORT.formatDate(d);
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
        setValue(ExtUtil.readDate(in));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeDate(out, d);
    }

    @Override
    public UncastData uncast() {
        return new UncastData(ISO8601.formatDate(d));
    }

    @Override
    public DateData cast(UncastData data) throws IllegalArgumentException {
        return new DateData(parseDate(data.value));

    }

    @Override
    public String toString() {
        return "StringData{d='" + ISO8601.formatDate(d) + "'}";
    }
}
