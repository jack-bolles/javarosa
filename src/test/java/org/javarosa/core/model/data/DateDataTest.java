package org.javarosa.core.model.data;

import org.junit.Test;

import java.time.LocalDate;

import static org.javarosa.core.model.data.DateData.dataFrom;
import static org.javarosa.core.model.data.DateData.parseDate;
import static org.junit.Assert.assertEquals;

public class DateDataTest {
    @Test
    public void testCastingAValidISODateString() {
        String validISODateString = "2016-04-13T16:26:00.000";
        DateData data = dataFrom(validISODateString);

        assertEquals(
                parseDate(validISODateString),
                data.getValue()
        );
    }

    @Test
    public void testCastingAValidISODateStringEmptyStringDelimiter() {
        String dateString = "2016-04-13 16:26:00.000";
        DateData data = dataFrom(dateString);

        assertEquals(
                parseDate(dateString),
                data.getValue()
        );
    }

    @Test
    public void datesIgnoreTimeElement() {
        String anISODateString = "2016-04-13T16:26:00.000";
        String anotherISODateString = "2016-04-13T00:00:00.000";
        DateData adate = dataFrom(anISODateString);
        DateData anotherdate = dataFrom(anotherISODateString);

        assertEquals(
                adate.getValue(),
                anotherdate.getValue()
        );

        //TODO - should IAnswerData impls check equality by value or reference?
        // these fail, even though checking equality of the underling values passes
//        assertEquals(
//                adate,
//                anotherdate
//        );
//
//        assertEquals(
//                adate.uncast(),
//                anotherdate.uncast()
//        );
    }

    @Test
    public void casting() {
        String anISODateString = "2016-04-13 16:26:00.000";
        DateData adate = dataFrom(anISODateString);

        assertEquals(
                "2016-04-13",
                adate.uncast().value
        );

        assertEquals(
                "2016-04-13",
                adate.uncast().value
        );
    }

    @Test(expected = NullPointerException.class)
    public void dateDataCanNotBePassedNUllForItsUnderlyingValue() {
        DateData data = new DateData(LocalDate.now());
        data.setValue(null);
    }

    @Test(expected = NullPointerException.class)
    public void serialisationFun() {
        DateData data = new DateData(); //should only be used for deserialisation purposes
        data.getValue();
    }
}