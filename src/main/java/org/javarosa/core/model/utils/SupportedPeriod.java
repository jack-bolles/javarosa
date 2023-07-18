package org.javarosa.core.model.utils;

import java.util.Calendar;
import java.util.Date;

public enum SupportedPeriod {
    week(){
        public Date pastPeriodFrom(Date ref, String start, boolean beginning, boolean includeToday, int nAgo){
            Calendar cd = Calendar.getInstance();
            cd.setTime(ref);
            int current_dow = cd.get(Calendar.DAY_OF_WEEK) - 1;
            int target_dow = DOW.valueOf(start).order;
            int offset = (includeToday ? 1 : 0);
            int diff = ((current_dow - target_dow + 7 + offset) % 7 - offset) + (7 * nAgo) - (beginning ? 0 : 6);
            return new Date(ref.getTime() - diff * IPreloadHandler.DatePreloadHandler.DAY_IN_MS);
        }
    };

    /**
     * Creates a Date object representing the amount of time between the
     * reference date, and the given parameters.
     *
     * @param ref          The starting reference date
     * @param start        "sun", "mon", ... etc. representing the start of the time period.
     * @param beginning    true=return first day of period, false=return last day of period
     * @param includeToday Whether today's date can count as the last day of the period
     * @param nAgo         How many periods ago. 1=most recent period, 0=period in progress
     * @return a Date object representing the amount of time between the
     * reference date, and the given parameters.
     */
    public Date pastPeriodFrom(Date ref, String start, boolean beginning, boolean includeToday, int nAgo) {throw new IllegalArgumentException("Use the enum instance");}
}

enum DOW {
    sun(0), mon(1), tue(2), wed(3), thu(4), fri(5), sat(6);
    final int order;

    DOW(int ordinal) {
        this.order = ordinal;
    }
}
