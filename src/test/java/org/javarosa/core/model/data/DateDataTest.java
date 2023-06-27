package org.javarosa.core.model.data;

import org.javarosa.core.model.utils.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.javarosa.core.model.data.DateData.dataFrom;
import static org.javarosa.core.model.data.DateData.parseDate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DateDataTest {
    private Date today;
    private Date notToday;

    @Before
    public void setUp() throws Exception {
        today = DateUtils.roundDate(new Date());
        notToday = DateUtils.roundDate(new Date(today.getTime() - today.getTime() / 2));
    }

    @Test
    public void testCastingAValidISODateString(){
        String validISODateString = "2016-04-13T16:26:00.000";
        DateData data = dataFrom(validISODateString);

        assertEquals(
                parseDate(validISODateString),
                data.getValue()
        );
    }

    @Test
    public void testCastingAValidISODateStringEmptyStringDelimiter(){
        String dateString = "2016-04-13 16:26:00.000";
        DateData data = dataFrom(dateString);

        assertEquals(
                parseDate(dateString),
                data.getValue()
        );
    }

    @Test
    public void datesIgnoreTimeElement(){
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
    public void casting(){
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

    @Test
    public void testDateDataKeepsCopiesTheSetDateEffectivelyMakingTheUnderlyingValueImmutable() {
        DateData data = new DateData(today);
        assertEquals("Setting during construction doesn't work properly", data.getValue(), today);

        Date temp = new Date(today.getTime());
        today.setTime(1234);
        assertEquals("DateData's value was mutated after being set on DateData, incorrectly", data.getValue(), temp);

        Date rep = (Date) data.getValue();
        rep.setTime(rep.getTime() - 1000);
        assertEquals("DateData's underlying value was mutated when the value returned by getValue was mutated, incorrectly", data.getValue(), temp);
    }

    @Test
    public void testSetData() {
        DateData data = new DateData(notToday);
        data.setValue(today);
        assertNotEquals("DateData did not set value properly. Maintained old value.", data.getValue(), notToday);
        assertEquals("DateData did not properly set value ", data.getValue(), today);

        data.setValue(notToday);
        assertNotEquals("DateData did not set value properly. Maintained old value.", data.getValue(), today);
        assertEquals("DateData did not properly reset value ", data.getValue(), notToday);

        Date temp = new Date(notToday.getTime());
        notToday.setTime(notToday.getTime() - 1324);
        assertEquals("DateData's value was mutated incorrectly", data.getValue(), temp);
    }

    @Test(expected = NullPointerException.class)
    public void dateDataCanNotBePassedNUllForItsUnderlyingValue() {
        DateData data = new DateData(today);
        data.setValue(null);
    }

    @Test(expected = NullPointerException.class)
    public void serialisationFun() {
        DateData data = new DateData(); //should only be used for deserialisation purposes
        data.getValue();
    }
}