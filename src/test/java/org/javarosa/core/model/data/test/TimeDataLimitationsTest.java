package org.javarosa.core.model.data.test;

import org.javarosa.core.model.data.TimeData;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import static org.javarosa.core.model.utils.DateUtils.TIME_OFFSET_REGEX;
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

    @Test
    public void formsEditedInDifferentTimeZonesKeepTheTimeAndIgnoreTimezones(){
        String warsawTime = "10:00:00.000+02:00";
        String kievTime = "10:00:00.000+03:00";

        assertEquals(TimeData.dataFrom(warsawTime), TimeData.dataFrom(kievTime));
    }

    @Test
    public void editingFormsSavedInTheSameLocationButAfterDSTChangeRetainsTimeOfDay() {
        LocalTime localTime = LocalTime.parse("10:00:00.000+02:00".split(TIME_OFFSET_REGEX)[0]);
        // A user opens saved form in Warsaw and during summertime as well - the hour should be the same
        LocalDate summerDate = LocalDate.of(2019, 8, 1);
        TimeData timeData = new TimeData(getZonedDateTime(LocalDateTime.of(summerDate, localTime), WARSAW.toZoneId()).toLocalTime());
        assertEquals("10:00", timeData.getDisplayText());

        // A user opens saved form in Warsaw as well but during wintertime - the hour is the same alleviating the mentioned limitation
        LocalDate winterDate = LocalDate.of(2019, 12, 1);
        timeData = new TimeData(getZonedDateTime(LocalDateTime.of(winterDate, localTime), WARSAW.toZoneId()).toLocalTime());
        assertEquals("10:00", timeData.getDisplayText());
    }

    @NotNull
    private static ZonedDateTime getZonedDateTime(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId);
    }
}
