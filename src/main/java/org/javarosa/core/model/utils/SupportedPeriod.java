package org.javarosa.core.model.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

public enum SupportedPeriod {
    week() {
        @Override
        LocalDate pastPeriodFrom(LocalDate anchorDate, String start, boolean beginning, boolean includeToday, int nAgo) {
            DayOfWeek currentDOW = anchorDate.getDayOfWeek();
            DayOfWeek targetDOWStart = dayToDay.get(start);
            DayOfWeek targetDOWEnd = targetDOWStart.plus(6);

            if (!beginning && !includeToday && currentDOW.equals(targetDOWEnd)) nAgo = nAgo >= 0 ? nAgo + 1 : nAgo - 1;

            LocalDate theDate = anchorDate.minusWeeks(nAgo).with(TemporalAdjusters.previousOrSame(targetDOWStart));
            if (!beginning) theDate = theDate.plusDays(6);

            return theDate;
        }
    };

    private static final Map<String, DayOfWeek> dayToDay = Stream.of(new AbstractMap.SimpleImmutableEntry<>("sun", SUNDAY), new AbstractMap.SimpleImmutableEntry<>("mon", MONDAY), new AbstractMap.SimpleImmutableEntry<>("tue", TUESDAY), new AbstractMap.SimpleImmutableEntry<>("wed", WEDNESDAY), new AbstractMap.SimpleImmutableEntry<>("thu", THURSDAY), new AbstractMap.SimpleImmutableEntry<>("fri", FRIDAY), new AbstractMap.SimpleImmutableEntry<>("sat", SATURDAY)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    /**
     * Creates a Date object representing the amount of time between the
     * reference date, and the given parameters.
     *
     * @param anchorDate   The starting point of reference date
     * @param start        "sun", "mon", ... etc. representing the start of the time period.
     * @param beginning    true=return first day of period, false=return last day of period
     * @param includeToday Whether today's date can count as the last day of the period
     * @param nAgo         How many periods ago. 1=most recent period, 0=period in progress
     * @return a LocalDate object representing the amount of time between the
     * reference date, and the given parameters.
     */
    abstract LocalDate pastPeriodFrom(LocalDate anchorDate, String start, boolean beginning, boolean includeToday, int nAgo);
}