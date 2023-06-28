package org.javarosa.core.model.data;

import org.javarosa.core.model.DataType;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.utils.DateUtils.localDateFrom;
import static org.junit.Assert.assertEquals;

public class AnswerDataTest {

    @Test
    public void wrapDataCanWrapAroundLocalDate(){
        DateData expected = new DateData(LocalDate.now());
        IAnswerData answerData = IAnswerData.wrapData(LocalDate.now(), DataType.DATE.value);
        assertEquals(expected, answerData);
    }
    @Test
    public void wrapDataCanWrapAroundUtilDate(){
        Date now = Date.from(Instant.now());
        DateData expected = new DateData(localDateFrom(now));
        IAnswerData answerData = IAnswerData.wrapData(now, DataType.DATE.value);
        assertEquals(expected, answerData);
    }

    /** see WhoVATest smoke test for the source of this path through the system.
     * TODO - Not sure if it's as expected or the smoke test needs updating.
     * For now, we allow the behaviour.
     */
    @Test
    public void localDateIsWrappedWhenDataTypeIsTEXT(){
        DateData expected = new DateData(LocalDate.now());
        IAnswerData answerData = IAnswerData.wrapData(LocalDate.now(), DataType.TEXT.value);
        assertEquals(expected, answerData);

    }
}
