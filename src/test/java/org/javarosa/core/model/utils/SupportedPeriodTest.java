package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;

import static org.javarosa.core.model.utils.SupportedPeriod.week;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SupportedPeriodTest {
    @Test
    public void canParsePreviousPeriods() {
        LocalDate testDate = LocalDate.of(2023, 7, 18);
        //        prevperiod-week-sun-head
        assertEquals(LocalDate.of(2023, 7, 9),
                week.pastPeriodFrom(testDate, "sun", true, true, 1));
        assertEquals(LocalDate.of(2023, 6, 25),
                week.pastPeriodFrom(testDate, "sun", true, true, 3));
    }

    @Test
    public void canParsePreviousPeriodIncludeToday() {
        LocalDate testDate = LocalDate.of(2023, 7, 18); //a Tuesday
        //        prevperiod-week-wed-head-x
        assertEquals(LocalDate.of(2023, 7, 12),
                week.pastPeriodFrom(testDate, "wed", true, true, 0));
        assertEquals(LocalDate.of(2023, 7, 5),
                week.pastPeriodFrom(testDate, "wed", true, true, 1));
        //        prevperiod-week-wed-head-
        assertEquals(LocalDate.of(2023, 7, 5),
                week.pastPeriodFrom(testDate, "wed", true, false, 1));
        assertEquals(LocalDate.of(2023, 6, 28),
                week.pastPeriodFrom(testDate, "wed", true, false, 2));
    }

    @Test
    public void canParsePreviousPeriodEachEnd() {
        LocalDate testDate = LocalDate.of(2023, 7, 18); //a Tuesday
        //        prevperiod-week-sun-head
        assertEquals(LocalDate.of(2023, 7, 5),
                week.pastPeriodFrom(testDate, "wed", true, false, 1));
        //        prevperiod-week-sun-head-x
        assertEquals(LocalDate.of(2023, 7, 5),
                week.pastPeriodFrom(testDate, "wed", true, true, 1));
        //        prevperiod-week-sun-tail
        assertEquals(LocalDate.of(2023, 7, 4),
                week.pastPeriodFrom(testDate, "wed", false, false, 1));
        //        prevperiod-week-sun-tail-x
        assertEquals(LocalDate.of(2023, 7, 11),
                week.pastPeriodFrom(testDate, "wed", false, true, 1));
    }

    @Test
    public void weekIsTheOnlySupportedPreviousPeriod() {
        IPreloadHandler.DatePreloadHandler handler = new IPreloadHandler.DatePreloadHandler();
        assertTrue(handler.handlePreload("prevperiod-week-sun-head").getValue() instanceof LocalDate);

        try {
            handler.handlePreload("prevperiod-month-sun-head");
            fail("month is not supported");
        } catch (IllegalArgumentException e) {
        }
        try {
            handler.handlePreload("prevperiod-quarter-sun-head");
            fail("quarter is not supported");
        } catch (IllegalArgumentException e) {
        }
        try {
            handler.handlePreload("prevperiod-year-sun-head");
            fail("year is not supported");
        } catch (IllegalArgumentException e) {
        }
    }

}