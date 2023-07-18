package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.utils.DateUtilsForTesting.dateFromLocalDate;
import static org.javarosa.core.model.utils.IPreloadHandler.DatePreloadHandler.pastPeriodDateFrom;
import static org.junit.Assert.assertEquals;

public class DateUtils_GetPastPeriodDateIncludeTodayTest {
    /** TODO - Not sure of the use case for this. It needs to be vetted.
     * The tests exist to document the functionality as I found it - JB  */
    @Test
    public void includeTodayReturnsNextLaterPeriodIfTrueAndRefDateIsTheEndOfAPeriod() {
        LocalDate endOfPeriod = LocalDate.of(2023, 6, 10);//a saturday, end of period
        Date endOfPeriodDate = dateFromLocalDate(endOfPeriod);

        boolean includeToday = true;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, !includeToday, -1));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 18)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, !includeToday, 0));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, !includeToday, 1));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, !includeToday, 2));
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                pastPeriodDateFrom(endOfPeriodDate, "week", "sun", true, includeToday, 2));
    }

    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheStartOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 4);//a sunday, start of period
        Date startOfPeriodDate = dateFromLocalDate(startOfPeriod);

        boolean includeToday = true;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, 2));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, 2));
    }


    @Test
    public void includeTodayReturnsSamePeriodIfTrueOrFalseAndRefDateIsTheMiddleOfAPeriod() {
        LocalDate startOfPeriod = LocalDate.of(2023, 6, 8);//a sunday, start of period
        Date startOfPeriodDate = dateFromLocalDate(startOfPeriod);

        boolean includeToday = true;
        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 11)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, -1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 6, 4)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, 0));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 28)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, 1));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, !includeToday, 2));

        assertEquals(dateFromLocalDate(LocalDate.of(2023, 5, 21)),
                pastPeriodDateFrom(startOfPeriodDate, "week", "sun", true, includeToday, 2));
    }

}
