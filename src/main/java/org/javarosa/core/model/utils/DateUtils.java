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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.javarosa.core.model.utils.StringUtils.split;

public class DateUtils {

    public static final String TIME_OFFSET_REGEX = "(?=[Z+\\-])";
    public static final int MONTH_OFFSET = (1 - Calendar.JANUARY);

    @NotNull
    public static Date dateFrom(LocalDateTime someDateTime, ZoneId zoneId) {
        return Date.from(someDateTime.atZone(zoneId).toInstant());
    }

    public static Date dateFromLocal(LocalDate localDate, ZoneId zoneId) {
        return Date.from(localDate.atStartOfDay(zoneId).toInstant());
    }

    public static final long DAY_IN_MS = 86400000L;

    public DateUtils() {
    }

    public static int secTicksAsNanoSeconds(int millis) {
        return Math.toIntExact(NANOSECONDS.convert(millis, MILLISECONDS));
    }

    /* ==== PARSING DATES/TIMES FROM STANDARD STRINGS ==== */
    public static Date parseDateTime(String str) {
        int i = str.indexOf("T");
        if (i == -1) return parseDate(str);

        if (stringDoesntHaveDateFields(str.substring(0, i)))
            return null;
        else {
            DateFields fields = new DateFields();
            boolean hasTime = parseTimeAndOffsetSegmentsForDateTime(str.substring(i + 1), fields);
            if (!hasTime) {
                return null;
            } else {
                String dateStr = str.substring(0, i);
                LocalDate localDate =localDateFromString(dateStr);
                DateFields newDate = DateFields.of(localDate.getYear(), localDate.getMonthValue(), localDate.getDayOfMonth());
                parseTimeAndOffsetSegmentsForDateTime(str.substring(i + 1), newDate);
                TimeZone tz = TimeZone.getDefault();
                return dateFrom(newDate.asLocalDateTime(), ZoneId.of(tz.getID()));
            }
        }

    }

    /** expects only date part of ISO string; ignores time and offset pieces */
    public static Date parseDate(String str) {
        if (stringDoesntHaveDateFields(str)) {
            throw new IllegalArgumentException("Fields = " + str);
        }

        localDateFromString(str);
        return dateFromLocal(localDateFromString(str), ZoneId.systemDefault());
    }

    private static LocalDate localDateFromString(String str) {
        List<String> pieces = split(str, "-", false);
        if (pieces.size() != 3) throw new IllegalArgumentException("Wrong number of fields to parse date: " + str);

        return LocalDate.of(Integer.parseInt(pieces.get(0)), Integer.parseInt(pieces.get(1)), Integer.parseInt(pieces.get(2)));
    }

    //TODO -assumes just the time string, need to guard against broader string
    public static Date parseTime(String str) {
        TimeAndOffset to = timeAndOffset(str);
        return dateFrom(LocalDateTime.of(LocalDate.now(),
                        LocalTime.parse(to.timePieces[0])),
                to.zoneOffset);
    }

    @NotNull
    private static TimeAndOffset timeAndOffset(String str) {
        String[] timePieces = str.split(TIME_OFFSET_REGEX);
        ZoneOffset zoneOffset = (timePieces.length == 2)
                ? ZoneOffset.of(timePieces[1])
                : OffsetDateTime.now().getOffset();
        return new TimeAndOffset(timePieces, zoneOffset);
    }

    private static class TimeAndOffset {
        public final String[] timePieces;
        public final ZoneOffset zoneOffset;

        public TimeAndOffset(String[] timePieces, ZoneOffset zoneOffset) {
            this.timePieces = timePieces;
            this.zoneOffset = zoneOffset;
        }
    }

    private static boolean stringDoesntHaveDateFields(String dateStr) {
        try {
            List<String> pieces = split(dateStr, "-", false);
            if (pieces.size() != 3)
                throw new IllegalArgumentException("Wrong number of fields to parse date: " + dateStr);

            //TODO -do better; Check for NumberFormatException - expensive way to check for correctness, without actually chekcing for correctness (type-yes; values-no
            Integer.parseInt(pieces.get(0));
            Integer.parseInt(pieces.get(1));
            Integer.parseInt(pieces.get(2));
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /** should only be used as part of constructing a DateTime.
     * Offsets, in particular, are meaningless for Time on its own.
     * @see DateUtils.parseTime(String timeStr) for getting a standalone string. */
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
        List<String> pieces = split(timeStr, ":", false);
        if (pieces.size() != 2 && pieces.size() != 3) return false;

        try {
            int hour = Integer.parseInt(pieces.get(0));
            int minute = Integer.parseInt(pieces.get(1));
            int second = 0;
            int secTicks = 0;

            if (pieces.size() == 3) {
                String secStr = pieces.get(2);
                int i;
                for (i = 0; i < secStr.length(); i++) {
                    char c = secStr.charAt(i);
                    if (!Character.isDigit(c) && c != '.') break;
                }
                secStr = secStr.substring(0, i);

                double fsec = Double.parseDouble(secStr);
                second = (int) fsec;
                secTicks = (int) (1000.0 * fsec - 1000.0 * second);
            }

            ff.hour = hour;
            ff.minute = minute;
            ff.second = second;
            ff.secTicks = secTicks;
            return ff.check();
        } catch (NumberFormatException nfe) {
            return false;
        }
    }


    /* ==== DATE UTILITY FUNCTIONS ==== */

    /** Same as RoundDate(), without the Date instance */
    public static Date getDate(int year, int month, int day) {
        TimeZone tz = TimeZone.getDefault();
        LocalDate localDate = LocalDate.of(year, month, day);
        return dateFrom(LocalDateTime.of(localDate, LocalTime.MIDNIGHT), tz.toZoneId());
    }

    /**
     * @return new Date object with same date but time set to midnight (in current timezone)
     */
    public static Date roundDate(Date d) {
        if (d == null) return null;

        TimeZone tz = TimeZone.getDefault();
        return dateFrom(LocalDateTime.of(localDateFrom(d, tz), LocalTime.MIDNIGHT), tz.toZoneId());
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

    private static LocalTime localTimeFrom(Date d, TimeZone aDefault) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(d);
        cd.setTimeZone(aDefault);
        return LocalTime.of(cd.get(Calendar.HOUR_OF_DAY),
                cd.get(Calendar.MINUTE),
                cd.get(Calendar.SECOND),
                secTicksAsNanoSeconds(cd.get(Calendar.MILLISECOND))
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
