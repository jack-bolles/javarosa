package org.javarosa.core.model.data.test;

import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.utils.DateFields;
import org.javarosa.core.model.utils.DateUtils;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static org.javarosa.core.model.utils.DateUtils.TIME_OFFSET_REGEX;
import static org.javarosa.core.model.utils.DateUtils.parseTime;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.junit.Assert.assertEquals;

/**
 * This test is intended to show the limitation of {@link TimeData}.
 * <p>
 * Using this data type in countries which change their time (summer/winter - DST) will cause that forms saved during
 * wintertime and then edited during summertime (and vice versa) will be treated as saved in a neighbor's timezone.
 * It's because we have just time and time offset like: 10:00:00.000+02:00 but we don't know when the form has been
 * saved so we parse it using the current date.
 * <p>
 * Example:
 * If we saved 10:00:00.000+02:00 during summertime (in Poland) and we are editing the form during winter time our
 * timezone is +01:00 not +02:00. As mentioned above javarosa doesn't know that the form has been saved in the same location
 * but different timezone because of DST so it treats the value like saved in the neighbor timezone
 * (in Kiev or London for example).
 * <p>
 * Related issues:
 * <a href="https://github.com/getodk/javarosa/pull/478">...</a>
 * <a href="https://github.com/getodk/collect/issues/170">...</a>
 */
public class TimeDataLimitationsTest {
    public static final TimeZone WARSAW = TimeZone.getTimeZone("Europe/Warsaw");
    public static final TimeZone KIEV = TimeZone.getTimeZone("Europe/Kiev");

    @Test
    public void editingFormsSavedInDifferentTimezoneTest() {
        StringWrapper savedTime = StringWrapper.empty();
        // A user is in Warsaw (GMT+2) saved a form with the time question
        withTimeZone(WARSAW, () -> {
            boolean isSummerTime = TimeZone.getDefault().inDaylightTime(new Date());
            savedTime.set(isSummerTime ? "10:00:00.000+02:00" : "10:00:00.000+01:00");

            // A user opens saved form in Warsaw as well - the hour should be the same
            TimeData timeData = new TimeData(parseTime(savedTime.get()));
            assertEquals("10:00", timeData.getDisplayText());
        });
        // A user travels to Kiev (GMT+3) and opens the saved form again - the hour should be edited +1h
        withTimeZone(KIEV, () -> {
            TimeData timeData = new TimeData(parseTime(savedTime.get()));
            assertEquals("11:00", timeData.getDisplayText());
        });
    }

    @Test
    public void editingFormsSavedInTheSameLocationButAfterDSTChangeTest() {
        DateFields dateFields = DateFields.of(2019, 8, 1);

        withTimeZone(WARSAW, () -> {
            String savedTime = "10:00:00.000+02:00";

            // A user opens saved form in Warsaw and during summertime as well - the hour should be the same
            TimeData timeData = new TimeData(parseTimeWithFixedDate(savedTime, dateFields, WARSAW));
            assertEquals("10:00", timeData.getDisplayText());

            // A user opens saved form in Warsaw as well but during wintertime - the hour is edited -1h (the mentioned limitation)
            dateFields.month = 12;
            timeData = new TimeData(parseTimeWithFixedDate(savedTime, dateFields, WARSAW));
            assertEquals("09:00", timeData.getDisplayText());
        });
    }

    @Test
    public void editingFormsSavedInTheSameLocationButAfterDSTChangeTestNew() {
        LocalTime localTime = LocalTime.parse("10:00:00.000+02:00".split(TIME_OFFSET_REGEX)[0]);

        withTimeZone(WARSAW, () -> {
            // A user opens saved form in Warsaw and during summertime as well - the hour should be the same
            LocalDate summerDate = LocalDate.of(2019, 8, 1);
            TimeData timeData = new TimeData(parseTimeAndPreserveTimeAcrossDST(LocalDateTime.of(summerDate, localTime), WARSAW.toZoneId()));
            assertEquals("10:00", timeData.getDisplayText());

            // A user opens saved form in Warsaw as well but during wintertime - the hour is the same alleviating the mentioned limitation
            LocalDate winterDate = LocalDate.of(2019, 12, 1);
            timeData = new TimeData(parseTimeAndPreserveTimeAcrossDST(LocalDateTime.of(winterDate, localTime), WARSAW.toZoneId()));
            assertEquals("10:00", timeData.getDisplayText());
        });
    }

    @Deprecated
    private static Date parseTimeWithFixedDate(String str, DateFields fields, TimeZone timeZone) {
        if (!parseTime(str, fields)) {
            return null;
        }
        return DateUtils.dateFrom(fields.asLocalDateTime(), ZoneId.of(timeZone.getID()));
    }

    private static Date parseTimeAndPreserveTimeAcrossDST(LocalDateTime localDateTime, ZoneId zoneId) {
        return DateUtils.dateFrom(localDateTime, zoneId);
    }

    static class StringWrapper {
        private String value;

        StringWrapper(String value) {
            this.value = value;
        }

        static StringWrapper empty() {
            return new StringWrapper("");
        }

        public String get() {
            return value;
        }

        public void set(String value) {
            this.value = value;
        }
    }
}
