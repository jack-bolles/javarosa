package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.utils.DateUtilsForTesting.dateFromLocalDate;
import static org.javarosa.core.model.utils.SupportedPeriod.week;
import static org.junit.Assert.assertEquals;

public class DatePreloadHandlerGetPastPeriodDateIncludeTodayTest {
    @Test
    public void includeTodayReturnsNextLaterPeriodIfTrueAndRefDateIsTheEndOfAPeriod() {
        LocalDate endOfPeriod = LocalDate.of(2023, 6, 10);//a saturday, end of period
        Date endOfPeriodDate = dateFromLocalDate(endOfPeriod);

        boolean includeToday = true;
        boolean includeToday4 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday4, -1));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 18)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday, -1));

        boolean includeToday3 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday3, 0));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday, 0));

        boolean includeToday2 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday2, 1));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday, 1));

        boolean includeToday1 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday1, 2));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(endOfPeriodDate, "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheStartOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 4);//a sunday, start of period
        Date startOfPeriodDate = dateFromLocalDate(startOfPeriod);

        boolean includeToday = true;
        boolean includeToday4 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday4, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, -1));

        boolean includeToday3 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday3, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, 0));

        boolean includeToday2 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday2, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, 1));

        boolean includeToday1 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday1, 2));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheMiddleOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 8);//a sunday, start of period
        Date startOfPeriodDate = dateFromLocalDate(startOfPeriod);

        boolean includeToday = true;
        boolean includeToday4 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday4, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, -1));

        boolean includeToday3 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday3, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, 0));

        boolean includeToday2 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday2, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, 1));

        boolean includeToday1 = !includeToday;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday1, 2));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriodDate, "sun", true, includeToday, 2));
    }
}
