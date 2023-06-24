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
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.time.LocalDateTime.of;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.javarosa.core.model.utils.StringUtils.split;

public class DateUtils {

    public static final String TIME_OFFSET_REGEX = "(?=[Z+\\-])";
    public static final int MONTH_OFFSET = (1 - Calendar.JANUARY);
    public static final long DAY_IN_MS = 86400000L;

    @NotNull
    public static Date dateFrom(LocalDateTime someDateTime, ZoneId zoneId) {
        return Date.from(someDateTime.atZone(zoneId).toInstant());
    }

    public DateUtils() {
    }

    public static int secTicksAsNanoSeconds(int millis) {
        return Math.toIntExact(NANOSECONDS.convert(millis, MILLISECONDS));
    }

    /** Full ISO string interpreted into a Date. Defaults to today, at start of day, in UTC */
    public static Date parseDateTime(String str) {
        int i = str.indexOf("T");
        if (i == -1) return parseDate(str);

        TimeAndOffset timeAndOffset;
        LocalTime time = LocalTime.MIDNIGHT;
        ZoneId zoneId = ZoneOffset.UTC;
        String timeAndOffsetString = str.substring(i + 1);
        if (!timeAndOffsetString.trim().isEmpty()) {
            timeAndOffset = timeAndOffset(timeAndOffsetString);
            time = timeAndOffset.localTime;
            zoneId = timeAndOffset.zoneOffset;
        }
        LocalDate localDate = localDateFromString(str.substring(0, i));
        return dateFrom(of(localDate, time), zoneId);
    }

