package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.javarosa.core.model.utils.DateUtilsForTesting.dateFromLocalDate;
import static org.javarosa.core.model.utils.SupportedPeriod.week;
import static org.junit.Assert.assertEquals;

public class DatePreloadHandlerGetPastPeriodDateIncludeTodayTest {
    @Test
    public void includeTodayReturnsNextLaterPeriodIfTrueAndRefDateIsTheEndOfAPeriod() {
        LocalDate endOfPeriod = LocalDate.of(2023, 6, 10);//a saturday, end of period

        boolean includeToday = true;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, -1));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 18)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, 0));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, 1));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, 2));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheStartOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 4);//a sunday, start of period

        boolean includeToday = true;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 2));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheMiddleOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 8);//a sunday, start of period

        boolean includeToday = true;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 2));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 2));
    }
}
