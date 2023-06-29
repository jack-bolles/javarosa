package org.javarosa.core.model.utils;

import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static java.time.ZoneId.of;
import static org.javarosa.core.model.utils.DateFormat.HUMAN_READABLE_SHORT;
import static org.javarosa.core.model.utils.DateFormat.ISO8601;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_HTTP;
import static org.javarosa.core.model.utils.DateFormat.TIMESTAMP_SUFFIX;
import static org.junit.Assert.assertEquals;
public class DateFormatTest {
    private LocalDateTime localDateTime;

    @Before
    public void setUp(){
        Instant instant = Instant.parse("2023-06-11T11:22:33.123Z");
        Clock clock = Clock.fixed(instant, of("Europe/London"));
        ZonedDateTime someDateTime = Instant.now(clock).atZone(of("UTC"));
        localDateTime = someDateTime.toLocalDateTime();
    }

    @Test
    public void formatsDateAsISO8601() {
        assertEquals("2023-06-11", ISO8601.formatLocalDate(localDateTime.toLocalDate()));
    }

    @Test
    public void formatsDateAsHumanShort() {
        assertEquals("11/06/23", HUMAN_READABLE_SHORT.formatLocalDate(localDateTime.toLocalDate()));
    }

    @Test
    public void formatsDateAsTimeStampSuffix() {
        assertEquals("20230611", TIMESTAMP_SUFFIX.formatLocalDate(localDateTime.toLocalDate()));
    }

    @Test
    public void formatsDateAsTimeStampHTTP() {
        assertEquals("Sun, 11 Jun 2023", TIMESTAMP_HTTP.formatLocalDate(localDateTime.toLocalDate()));
    }

    @Test
    public void formatsTimeAsISO8601() {
        assertEquals("11:22:33.123", ISO8601.formatLocalTime(localDateTime.toLocalTime()));
    }

    @Test
    public void formatsTimeAsHumanShort() {
        assertEquals("11:22", HUMAN_READABLE_SHORT.formatLocalTime(localDateTime.toLocalTime()));
    }

    @Test
    public void formatsTimeAsTimeStampSuffix() {
        assertEquals("112233", TIMESTAMP_SUFFIX.formatLocalTime(localDateTime.toLocalTime()));
    }

    @Test
    public void formatsTimeAsTimeStampHTTP() {
        assertEquals("11:22:33 UTC", TIMESTAMP_HTTP.formatLocalTime(localDateTime.toLocalTime()));
    }

    @Test
    public void formatsTimeAsTimeStampHTTPWhenInAnotherTimeZone() {
        assertEquals("10:22:33 UTC", TIMESTAMP_HTTP.formatLocalTime(localDateTime.toLocalTime()));
    }
}