package org.javarosa.core.model.utils;

import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.javarosa.core.model.utils.StringUtils.split;

public class DateUtilsOldPath {

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
        c.setTime(new Date(DateUtils.dateFrom(f.asLocalDateTime(), ZoneId.of(utc.getID())).getTime() + msecOffset));
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

    private static DateFields getFields(Date d, TimeZone aDefault) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(d);
        cd.setTimeZone(aDefault);

        return new DateFields(cd.get(Calendar.YEAR), cd.get(Calendar.MONTH) + DateUtils.MONTH_OFFSET, cd.get(Calendar.DAY_OF_MONTH), cd.get(Calendar.HOUR_OF_DAY), cd.get(Calendar.MINUTE), cd.get(Calendar.SECOND), cd.get(Calendar.MILLISECOND), cd.get(Calendar.DAY_OF_WEEK), cd.get(Calendar.WEEK_OF_YEAR));
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

    private static class SecondsAndTicks {
        private final int second;
        private final int secTicks;

        private SecondsAndTicks(int second, int secTicks) {
            this.second = second;
            this.secTicks = secTicks;
        }
    }
}
