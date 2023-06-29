package org.javarosa.core.model.data;

import org.javarosa.core.model.DataType;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.javarosa.core.model.DataType.BOOLEAN;
import static org.javarosa.core.model.DataType.DECIMAL;
import static org.javarosa.core.model.DataType.INTEGER;
import static org.javarosa.core.model.DataType.LONG;
import static org.junit.Assert.assertEquals;

public class AnswerDataTest {

    @Test
    public void booleansWrapByValueFirst() {
        Stream<DataType> dataTypeStream = Arrays
                .stream(DataType.values())
                .filter(type -> type.value != BOOLEAN.value);
        dataTypeStream.forEach(dataType ->
                assertEquals(IAnswerData.wrapData(true, dataType.value), new BooleanData(true)));
    }

    @Test public void booleanDataTypeCanCoerceDoublesAndStringValues(){
        //TODO - empty string returns null, not false; despite DataType.BOOLEAN ... bug?
//        assertEquals(IAnswerData.wrapData("", BOOLEAN.value), new BooleanData(false));
        assertEquals(IAnswerData.wrapData("not blank", BOOLEAN.value), new BooleanData(true));
        assertEquals(IAnswerData.wrapData(0.0, BOOLEAN.value), new BooleanData(false));
        assertEquals(IAnswerData.wrapData(0.001, BOOLEAN.value), new BooleanData(true));
    }

    @Test
    public void doublesWrapAccordingToTheirNumericType() {
        double val = 11.42;
        assertEquals(IAnswerData.wrapData(val, INTEGER.value), new IntegerData((int) val));
        assertEquals(IAnswerData.wrapData(val, LONG.value), new LongData((long) val));
        assertEquals(IAnswerData.wrapData(val, DECIMAL.value), new DecimalData(val));
    }

    @Test
    public void DATEtypeDoesntWrapNumbers() {
        IAnswerData answerData = IAnswerData.wrapData(42.0, DataType.DATE.value);
        assertEquals(new IntegerData(42), answerData);
    }
    @Test(expected = UnsupportedOperationException.class)
    public void DATEtypeDoesntWrapText() {
        IAnswerData.wrapData("1970-1-1", DataType.DATE.value);
    }

    @Test
    public void canWrapAroundLocalDateValAndDATEtype() {
        DateData expected = new DateData(LocalDate.now());
        IAnswerData answerData = IAnswerData.wrapData(LocalDate.now(), DataType.DATE.value);
        assertEquals(expected, answerData);
    }
    @Test
    public void canWrapAroundLocalDateTimeAndDATEtype() {
        LocalTime time = LocalTime.now();
        TimeData expected = new TimeData(time);
        IAnswerData answerData = IAnswerData.wrapData(time, DataType.DATE.value);
        assertEquals(expected, answerData);
    }

}
