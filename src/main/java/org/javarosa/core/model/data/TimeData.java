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
import java.time.LocalTime;
import java.util.Date;

import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateUtils.timeAndOffset;

public class TimeData implements IAnswerData {

    public static TimeData dataFrom(String timeString) {
        return new TimeData(timeAndOffset(timeString).localTime);
    }

    public LocalTime localtime;

    /**
     * Empty Constructor, necessary for dynamic construction during deserialization.
     * Shouldn't be used otherwise.
     */
    public TimeData() {
    }

    public TimeData(Date d) {
        throw new RuntimeException();
    }

    public TimeData(LocalTime time) {
        setValue(time);
    }


    @Override
    public IAnswerData clone() {
        return new TimeData(localtime);
    }

    @Override
    public void setValue(Object o) {
        if (localtime != null) throw new IllegalArgumentException("time on TimeData can be set once and only once");
        if (o == null) throw new NullPointerException("Attempt to set an IAnswerData class to null.");

        localtime = (LocalTime) o;
    }

    @Override
    public @NotNull Object getValue() {
        if (localtime == null) throw new NullPointerException("LocalTime not set yet on TimeData");
        return localtime;
    }

    @Override
    public String getDisplayText() {
        return HUMAN_READABLE_SHORT.formatLocalTime(localtime);
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException {
        setValue(ExtUtil.readLocalTime(in));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeLocalTime(out, localtime);
    }

    @Override
    public UncastData uncast() {
        return new UncastData(ISO8601.formatLocalTime(localtime));
    }

    @Override
    public TimeData cast(UncastData data) throws IllegalArgumentException {
        return new TimeData(timeAndOffset(data.value).localTime);
    }

    @Override
    public String toString() {
        return "TimeData{localTime='" + ISO8601.formatLocalTime(localtime) + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeData that = (TimeData) o;
        return localtime.equals(that.localtime);
    }

    @Override
    public int hashCode() {
        return localtime.hashCode();
    }
}
