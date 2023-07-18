package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import static org.javarosa.core.model.utils.DateUtilsForTesting.dateFromLocalDate;
import static org.javarosa.core.model.utils.SupportedPeriod.week;
import static org.junit.Assert.assertEquals;

public class DatePreloadHandlerGetPastPeriodDateTest {

    @Test
    public void testGetPastPeriodDateFindsRespectsBeginningParameter() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 8);//a thursday

        LocalDate expectedPeriodBeginningLocalDate = LocalDate.of(2023, 6, 4);//a sunday
        LocalDate expectedPeriodEndingLocalDate = LocalDate.of(2023, 6, 10);//a saturday

        boolean beginning = true;
        Date pastPeriodBeginningDate = week.pastPeriodFrom(someLocalDate, "sun", beginning, false, 0);
        assertEquals(Calendar.SUNDAY, dayOfWeek(pastPeriodBeginningDate));
        assertEquals(dateFromLocalDate(expectedPeriodBeginningLocalDate), pastPeriodBeginningDate);

        Date pastPeriodEndingDate = week.pastPeriodFrom(someLocalDate, "sun", !beginning, false, 0);
        assertEquals(Calendar.SATURDAY, dayOfWeek(pastPeriodEndingDate));
        assertEquals(dateFromLocalDate(expectedPeriodEndingLocalDate), pastPeriodEndingDate);
    }

    @Test
    public void testPeriodCanStartAnyDayOfTheWeek(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 8);//a thursday

        LocalDate expectedPeriodBeginningLocalDate = LocalDate.of(2023, 6, 7);//a wednesday
        LocalDate expectedPeriodEndingLocalDate = LocalDate.of(2023, 6, 13);//a tuesday

        boolean beginning = true;
        String start = "wed";
        Date pastPeriodBeginningDate = week.pastPeriodFrom(someLocalDate, start, beginning, false, 0);
        assertEquals(Calendar.WEDNESDAY, dayOfWeek(pastPeriodBeginningDate));
        assertEquals(dateFromLocalDate(expectedPeriodBeginningLocalDate), pastPeriodBeginningDate);

        Date pastPeriodEndingDate = week.pastPeriodFrom(someLocalDate, start, !beginning, false, 0);
        assertEquals(Calendar.TUESDAY, dayOfWeek(pastPeriodEndingDate));
        assertEquals(dateFromLocalDate(expectedPeriodEndingLocalDate), pastPeriodEndingDate);
    }

    @Test
    public void testGetPastPeriodDateFindsCorrectWeek() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 8);//a thursday

        LocalDate expectedLocalDate = LocalDate.of(2023, 5, 21);//a sunday
        Date expectedDate = dateFromLocalDate(expectedLocalDate);

        int periodsAgo = 2;
        Date pastPeriodDate = week.pastPeriodFrom(someLocalDate, "sun", true, false, periodsAgo);
        assertEquals(Calendar.SUNDAY, dayOfWeek(pastPeriodDate));
        assertEquals(expectedDate, pastPeriodDate);
    }

    private static int dayOfWeek(Date pastPeriodDate) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(pastPeriodDate);
        return cd.get(Calendar.DAY_OF_WEEK);
    }
}