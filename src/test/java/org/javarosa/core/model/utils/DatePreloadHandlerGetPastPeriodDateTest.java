package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.javarosa.core.model.utils.SupportedPeriod.week;
import static org.junit.Assert.assertEquals;

public class DatePreloadHandlerGetPastPeriodDateTest {

    @Test
    public void testGetPastPeriodDateFindsRespectsBeginningParameter() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 8);//a thursday

        LocalDate expectedPeriodBeginningLocalDate = LocalDate.of(2023, 6, 4);//a sunday
        LocalDate expectedPeriodEndingLocalDate = LocalDate.of(2023, 6, 10);//a saturday

        boolean beginning = true;
        LocalDate pastPeriodBeginningDate = week.pastPeriodFrom(someLocalDate, "sun", beginning, false, 0);
        assertEquals(DayOfWeek.SUNDAY, dayOfWeek(pastPeriodBeginningDate));
        assertEquals(expectedPeriodBeginningLocalDate, pastPeriodBeginningDate);

        LocalDate pastPeriodEndingDate = week.pastPeriodFrom(someLocalDate, "sun", !beginning, false, 0);
        assertEquals(DayOfWeek.SATURDAY, dayOfWeek(pastPeriodEndingDate));
        assertEquals(expectedPeriodEndingLocalDate, pastPeriodEndingDate);
    }

    @Test
    public void testPeriodCanStartAnyDayOfTheWeek(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 8);//a thursday

        LocalDate expectedPeriodBeginningLocalDate = LocalDate.of(2023, 6, 7);//a wednesday
        LocalDate expectedPeriodEndingLocalDate = LocalDate.of(2023, 6, 13);//a tuesday

        boolean beginning = true;
        String start = "wed";
        LocalDate pastPeriodBeginningDate = week.pastPeriodFrom(someLocalDate, start, beginning, false, 0);
        assertEquals(DayOfWeek.WEDNESDAY, dayOfWeek(pastPeriodBeginningDate));
        assertEquals(expectedPeriodBeginningLocalDate, pastPeriodBeginningDate);

        LocalDate pastPeriodEndingDate = week.pastPeriodFrom(someLocalDate, start, !beginning, false, 0);
        assertEquals(DayOfWeek.TUESDAY, dayOfWeek(pastPeriodEndingDate));
        assertEquals(expectedPeriodEndingLocalDate, pastPeriodEndingDate);
    }

    @Test
    public void testGetPastPeriodDateFindsCorrectWeek() {
        LocalDate someLocalDate = LocalDate.of(2023, 6, 8);//a thursday
        LocalDate expectedLocalDate = LocalDate.of(2023, 5, 21);

        int periodsAgo = 2;
        LocalDate pastPeriodDate = week.pastPeriodFrom(someLocalDate, "sun", true, false, periodsAgo);
        assertEquals(DayOfWeek.SUNDAY, dayOfWeek(pastPeriodDate));
        assertEquals(expectedLocalDate, pastPeriodDate);
    }

    private static DayOfWeek dayOfWeek(LocalDate periodDate) {
        return periodDate.getDayOfWeek();
    }
}