package org.javarosa.core.model.actions;

import org.javarosa.core.test.Scenario;
import org.javarosa.xform.parse.ParseException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.javarosa.core.test.AnswerDataMatchers.intAnswer;
import static org.javarosa.core.util.BindBuilderXFormsElement.bind;
import static org.javarosa.core.util.XFormsElement.body;
import static org.javarosa.core.util.XFormsElement.head;
import static org.javarosa.core.util.XFormsElement.html;
import static org.javarosa.core.util.XFormsElement.input;
import static org.javarosa.core.util.XFormsElement.mainInstance;
import static org.javarosa.core.util.XFormsElement.model;
import static org.javarosa.core.util.XFormsElement.repeat;
import static org.javarosa.core.util.XFormsElement.setvalue;
import static org.javarosa.core.util.XFormsElement.t;
import static org.javarosa.core.util.XFormsElement.title;
import static org.javarosa.test.utils.ResourcePathHelper.r;

/**
 * Specification: <a href="https://getodk.github.io/xforms-spec/#the-odk-new-repeat-event">...</a>.
 */
public class OdkNewRepeatEventTest {
    @Test
    public void setValueOnRepeatInsertInBody_setsValueInRepeat() throws ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        assertThat(scenario.countRepeatInstancesOf("/data/my-repeat"), is(0));
        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.countRepeatInstancesOf("/data/my-repeat"), is(1));
        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-position").getDisplayText(), is("1"));
    }

    @Test
    public void addingRepeat_doesNotChangeValueSetForPreviousRepeat() throws ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-position").getDisplayText(), is("1"));

        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.answerOf("/data/my-repeat[1]/defaults-to-position").getDisplayText(), is("2"));

        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-position").getDisplayText(), is("1"));
    }

    @Test
    public void setValueOnRepeatInBody_usesCurrentContextForRelativeReferences() throws ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.answer("/data/my-toplevel-value", "12");

        scenario.createNewRepeat("/data/my-repeat");
        assertThat(scenario.answerOf("/data/my-repeat[0]/defaults-to-toplevel").getDisplayText(), is("14"));
    }

    @Test
    public void setValueOnRepeatWithCount_setsValueForEachRepeat() throws ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        scenario.answer("/data/repeat-count", 4);

        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }

        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(4));

        assertThat(scenario.answerOf("/data/my-jr-count-repeat[0]/defaults-to-position-again").getDisplayText(), is("1"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[1]/defaults-to-position-again").getDisplayText(), is("2"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[2]/defaults-to-position-again").getDisplayText(), is("3"));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[3]/defaults-to-position-again").getDisplayText(), is("4"));

        // Adding repeats should trigger odk-new-repeat for those new nodes
        scenario.answer("/data/repeat-count", 6);

        scenario.jumpToBeginningOfForm();
        while (!scenario.atTheEndOfForm()) {
            scenario.next();
        }
        assertThat(scenario.countRepeatInstancesOf("/data/my-jr-count-repeat"), is(6));
        assertThat(scenario.answerOf("/data/my-jr-count-repeat[5]/defaults-to-position-again").getDisplayText(), is("6"));

    }

    @Test
    public void repeatInFormDefInstance_neverFiresNewRepeatEvent() throws ParseException {
        Scenario scenario = Scenario.init(r("event-odk-new-repeat.xml"));

        assertThat(scenario.answerOf("/data/my-repeat-without-template[0]/my-value"), is(nullValue()));
        assertThat(scenario.answerOf("/data/my-repeat-without-template[1]/my-value"), is(nullValue()));

        scenario.createNewRepeat("/data/my-repeat-without-template");
        assertThat(scenario.answerOf("/data/my-repeat-without-template[2]/my-value").getDisplayText(), is("2"));
    }

    @Test
    public void newRepeatInstance_doesNotTriggerActionOnUnrelatedRepeat() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Parallel repeats", html(
            head(
                title("Parallel repeats"),
                model(
                    mainInstance(t("data id=\"parallel-repeats\"",
                        t("repeat1",
                            t("q1")),

                        t("repeat2",
                            t("q1")
                        )
                    ))
                )
            ),
            body(
                repeat("/data/repeat1",
                    setvalue("odk-new-repeat", "/data/repeat1/q1", "concat('foo','bar')"),
                    input("/data/repeat1/q1")
                ),
                repeat("/data/repeat2",
                    setvalue("odk-new-repeat", "/data/repeat2/q1", "concat('bar','baz')"),
                    input("/data/repeat2/q1")
                ))));

        scenario.createNewRepeat("/data/repeat1");
        scenario.createNewRepeat("/data/repeat1");

        scenario.createNewRepeat("/data/repeat2");
        scenario.createNewRepeat("/data/repeat2");

        assertThat(scenario.answerOf("/data/repeat1[1]/q1").getDisplayText(), is("foobar"));
        assertThat(scenario.answerOf("/data/repeat1[2]/q1").getDisplayText(), is("foobar"));

        assertThat(scenario.answerOf("/data/repeat2[1]/q1").getDisplayText(), is("barbaz"));
        assertThat(scenario.answerOf("/data/repeat2[2]/q1").getDisplayText(), is("barbaz"));
    }

    @Test
    public void newRepeatInstance_canUsePreviousInstanceAsDefault() throws IOException, ParseException {
        Scenario scenario = Scenario.init("Default from prior instance", html(
            head(
                title("Default from prior instance"),
                model(
                    mainInstance(t("data id=\"default-from-prior-instance\"",
                        t("repeat",
                            t("q"))
                    )),
                    bind("/data/repeat/q").type("integer")
                )
            ),
            body(
                repeat("/data/repeat",
                    setvalue("odk-new-repeat", "/data/repeat/q", "/data/repeat[position()=position(current()/..)-1]/q"),
                    input("/data/repeat/q")
                ))));
        scenario.next();
        scenario.next();
        scenario.answer(7);
        assertThat(scenario.answerOf("/data/repeat[0]/q"), is(intAnswer(7)));

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        assertThat(scenario.answerOf("/data/repeat[1]/q"), is(intAnswer(7)));
        scenario.answer(8); // override the default

        scenario.next();
        scenario.createNewRepeat();
        scenario.next();
        assertThat(scenario.answerOf("/data/repeat[0]/q"), is(intAnswer(7)));
        assertThat(scenario.answerOf("/data/repeat[1]/q"), is(intAnswer(8)));
        assertThat(scenario.answerOf("/data/repeat[2]/q"), is(intAnswer(8)));
    }

    // Not part of ODK XForms so throws parse exception.
    @Test(expected = ParseException.class)
    public void setValueOnRepeatInsertInModel_notAllowed() throws ParseException {
        Scenario.init(r("event-odk-new-repeat-model.xml"));
    }
}
