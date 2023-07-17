import org.javarosa.core.model.DataType;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.TimeData;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import static org.javarosa.core.model.DataType.BOOLEAN;
import static org.javarosa.core.model.utils.DateUtils.localDateFrom;
import static org.javarosa.core.model.utils.DateUtils.localTimeFrom;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AnswerDataMoreQuestionsThanAnswersTest {

    /** this should be BooleanData(false) */
    @Test
    public void booleanDataTypeCantCoerceEmptyStringToFalse_BUG_Q(){
        assertNull(IAnswerData.wrapData("", BOOLEAN.value));
    }

    /**
     * @Deprecated - allow deprecated val-type combinations to still work
     * until this subsystem is better understood
     */
    @Test
    public void DATEtypeCanWrapAroundUtilDate() {
        Date now = Date.from(Instant.now());
        DateData expected = new DateData(localDateFrom(now));
        IAnswerData answerData = IAnswerData.wrapData(now, DataType.DATE.value);
        assertEquals(expected, answerData);
    }

    /** @Deprecated - allow deprecated val-type combinations to still work until tis subsystem is better understood */
    @Test
    public void TIMEtypeCanWrapAroundUtilDate(){
        Date now = Date.from(Instant.now());
        TimeData expected = new TimeData(localTimeFrom(now));
        IAnswerData answerData = IAnswerData.wrapData(now, DataType.TIME.value);
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
