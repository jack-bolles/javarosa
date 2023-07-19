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

package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.time.LocalDateTime.of;
import static org.javarosa.core.model.utils.StringUtils.split;

public class DateUtils {

    public static final String TIME_OFFSET_REGEX = "(?=[Z+\\-])";
    private static final String DATE_TIME_SPLIT_REGEX = "([T\\s])";

    /**
     * Full ISO string interpreted into a Date.
     * Defaults to start of day, in UTC if time and/or offset are missing
     */
    public static Date parseDateTime(String str) {
        LocalDate localDate;
        LocalTime time = LocalTime.MIDNIGHT;
        ZoneId zoneId = ZoneOffset.UTC;

        int i = str.indexOf("T");
        if (i == -1) {
            localDate = localDateFromString(str);
        } else {
            String timeAndOffsetString = str.substring(i + 1);
            if (!timeAndOffsetString.trim().isEmpty()) {
                TimeAndOffset timeAndOffset = timeAndOffset(timeAndOffsetString);
                time = timeAndOffset.localTime;
                zoneId = timeAndOffset.zoneOffset;
            }
            localDate = localDateFromString(str.substring(0, i));
        }

        return dateFrom(of(localDate, time), zoneId);
    }

    /**
     * TODO - assumes just the time and optionally offset,
     * need to guard against broader string?
     */
    @NotNull
    public static TimeAndOffset timeAndOffset(String str) {
        String[] timePieces = str.split(TIME_OFFSET_REGEX);
        ZoneOffset zoneOffset = (timePieces.length == 2)
                ? ZoneOffset.of(timePieces[1])
                : ZoneOffset.UTC;
        return new TimeAndOffset(LocalTime.parse(timePieces[0]), zoneOffset);
    }

    public static LocalDate localDateFromString(String str) {
        String dateString = str.split(DATE_TIME_SPLIT_REGEX)[0];
        List<String> pieces = split(dateString, "-", false);
        if (pieces.size() != 3)
            throw new IllegalArgumentException("Wrong number of fields to parse date: " + dateString);

        return LocalDate.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2)));
    }

    public static class TimeAndOffset {
        public final LocalTime localTime;
        public final ZoneOffset zoneOffset;

        public TimeAndOffset(LocalTime time, ZoneOffset zoneOffset) {
            this.localTime = time;
            this.zoneOffset = zoneOffset;
        }
    }


    /* ==== DATE UTILITY FUNCTIONS ==== */
    @NotNull
    public static Date dateFrom(LocalDateTime someDateTime, ZoneId zoneId) {
        return Date.from(someDateTime.atZone(zoneId).toInstant());
    }

    /** Same as RoundDate(), without the Date instance */
    public static Date getDate(int year, int month, int day) {
        LocalDate localDate = LocalDate.of(year, month, day);
        return dateFrom(of(localDate, LocalTime.MIDNIGHT), TimeZone.getDefault().toZoneId());
    }

    /** @return new Date object with same date but time set to midnight (in current timezone) */
    public static Date roundDate(Date d) {
        if (d == null) return null;
        return dateFrom(of(localDateFrom(d), LocalTime.MIDNIGHT), TimeZone.getDefault().toZoneId());
    }

    public static LocalDate localDateFrom(Date d) {
        return localDateTimeFrom(d).toLocalDate();
    }

    public static LocalTime localTimeFrom(Date val) {
        return localDateTimeFrom(val).toLocalTime();
    }

    public static LocalDateTime localDateTimeFrom(Date dateToConvert) {
        return Instant.ofEpochMilli(dateToConvert.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
