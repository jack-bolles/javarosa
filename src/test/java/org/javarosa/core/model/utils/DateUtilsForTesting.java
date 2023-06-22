package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtilsForTesting {
    @NotNull
    public static Date dateFromLocalDate(LocalDate someDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        LocalTime noon = LocalTime.NOON;
        return dateFromLocalDateTime(LocalDateTime.of(someDate, noon), zoneId);
    }

    public static Date dateFromLocalDateTime(LocalDateTime someDateTime) {
        return dateFromLocalDateTime(someDateTime, ZoneId.systemDefault());
    }

    @NotNull
    public static Date dateFromLocalDateTime(LocalDateTime someDateTime, ZoneId zoneId) {
        return DateUtils.dateFrom(someDateTime, zoneId);
    }
}
