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

import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateUtils.localDateFromString;

public class DateData implements IAnswerData {

    @NotNull
    public static DateData dataFrom(String dateString) {
        return new DateData(localDateFromString(dateString));
    }

    private LocalDate localDate;

    /**
     * Empty Constructor, necessary for dynamic construction during deserialization.
     * Shouldn't be used otherwise.
     */
    public DateData() { }

    public DateData(LocalDate localDate) {
        setValue(localDate);
    }

    @Override
    public IAnswerData clone() {
        return new DateData(localDate);
    }

    @Override
    public void setValue(Object o) {
        if(localDate != null){
            throw new IllegalArgumentException("date on DateData can be set once and only once");
        }

        if (o == null) {
            throw new NullPointerException("Attempt to set DateData::IAnswerData to null.");
        }
        localDate = (LocalDate) o;
    }

    @Override
    public @NotNull Object getValue() {
        if(localDate == null) throw new NullPointerException("LocalDate not set yet on DateData");
        return localDate;
    }

    @Override
    public String getDisplayText() {
        return HUMAN_READABLE_SHORT.formatLocalDate(localDate);
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
        setValue(ExtUtil.readLocalDate(in));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeLocalDate(out, localDate);
    }

    @Override
    public UncastData uncast() {
        return new UncastData(ISO8601.formatLocalDate(localDate));
    }

    @Override
    public DateData cast(UncastData data) throws IllegalArgumentException {
        return new DateData(localDateFromString(data.value));
    }

    @Override
    public String toString() {
        return "DateData{localDate='" + ISO8601.formatLocalDate(localDate) + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateData that = (DateData) o;
        return localDate.equals(that.localDate);
    }

    @Override
    public int hashCode() {
        return localDate.hashCode();
    }

}
