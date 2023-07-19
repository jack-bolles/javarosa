package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.javarosa.core.model.utils.SupportedPeriod.week;
import static org.junit.Assert.assertEquals;

public class DatePreloadHandlerGetPastPeriodDateIncludeTodayTest {
    @Test
    public void includeTodayReturnsNextFurtherPeriodIfTrueAndBeginningIsFalseAndRefDateIsTheEndOfAPeriod() {
        LocalDate endOfPeriod = LocalDate.of(2023, 6, 10);//a saturday, end of period

        boolean includeToday = true;
        assertEquals(LocalDate.of(2023, 6, 24),
                week.pastPeriodFrom(endOfPeriod, "sun", false, !includeToday, -1));
        assertEquals(LocalDate.of(2023, 6, 17),
                week.pastPeriodFrom(endOfPeriod, "sun", false, includeToday, -1));

        assertEquals(LocalDate.of(2023, 6, 3),
                week.pastPeriodFrom(endOfPeriod, "sun", false, !includeToday, 0));
        assertEquals(LocalDate.of(2023, 6, 10),
                week.pastPeriodFrom(endOfPeriod, "sun", false, includeToday, 0));

        assertEquals(LocalDate.of(2023, 5, 27),
                week.pastPeriodFrom(endOfPeriod, "sun", false, !includeToday, 1));
        assertEquals(LocalDate.of(2023, 6, 3),
                week.pastPeriodFrom(endOfPeriod, "sun", false, includeToday, 1));

        assertEquals(LocalDate.of(2023, 5, 20),
                week.pastPeriodFrom(endOfPeriod, "sun", false, !includeToday, 2));
        assertEquals(LocalDate.of(2023, 5, 27),
                week.pastPeriodFrom(endOfPeriod, "sun", false, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsNextFurtherPeriodIfTrueAndRefDateIsTheEndOfAPeriod() {
        LocalDate endOfPeriod = LocalDate.of(2023, 6, 10);//a saturday, end of period

        boolean includeToday = true;
        assertEquals(LocalDate.of(2023, 6, 11),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, -1));
        assertEquals(LocalDate.of(2023, 6, 11),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, -1));

        assertEquals(LocalDate.of(2023, 6, 4),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, 0));
        assertEquals(LocalDate.of(2023, 6, 4),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, 0));

        assertEquals(LocalDate.of(2023, 5, 28),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, 1));
        assertEquals(LocalDate.of(2023, 5, 28),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, 1));

        assertEquals(LocalDate.of(2023, 5, 21),
                week.pastPeriodFrom(endOfPeriod, "sun", true, !includeToday, 2));
        assertEquals(LocalDate.of(2023, 5, 21),
                week.pastPeriodFrom(endOfPeriod, "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheStartOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 4);//a sunday, start of period

        boolean includeToday = true;
        assertEquals(LocalDate.of(2023, 6, 11),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, -1));

        assertEquals(LocalDate.of(2023, 6, 11),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, -1));

        assertEquals(LocalDate.of(2023, 6, 4),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 0));

        assertEquals(LocalDate.of(2023, 6, 4),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 0));

        assertEquals(LocalDate.of(2023, 5, 28),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 1));

        assertEquals(LocalDate.of(2023, 5, 28),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 1));

        assertEquals(LocalDate.of(2023, 5, 21),
                week.pastPeriodFrom(startOfPeriod, "sun", true, !includeToday, 2));

        assertEquals(LocalDate.of(2023, 5, 21),
                week.pastPeriodFrom(startOfPeriod, "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheMiddleOfAPeriod() {
        LocalDate middleOfPeriod = LocalDate.of(2023, 6, 8);//a thursday, middle of period

        boolean includeToday = true;
        assertEquals(LocalDate.of(2023, 6, 11),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, !includeToday, -1));

        assertEquals(LocalDate.of(2023, 6, 11),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, includeToday, -1));

        assertEquals(LocalDate.of(2023, 6, 4),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, !includeToday, 0));

        assertEquals(LocalDate.of(2023, 6, 4),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, includeToday, 0));

        assertEquals(LocalDate.of(2023, 5, 28),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, !includeToday, 1));

        assertEquals(LocalDate.of(2023, 5, 28),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, includeToday, 1));

        assertEquals(LocalDate.of(2023, 5, 21),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, !includeToday, 2));

        assertEquals(LocalDate.of(2023, 5, 21),
                week.pastPeriodFrom(middleOfPeriod, "sun", true, includeToday, 2));
    }
}
