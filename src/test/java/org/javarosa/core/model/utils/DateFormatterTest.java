package org.javarosa.core.model.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.utils.DateUtils.dateFromLocalDate;
import static org.junit.Assert.assertEquals;

public class DateFormatterTest {
    @Test
    public void formatsDateAsISO8601(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_ISO8601);
        assertEquals("2023-06-11", formattedDate);
    }

    @Test public void formatsDateAsDaysFromToday(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY);
        assertEquals("11/06/23", formattedDate);
    }

    @Test public void formatsDateAsHumanShort(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_HUMAN_READABLE_SHORT);
        assertEquals("11/06/23", formattedDate);
    }
    @Test public void formatsDateAsTimeStampSuffix(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_TIMESTAMP_SUFFIX);
        assertEquals("20230611", formattedDate);
    }

    @Test public void formatsDateAsTimeStampHTTP(){
        LocalDate someLocalDate = LocalDate.of(2023, 6, 11);
        Date dateToTest = dateFromLocalDate(someLocalDate);
        String formattedDate = DateFormatter.formatDate(dateToTest, DateFormatter.FORMAT_TIMESTAMP_HTTP);
        assertEquals("Sat, 10 Jun 2023", formattedDate);
    }

}