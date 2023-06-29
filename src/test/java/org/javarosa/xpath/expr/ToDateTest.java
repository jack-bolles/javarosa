package org.javarosa.xpath.expr;

import org.javarosa.xpath.XPathTypeMismatchException;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.javarosa.core.model.utils.DateUtilsForTesting.dateFromLocalDateTime;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;
import static org.javarosa.xpath.expr.XPathFuncExpr.toDate;
import static org.junit.Assert.assertEquals;

public class ToDateTest {
    private static final ZonedDateTime EPOCH_UTC_ZONED_DATE_TIME
            = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC")); // 1970-01-01T00:00:00 UTC

    @NotNull
    private static ZonedDateTime adjustLocalDateToOffsetTimeZone(LocalDateTime localDateTime, int hoursToOffset) {
        ZoneOffset offsetFromLocal = ZoneOffset.ofHours(hoursToOffset);
        ZoneId zoneIdFromLocal = ZoneId.ofOffset("GMT", offsetFromLocal);
        ZoneId resetZoneId = ZoneId.ofOffset("GMT", ZoneOffset.ofHours(0));
        return ZonedDateTime.of(localDateTime, zoneIdFromLocal).withZoneSameInstant(resetZoneId);
    }

    private static int secTicksAsNanoSeconds(int millis) {
        return Math.toIntExact(TimeUnit.NANOSECONDS.convert(millis, TimeUnit.MILLISECONDS));
    }

    @NotNull
    private static LocalDateTime localDateTime(int year, int month, int day, int hour, int minute, int second, int milli) {
        return LocalDateTime.of(LocalDate.of(year, month, day), LocalTime.of(hour, minute, second, secTicksAsNanoSeconds(milli)));
    }

    @NotNull
    private static Date epochPlusAYear(String tzID) {
        ZonedDateTime zonedDateTime = EPOCH_UTC_ZONED_DATE_TIME.plusDays(365)
                .withZoneSameLocal(ZoneId.of(tzID));
        return dateFromLocalDateTime(zonedDateTime.toLocalDateTime());
    }

    @Test
    public void convertsISO8601DatesWithoutPreservingTime() {
        assertEquals(
                dateFromLocalDateTime(LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.MIDNIGHT)),
                toDate("2018-01-01", false)
        );
    }

    @Test
    public void convertsISO8601DatesWithoutOffsetPreservingTime() {
        assertEquals(
                dateFromLocalDateTime(localDateTime(2018, 1, 1, 10, 20, 30, 400)),
                toDate("2018-01-01T10:20:30.400", true)
        );
    }

    @Test
    public void convertsISO8601DatesWithOffsetPreservingTime() {
        LocalDateTime localDateTime = localDateTime(2018, 1, 1, 10, 20, 30, 400);
        ZonedDateTime zonedDateTime = adjustLocalDateToOffsetTimeZone(localDateTime, 2);
        assertEquals(
                dateFromLocalDateTime(zonedDateTime.toLocalDateTime()),
                toDate("2018-01-01T10:20:30.400+02", true)
        );
    }

    @Test
    public void convertsTimestampsWithoutPreservingTime() {
        assertEquals(
                dateFromLocalDateTime(EPOCH_UTC_ZONED_DATE_TIME.plusDays(365).toLocalDateTime()), // 1971-01-01T00:00:00 UTC
                toDate(365d, false));
    }

    @Test
    public void convertsTimestampsWithoutPreservingTimeOnLocalTimeZone() {
        //new
        TimeZone PST = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"));
        withTimeZone(PST, tz -> assertEquals(
                        epochPlusAYear(tz.getID()),
                        toDate(365d, false)
                )
        );
    }

    @Test
    public void convertsTimestampsToDatesAtMidnightUTC() {
        TimeZone UTC = TimeZone.getTimeZone(ZoneId.of("UTC"));
        withTimeZone(UTC, tz -> assertEquals(
                        epochPlusAYear("UTC"),
                        toDate(365d, true)
                )
        );
    }

    @Test
    public void datesGoUnchanged() {
        Date date = dateFromLocalDateTime(LocalDateTime.of(LocalDate.of(2018, 1, 1), LocalTime.MIDNIGHT));
        assertEquals(date, toDate(date, false));
        assertEquals(date, toDate(date, true));
    }

    @Test
    public void emptyStringsGoUnchanged() {
        assertEquals("", toDate("", false));
        assertEquals("", toDate("", true));
    }

    @Test
    public void doubleNaNGoesUnchanged() {
        // NaN is xpath's 'null values'
        assertEquals(Double.NaN, toDate(Double.NaN, false));
        assertEquals(Double.NaN, toDate(Double.NaN, true));
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void doubleValuesLessThanTheIntegerMinThrow() {
        toDate(Integer.valueOf(Integer.MIN_VALUE).doubleValue() - 1, false);
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void doubleValuesGreaterThanTheIntegerMaxThrow() {
        toDate(Integer.valueOf(Integer.MAX_VALUE).doubleValue() + 1, false);
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void positiveInfinityThrows() {
        toDate(POSITIVE_INFINITY, false);
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void negativeInfinityThrows() {
        toDate(NEGATIVE_INFINITY, false);
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void unparseableDateStringsThrow() {
        toDate("some random text", false);
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void booleansThrow() {
        // We test this type specifically because, according to the documentation
        // Dates can be encoded as booleans, but booleans can't be decoded into Dates
        toDate(false, false);
    }

    @Test(expected = XPathTypeMismatchException.class)
    public void anyOtherTypeThrows() {
        // We will test just one type to cover the final 'else' block.
        // We can't explicitly test for all possible types.
        toDate(2L, false);
    }
}
