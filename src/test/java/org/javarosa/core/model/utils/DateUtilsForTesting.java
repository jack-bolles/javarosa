package org.javarosa.core.model.utils;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateUtilsForTesting {

    public static Date dateFromLocalDateTime(LocalDateTime someDateTime) {
        return dateFromLocalDateTime(someDateTime, ZoneId.systemDefault());
    }

    @NotNull
    public static Date dateFromLocalDateTime(LocalDateTime someDateTime, ZoneId zoneId) {
        return DateUtils.dateFrom(someDateTime, zoneId);
    }
}
