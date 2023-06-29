/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Stream;

import static java.time.LocalDateTime.of;
import static java.util.TimeZone.getTimeZone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.javarosa.core.model.utils.DateUtils.TIME_OFFSET_REGEX;
import static org.javarosa.core.model.utils.DateUtils.timeAndOffset;
import static org.javarosa.test.utils.SystemHelper.withTimeZone;

@RunWith(Parameterized.class)
public class DateUtilsParseTimeTests {
    @Parameterized.Parameter()
    public String input;

    @Parameterized.Parameter(value = 1)
    public Temporal expectedTime;

    @Parameterized.Parameters(name = "Input: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"14:00", LocalTime.parse("14:00")},
                {"14:00Z", OffsetTime.parse("14:00Z")},
                {"14:00+02", OffsetTime.parse("14:00+02:00")},
                {"14:00-02", OffsetTime.parse("14:00-02:00")},
                {"14:00+02:30", OffsetTime.parse("14:00+02:30")},
                {"14:00-02:30", OffsetTime.parse("14:00-02:30")},
        });
    }

    @Test
    public void parseTime_produces_expected_results_in_all_time_zones() {
        // The tricky part of DateUtils.parseTime is that we allow for input time
        // values to include time offset declarations, which has issues at different
        // levels:
        // - Conceptually, time values with offsets don't make sense until they're
        //   paired with a date so, how can we reason about what "10:00+02:00" means,
        //   and what should be a valid expected output for that input value?
        // - Next, DateUtils.parseTime() produces Date values, which are a date and a
        //   time in the system's default time zone. Then, which date would we have to
        //   expect?
        //
        // To solve these issues, DateUtils.parseTime() will use the system's current
        // date as a base for its output value whenever that is and whichever time zone
        // the system's at.
        //
        // By testing parseTime under different system default time zones we're trying
        // to have the confidence that our resulting Date objects will always translate
        // to the same time declaration from the input string (ignoring their date part,
        // of course).
        Stream.of(
                TimeZone.getDefault(),
                getTimeZone("UTC"),
                getTimeZone("GMT+12"),
                getTimeZone("GMT-13"),
                getTimeZone("GMT+0230")
        ).forEach(tz -> withTimeZone(tz, () -> assertThat(parseTime(input), is(expectedTime))));
    }

    @Test
    public void splittingTimes() {
        String regex = "(?=[Z+\\-])";

        String[] timePieces = "10:00:00.000+02:00".split(regex);
        assertThat(timePieces.length, is(2));
        assertThat(timePieces[1], is("+02:00"));

        timePieces = "10:00:00.000-02:00".split(regex);
        assertThat(timePieces.length, is(2));
        assertThat(timePieces[1], is("-02:00"));

        timePieces = "10:00:00.000Z".split(regex);
        assertThat(timePieces.length, is(2));
        assertThat(timePieces[1], is("Z"));

        timePieces = "10:00:00.000".split(regex);
        assertThat(timePieces.length, is(1));
    }

    /**
     * Returns a LocalTime or a OffsetTime obtained from the result of
     * calling DateUtils.parseTime() with the provided input.
     * <p>
     * The interim OffsetDateTime value ensures that it represents the
     * same instant as the Date from the call to DateUtils.parseTime().
     */
    private Temporal parseTime(String input) {
        Instant inputInstant = todayAt(input).toInstant();
        String[] timePieces = input.split(TIME_OFFSET_REGEX);
        return timePieces.length == 2
                ? OffsetDateTime.ofInstant(inputInstant, ZoneOffset.of(timePieces[1])).toOffsetTime()
                : OffsetDateTime.ofInstant(inputInstant, ZoneOffset.UTC).toLocalTime();
    }

    private static Date todayAt(String str) {
        DateUtils.TimeAndOffset to = timeAndOffset(str);
        return DateUtils.dateFrom(of(LocalDate.now(), to.localTime), to.zoneOffset);
    }

}