    /**
     * expects only date part of ISO string; ignores time and offset pieces
     * returns a Date at the startOfTheDay in the System's default ZoneId
     */
    public static Date parseDate(String str) {
        LocalDate localDate = localDateFromString(str);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate localDateFromString(String str) {
        List<String> pieces = split(str, "-", false);
        if (pieces.size() != 3) throw new IllegalArgumentException("Wrong number of fields to parse date: " + str);

        return LocalDate.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2)));
    }

    /**
     * TODO - assumes just the time string, need to guard against broader string?
     * uses current date, at the time specified
     */
    public static Date parseTime(String str) {
        TimeAndOffset to = timeAndOffset(str);
        return dateFrom(of(LocalDate.now(), to.localTime), to.zoneOffset);
    }

    @NotNull
    private static TimeAndOffset timeAndOffset(String str) {
        String[] timePieces = str.split(TIME_OFFSET_REGEX);
        ZoneOffset zoneOffset = (timePieces.length == 2)
                ? ZoneOffset.of(timePieces[1])
                : ZoneOffset.UTC;
        return new TimeAndOffset(LocalTime.parse(timePieces[0]), zoneOffset);
    }

    private static class TimeAndOffset {
        private final LocalTime localTime;
        private final ZoneOffset zoneOffset;

        private TimeAndOffset(LocalTime time, ZoneOffset zoneOffset) {
            this.localTime = time;
            this.zoneOffset = zoneOffset;
        }
    }

    /**
     * Only used by one test method. not the mainline of parsing a string to a date/time value
     * will be removing in a subsequent commit
     *
     * @see DateUtils.parseTime(String) for getting a standalone string.
     */
    @Deprecated
    public static boolean parseTimeAndOffsetSegmentsForDateTime(String timeStr, DateFields f) {
        //get timezone information first. Make a Datefields set for the possible offset
        //NOTE: DO NOT DO DIRECT COMPUTATIONS AGAINST THIS. It's a holder for hour/minute
        //data only, but has data in other fields
        DateFields timeOffset = null;

        if (timeStr.charAt(timeStr.length() - 1) == 'Z') {
            //UTC!

            //Clean up string for later processing
            timeStr = timeStr.substring(0, timeStr.length() - 1);
            timeOffset = new DateFields();
        } else if (timeStr.contains("+") || timeStr.contains("-")) {
            timeOffset = new DateFields();

            List<String> pieces = split(timeStr, "+", false);

            //We're going to add the Offset straight up to get UTC
            //so we need to invert the sign on the offset string
            int offsetSign = -1;

            if (pieces.size() <= 1) {
                pieces = split(timeStr, "-", false);
                offsetSign = 1;
            }

            timeStr = pieces.get(0);

            String offset = pieces.get(1);
            String hours = offset;
            if (offset.contains(":")) {
                List<String> tzPieces = split(offset, ":", false);
                hours = tzPieces.get(0);
                int mins = Integer.parseInt(tzPieces.get(1));
                timeOffset.minute = mins * offsetSign;
            }
            timeOffset.hour = Integer.parseInt(hours) * offsetSign;
        }

        //Do the actual parse for the real time values;
        if (!parseRawTime(timeStr, f)) {
            return false;
        }

        if (!(f.check())) {
            return false;
        }

        //Time is good, if there was no timezone info, just return that;
        if (timeOffset == null) {
            return true;
        }

        //Now apply any relevant offsets from the timezone.
        TimeZone utc = TimeZone.getTimeZone("UTC");
        Calendar c = Calendar.getInstance(utc);
        long msecOffset = (((60L * timeOffset.hour) + timeOffset.minute) * 60 * 1000L);
        c.setTime(new Date(dateFrom(f.asLocalDateTime(), ZoneId.of(utc.getID())).getTime() + msecOffset));
        //c is now in the timezone of the parsed value, so put
        //it in the local timezone.
        c.setTimeZone(TimeZone.getDefault());

        Date d = c.getTime();
        DateFields adjusted = getFields(d, TimeZone.getDefault());

        // time zone adjustment may +/- across midnight
        // which can result in +/- across a year
        f.year = adjusted.year;
        f.month = adjusted.month;
        f.day = adjusted.day;
        f.dow = adjusted.dow;
        f.hour = adjusted.hour;
        f.minute = adjusted.minute;
        f.second = adjusted.second;
        f.secTicks = adjusted.secTicks;

        return f.check();
    }

    /**
     * Parse the raw components of time (hh:mm:ss) with no timezone information
     */
    private static boolean parseRawTime(String timeStr, DateFields ff) {
        RawTime rawTime = asRawTime(timeStr);
        if (rawTime == null) return false;
        ff.hour = rawTime.hour;
        ff.minute = rawTime.minute;
        ff.second = rawTime.second;
        ff.secTicks = rawTime.secTicks;
        return ff.check();

    }

    @Nullable
    private static RawTime asRawTime(String timeStr) {
        try {
            List<String> pieces = split(timeStr, ":", false);
            if (pieces.size() != 2 && pieces.size() != 3) return null;

            int hour = Integer.parseInt(pieces.get(0));
            int minute = Integer.parseInt(pieces.get(1));
            int second = 0;
            int secTicks = 0;

            if (pieces.size() == 3) {
                SecondsAndTicks tAndO = secondsAndTicks(pieces.get(2));
                second = tAndO.second;
                secTicks = tAndO.secTicks;
            }
            return new RawTime(hour, minute, second, secTicks);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private static class RawTime {
        private final int hour;
        private final int minute;
        private final int second;
        private final int secTicks;

        private RawTime(int hour, int minute, int second, int secTicks) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.secTicks = secTicks;
        }
    }

    private static SecondsAndTicks secondsAndTicks(String secondsAndTicks) {
        String secStr = secondsAndTicks;
        int i;
        for (i = 0; i < secStr.length(); i++) {
            char c = secStr.charAt(i);
            if (!Character.isDigit(c) && c != '.') break;
        }
        secStr = secStr.substring(0, i);

        double fsec = Double.parseDouble(secStr);
        int second = (int) fsec;
        int secTicks = (int) (1000.0 * fsec - 1000.0 * second);
        return new SecondsAndTicks(second, secTicks);
    }

    private static class SecondsAndTicks {
        private final int second;
        private final int secTicks;

        private SecondsAndTicks(int second, int secTicks) {
            this.second = second;
            this.secTicks = secTicks;
        }
    }


    /* ==== DATE UTILITY FUNCTIONS ==== */

    /** Same as RoundDate(), without the Date instance */
    public static Date getDate(int year, int month, int day) {
        TimeZone tz = TimeZone.getDefault();
        LocalDate localDate = LocalDate.of(year, month, day);
        return dateFrom(of(localDate, LocalTime.MIDNIGHT), tz.toZoneId());
    }

    /** @return new Date object with same date but time set to midnight (in current timezone) */
    public static Date roundDate(Date d) {
        if (d == null) return null;

        TimeZone tz = TimeZone.getDefault();
        return dateFrom(of(localDateFrom(d, tz), LocalTime.MIDNIGHT), tz.toZoneId());
    }

    private static LocalDate localDateFrom(Date d, TimeZone aDefault) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(d);
        cd.setTimeZone(aDefault);

        return LocalDate.of(cd.get(Calendar.YEAR),
                cd.get(Calendar.MONTH) + MONTH_OFFSET,
                cd.get(Calendar.DAY_OF_MONTH)
        );
    }

    /* ==== DATE OPERATIONS ==== */

    /**
     * Creates a Date object representing the amount of time between the
     * reference date, and the given parameters.
     *
     * @param ref          The starting reference date
     * @param type         "week", or "month", representing the time period which is to be returned.
     * @param start        "sun", "mon", ... etc. representing the start of the time period.
     * @param beginning    true=return first day of period, false=return last day of period
     * @param includeToday Whether today's date can count as the last day of the period
     * @param nAgo         How many periods ago. 1=most recent period, 0=period in progress
     * @return a Date object representing the amount of time between the
     * reference date, and the given parameters.
     */
    public static Date getPastPeriodDate(Date ref, String type, String start, boolean beginning, boolean includeToday, int nAgo) {
        if (type.equals("week")) {
            Calendar cd = Calendar.getInstance();
            cd.setTime(ref);
            int current_dow = cd.get(Calendar.DAY_OF_WEEK) - 1;
            int target_dow = DOW.valueOf(start).order;
            int offset = (includeToday ? 1 : 0);
            int diff = ((current_dow - target_dow + 7 + offset) % 7 - offset) + (7 * nAgo) - (beginning ? 0 : 6); //booyah
            return new Date(ref.getTime() - diff * DAY_IN_MS);
        } else if (type.equals("month")) {
            //not supported
            return null;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static DateFields getFields(Date d, TimeZone aDefault) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(d);
        cd.setTimeZone(aDefault);

        return new DateFields(cd.get(Calendar.YEAR), cd.get(Calendar.MONTH) + MONTH_OFFSET, cd.get(Calendar.DAY_OF_MONTH), cd.get(Calendar.HOUR_OF_DAY), cd.get(Calendar.MINUTE), cd.get(Calendar.SECOND), cd.get(Calendar.MILLISECOND), cd.get(Calendar.DAY_OF_WEEK), cd.get(Calendar.WEEK_OF_YEAR));
    }

    //convenience, should go away soon
    private enum DOW {
        sun(0), mon(1), tue(2), wed(3), thu(4), fri(5), sat(6);
        final int order;

        DOW(int ordinal) {
            this.order = ordinal;
        }
    }
}
