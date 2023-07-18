package org.javarosa.core.model.utils;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DatePreloadHandlerTest {
    @Test
    public void canParsePreviousPeriod() {
        IPreloadHandler.DatePreloadHandler handler = new IPreloadHandler.DatePreloadHandler();
        IAnswerData answerData = handler.handlePreload("prevperiod-week-sun-head");
        Object actual = answerData.getValue();
        LocalDate expected = LocalDate.of(2023, 7, 9);
        assertEquals(expected, actual);
    }

    @Test
    public void weekIsTheOnlySupportedPreviousPeriod() {
        IPreloadHandler.DatePreloadHandler handler = new IPreloadHandler.DatePreloadHandler();
        assertTrue(handler.handlePreload("prevperiod-week-sun-head").getValue() instanceof LocalDate);

        try {
            IAnswerData answerData = handler.handlePreload("prevperiod-month-sun-head");
            fail("year is not supported");
        } catch (IllegalArgumentException e) { }
        try {
            IAnswerData answerData = handler.handlePreload("prevperiod-quarter-sun-head");
            fail("year is not supported");
        } catch (IllegalArgumentException e) { }
        try {
            IAnswerData answerData = handler.handlePreload("prevperiod-year-sun-head");
            fail("year is not supported");
        } catch (IllegalArgumentException e) { }
    }

}